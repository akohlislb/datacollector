/**
 * (c) 2015 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.stage.origin.hdfs.cluster;

import com.streamsets.pipeline.api.ComplexField;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ErrorListener;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Source;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooser;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.config.LogMode;
import com.streamsets.pipeline.config.LogModeChooserValues;
import com.streamsets.pipeline.configurablestage.DClusterSourceOffsetCommitter;
import com.streamsets.pipeline.lib.parser.log.RegExConfig;

import java.util.List;
import java.util.Map;


@StageDef(
    version = 1,
    label = "Hadoop FS",
    description = "Reads data from Hadoop file system",
    execution = ExecutionMode.CLUSTER,
    icon = "hdfs.png",
    privateClassLoader = true
)
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
public class ClusterHdfsDSource extends DClusterSourceOffsetCommitter implements ErrorListener {
  private ClusterHdfsSource clusterHDFSSource;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.STRING,
    label = "Hadoop FS URI",
    description = "Include the HDFS scheme and authority: hdfs://<authority>:<port>. If this is not set, the URI will be set to the value of "
      + "'fs.defaultFS' configuration",
    displayPosition = 10,
    group = "HADOOP_FS")
  public String hdfsUri;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.LIST,
    defaultValue = "[]",
    label = "Directory Path",
    description = "HDFS directory path",
    displayPosition = 10,
    group = "HADOOP_FS"
  )
  public List<String> hdfsDirLocations; // hdfsDirLocation

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    label = "Include All Subdirectories",
    defaultValue = "true",
    description = "Reads all subdirectories within the directory path",
    displayPosition = 20,
    group = "HADOOP_FS"
  )
  public boolean recursive;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.MODEL,
    label = "Data Format",
    description = "Data format",
    displayPosition = 30,
    group = "HADOOP_FS"
   )
  @ValueChooser(DataFormatChooserValues.class)
  public DataFormat dataFormat;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "1024",
      label = "Max Line Length",
      description = "Longer lines are truncated",
      displayPosition = 40,
      group = "TEXT",
      dependsOn = "dataFormat",
      triggeredByValue = "TEXT",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public int textMaxLineLen;


  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "4096",
      label = "Max Object Length (chars)",
      description = "Larger objects are not processed",
      displayPosition = 50,
      group = "JSON",
      dependsOn = "dataFormat",
      triggeredByValue = "JSON",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public int jsonMaxObjectLen;

  // LOG Configuration

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.NUMBER,
    defaultValue = "1024",
    label = "Max Line Length",
    description = "Longer lines are truncated",
    displayPosition = 60,
    group = "LOG",
    dependsOn = "dataFormat",
    triggeredByValue = "LOG",
    min = 1,
    max = Integer.MAX_VALUE
  )
  public int logMaxObjectLen;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.MODEL,
    defaultValue = "COMMON_LOG_FORMAT",
    label = "Log Format",
    description = "Log format. Stack traces are not supported for Log4j",
    displayPosition = 70,
    group = "LOG",
    dependsOn = "dataFormat",
    triggeredByValue = "LOG"
  )
  @ValueChooser(LogModeChooserValues.class)
  public LogMode logMode;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    defaultValue = "false",
    label = "Retain Original Line",
    description = "Indicates if the original line of log should be retained in the record",
    displayPosition = 80,
    group = "LOG",
    dependsOn = "dataFormat",
    triggeredByValue = "LOG"
  )
  public boolean retainOriginalLine;

  //APACHE_CUSTOM_LOG_FORMAT
  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "%h %l %u %t \"%r\" %>s %b",
    label = "Custom Log Format",
    description = "Format built using the apache log format strings.",
    displayPosition = 90,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "APACHE_CUSTOM_LOG_FORMAT"
  )
  public String customLogFormat;

  //REGEX
  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)",
    label = "Regular Expression",
    description = "The regular expression which is used to parse the log line.",
    displayPosition = 100,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "REGEX"
  )
  public String regex;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.MODEL,
    defaultValue = "",
    label = "Field Path To RegEx Group Mapping",
    description = "Map groups in the regular expression to field paths.",
    displayPosition = 110,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "REGEX"
  )
  @ComplexField(RegExConfig.class)
  public List<RegExConfig> fieldPathsToGroupName;

  //GROK
  @ConfigDef(
    required = false,
    type = ConfigDef.Type.TEXT,
    defaultValue = "",
    label = "Grok Pattern Definition",
    description = "Define your own grok patterns which will be used to parse the logs",
    displayPosition = 120,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "GROK",
    mode = ConfigDef.Mode.PLAIN_TEXT
  )
  public String grokPatternDefinition;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "%{COMMONAPACHELOG}",
    label = "Grok Pattern",
    description = "The grok pattern which is used to parse the log line.",
    displayPosition = 130,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "GROK"
  )
  public String grokPattern;

  //LOG4J
  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    defaultValue = "false",
    label = "Use Custom Log Format",
    description = "Select this option to specify your own custom log4j format.",
    displayPosition = 140,
    group = "LOG",
    dependsOn = "logMode",
    triggeredByValue = "LOG4J"
  )
  public boolean enableLog4jCustomLogFormat;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "%r [%t] %-5p %c %x - %m%n",
    label = "Custom Log4J Format",
    description = "Specify your own custom log4j format.",
    displayPosition = 150,
    group = "LOG",
    dependsOn = "enableLog4jCustomLogFormat",
    triggeredByValue = "true"
  )
  public String log4jCustomLogFormat;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    defaultValue = "false",
    label = "Produce Single Record",
    description = "Generates a single record for multiple objects within a message",
    displayPosition = 160,
    group = "HADOOP_FS")
  public boolean produceSingleRecordPerMessage;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    label = "Kerberos Authentication",
    defaultValue = "false",
    description = "",
    displayPosition = 170,
    group = "HADOOP_FS"
  )
  public boolean hdfsKerberos;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "",
    label = "Hadoop Configuration Directory",
    description = "A directory (or symlink) under SDC resources directory to load core-site.xml, hdfs-site.xml, " +
      "yarn-site.xml, and mapred-site.xml files to configure Hadoop.",
    displayPosition = 180,
    group = "HADOOP_FS"
  )
  public String hdfsConfDir;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.STRING,
    label = "HDFS User",
    description = "If set, the data collector will read from HDFS as this user. " +
      "The data collector user must be configured as a proxy user in HDFS.",
    displayPosition = 190,
    group = "HADOOP_FS"
  )
   public String hdfsUser;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.MAP,
    label = "Hadoop FS Configuration",
    description = "Additional Hadoop properties to pass to the underlying Hadoop FileSystem",
    displayPosition = 200,
    group = "HADOOP_FS"
  )
  public Map<String, String> hdfsConfigs;

  @Override
  protected Source createSource() {
     clusterHDFSSource = new ClusterHdfsSource(hdfsUri, hdfsDirLocations, recursive, hdfsConfigs, dataFormat,
      textMaxLineLen, jsonMaxObjectLen, logMode, retainOriginalLine, customLogFormat, regex, fieldPathsToGroupName,
       grokPatternDefinition, grokPattern, enableLog4jCustomLogFormat, log4jCustomLogFormat, logMaxObjectLen,
       produceSingleRecordPerMessage, hdfsKerberos, hdfsUser, hdfsConfDir);
     return clusterHDFSSource;
  }

  @Override
  public Source getSource() {
    return clusterHDFSSource;
  }

  @Override
  public void errorNotification(Throwable throwable) {
    ClusterHdfsSource source = this.clusterHDFSSource;
    if (source != null) {
      source.errorNotification(throwable);
    }
  }

  @Override
  public void shutdown() {
    ClusterHdfsSource source = this.clusterHDFSSource;
    if (source != null) {
      source.shutdown();
    }
  }
}
