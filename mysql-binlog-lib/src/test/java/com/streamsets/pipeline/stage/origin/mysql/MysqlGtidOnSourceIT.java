/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.mysql;

import static com.streamsets.pipeline.api.Field.create;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.shyiko.mysql.binlog.GtidSet;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.sdk.SourceRunner;
import com.streamsets.pipeline.sdk.StageRunner;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

public class MysqlGtidOnSourceIT extends AbstractMysqlSource {
  @Rule
  public MySQLContainer mysql = new MySQLContainer("mysql:5.6").withConfigurationOverride("mysql_gtid_on");

  @Override
  public MySQLContainer createMysqlContainer() {
    return mysql;
  }

  @Test
  public void shouldWriteGtidAndSeqNoAndIncompleteTx() throws Exception {
    MysqlSourceConfig config = createConfig("root");
    MysqlSource source = createMysqlSource(config);
    SourceRunner runner = new SourceRunner.Builder(MysqlDSource.class, source)
        .addOutputLane(LANE)
        .build();
    runner.runInit();

    String serverGtid = getNextServerGtid();

    StageRunner.Output output = runner.runProduce(null, MAX_BATCH_SIZE);
    List<Record> records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, is(Matchers.<Record>empty()));

    // add one more
    execute(ds, Arrays.asList(
        "INSERT INTO foo (bar) VALUES (2)",
        "INSERT INTO foo (bar) VALUES (3)")
    );
    output = runner.runProduce(null, MAX_BATCH_SIZE);
    records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, hasSize(2));

    assertThat(records.get(0).get("/GTID").getValueAsString(), is(serverGtid));
    assertThat(records.get(0).get("/SeqNo"), is(create(1L)));
    String offset = records.get(0).get("/Offset").getValueAsString();
    GtidSourceOffset go = GtidSourceOffset.parse(offset);
    assertThat(go.incompleteTransactionsContain(serverGtid, 1), is(true));
    assertThat(go.incompleteTransactionsContain(serverGtid, 2), is(false));

    assertThat(records.get(1).get("/GTID").getValueAsString(), is(serverGtid));
    assertThat(records.get(1).get("/SeqNo"), is(create(2L)));
    offset = records.get(1).get("/Offset").getValueAsString();
    go = GtidSourceOffset.parse(offset);
    assertThat(go.incompleteTransactionsContain(serverGtid, 1), is(true));
    assertThat(go.incompleteTransactionsContain(serverGtid, 2), is(true));
    assertThat(go.incompleteTransactionsContain(serverGtid, 3), is(false));

    assertThat(records.get(1).get("/Offset").getValueAsString(), is(output.getNewOffset()));

    // should advance gtid
    String nextServerGtid = getNextServerGtid();
    assertThat(nextServerGtid, is(not(serverGtid)));
    execute(ds, Arrays.asList(
        "INSERT INTO foo (bar) VALUES (2)",
        "INSERT INTO foo (bar) VALUES (3)")
    );
    output = runner.runProduce(output.getNewOffset(), MAX_BATCH_SIZE);
    records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, hasSize(2));
    assertThat(records.get(0).get("/GTID").getValueAsString(), is(nextServerGtid));

    offset = records.get(0).get("/Offset").getValueAsString();
    go = GtidSourceOffset.parse(offset);
    // this transaction should have been finished
    assertThat(go.incompleteTransactionsContain(serverGtid, 1), is(false));
    assertThat(go.incompleteTransactionsContain(nextServerGtid, 1), is(true));
    assertThat(go.incompleteTransactionsContain(nextServerGtid, 2), is(false));

    assertThat(records.get(1).get("/Offset").getValueAsString(), is(output.getNewOffset()));
  }

  @Test
  public void shouldSkipIncompleteTransactions() throws Exception {
    MysqlSourceConfig config = createConfig("root");
    MysqlSource source = createMysqlSource(config);
    SourceRunner runner = new SourceRunner.Builder(MysqlDSource.class, source)
        .addOutputLane(LANE)
        .build();
    runner.runInit();

    StageRunner.Output output = runner.runProduce(null, MAX_BATCH_SIZE);
    List<Record> records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, is(Matchers.<Record>empty()));

    // add one more
    execute(ds, Arrays.asList(
        "INSERT INTO foo (bar) VALUES (2)",
        "INSERT INTO foo (bar) VALUES (3)")
    );
    output = runner.runProduce(null, MAX_BATCH_SIZE);
    records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, hasSize(2));

    String offset = records.get(0).get("/Offset").getValueAsString();
    String offset2 = records.get(1).get("/Offset").getValueAsString();

    // reconnect
    runner.runDestroy();
    source = createMysqlSource(config);
    runner = new SourceRunner.Builder(MysqlDSource.class, source)
        .addOutputLane(LANE)
        .build();
    runner.runInit();
    output = runner.runProduce(offset, MAX_BATCH_SIZE);
    records = new ArrayList<>(output.getRecords().get(LANE));
    assertThat(records, hasSize(1));
    assertThat(records.get(0).get("/Offset").getValueAsString(), is(offset2));
    assertThat(records.get(0).get("/SeqNo"), is(create(2L)));
    assertThat(records.get(0).get("/Data/bar"), is(create(3)));
  }

  public String getNextServerGtid() throws Exception {
    String serverUUID = Util.getGlobalVariable(ds, "server_uuid");
    String executed = Util.getServerGtidExecuted(ds);
    GtidSet ex = new GtidSet(executed);
    for (GtidSet.UUIDSet uuidSet : ex.getUUIDSets()) {
      if (uuidSet.getUUID().equals(serverUUID)) {
        List<GtidSet.Interval> intervals = new ArrayList<>(uuidSet.getIntervals());
        GtidSet.Interval last = intervals.get(intervals.size() - 1);
        return String.format("%s:%d", serverUUID, last.getEnd() + 1);
      }
    }
    throw new IllegalStateException("Cannot find last server gtid");
  }

  @Test
  public void shouldStartFromOffset() throws Exception {
    // this event SHOULD NOT be included in offset
    execute(ds, "INSERT INTO foo (bar) VALUES (0)");

    // this event WILL be included in offset (this way mysql master status works)
    execute(ds, "INSERT INTO foo (bar) VALUES (1)");

    String offset = Util.getServerGtidExecuted(ds);

    execute(ds, "INSERT INTO foo (bar) VALUES (2)");

    MysqlSourceConfig config = createConfig("root");
    config.initialOffset = offset;
    MysqlSource source = createMysqlSource(config);
    SourceRunner runner = new SourceRunner.Builder(MysqlDSource.class, source)
        .addOutputLane(LANE)
        .build();
    runner.runInit();

    final String lastSourceOffset = null;
    StageRunner.Output output = runner.runProduce(lastSourceOffset, MAX_BATCH_SIZE);
    List<Record> records = new ArrayList<>();

    while (!output.getRecords().get(LANE).isEmpty()) {
      records.addAll(output.getRecords().get(LANE));
      output = runner.runProduce(output.getNewOffset(), MAX_BATCH_SIZE);
    }
    assertThat(records, hasSize(2));

    // add one more
    execute(ds, "INSERT INTO foo (bar) VALUES (3)");
    output = runner.runProduce(output.getNewOffset(), MAX_BATCH_SIZE);
    records.addAll(output.getRecords().get(LANE));

    assertThat(records, hasSize(3));

    for (Record record : records) {
      if (record.get("/Table").getValueAsString().equals("foo") &&
          record.get("/Data/bar").getValueAsInteger() == 0) {
        fail("Value before start offset found");
      }
    }
  }
}
