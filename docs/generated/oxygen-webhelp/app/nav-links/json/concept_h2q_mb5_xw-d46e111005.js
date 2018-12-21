define({"topics" : [{"title":"Pipeline Execution Mode","shortdesc":"\n               <p class=\"shortdesc\">Pipelines can run in standalone, cluster, or edge execution mode. Some pipeline         execution modes do not support all\n                  statistics aggregator options.\n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_pyb_2fc_jcb","attributes": {"data-id":"concept_pyb_2fc_jcb",},"menu": {"hasChildren":false,},"tocID":"concept_pyb_2fc_jcb-d46e111210","topics":[]},{"title":"Write Statistics Directly to <span class=\"ph\">Control Hub</span>","shortdesc":"\n               <p class=\"shortdesc\">When you write statistics directly to <span class=\"ph\">Control Hub</span>, <span class=\"ph\">Control Hub</span> does         not generate a system pipeline for the job. Instead, the <span class=\"ph\">Data Collector</span> or <span class=\"ph\">SDC Edge</span>         directly sends the statistics to <span class=\"ph\">Control Hub</span>.\n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_abc_1w1_c1b","attributes": {"data-id":"concept_abc_1w1_c1b",},"menu": {"hasChildren":false,},"tocID":"concept_abc_1w1_c1b-d46e111235","topics":[]},{"title":"Write Statistics to SDC RPC","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_c53_pzp_yy","attributes": {"data-id":"concept_c53_pzp_yy",},"menu": {"hasChildren":true,},"tocID":"concept_c53_pzp_yy-d46e111287","next":"concept_c53_pzp_yy-d46e111287",},{"title":"Write Statistics to Kafka","shortdesc":"\n               <p class=\"shortdesc\">When you write statistics to a Kafka cluster, <span class=\"ph\">Data Collector</span>         effectively adds a Kafka Producer destination to the pipeline that you are configuring. <span class=\"ph\">Control Hub</span>         automatically generates and runs a system pipeline for the job. The system pipeline reads         the statistics\n                  from Kafka, and then aggregates and sends the statistics to <span class=\"ph\">Control Hub</span>. \n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_wmv_cbb_fx","attributes": {"data-id":"concept_wmv_cbb_fx",},"menu": {"hasChildren":true,},"tocID":"concept_wmv_cbb_fx-d46e111381","next":"concept_wmv_cbb_fx-d46e111381",},{"title":"Write Statistics to Kinesis Streams","shortdesc":"\n               <p class=\"shortdesc\"> When you write statistics to Amazon Kinesis Streams, <span class=\"ph\">Data Collector</span>         effectively adds a Kinesis Producer destination to the pipeline that you are configuring.             <span class=\"ph\">Control Hub</span>         automatically generates and runs a system pipeline for the job. The system pipeline reads         the statistics\n                  from Kinesis Streams, and then aggregates and sends the statistics to <span class=\"ph\">Control Hub</span>. \n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_em4_2bb_fx","attributes": {"data-id":"concept_em4_2bb_fx",},"menu": {"hasChildren":true,},"tocID":"concept_em4_2bb_fx-d46e111711","next":"concept_em4_2bb_fx-d46e111711",},{"title":"Write Statistics to MapR Streams","shortdesc":"\n               <p class=\"shortdesc\">When you write statistics to MapR Streams, <span class=\"ph\">Data Collector</span>         effectively adds a MapR Streams Producer destination to the pipeline that you are         configuring. <span class=\"ph\">Control Hub</span>         automatically generates and runs a system pipeline for the job. The system pipeline reads         the statistics\n                  from MapR Streams, and then aggregates and sends the statistics to <span class=\"ph\">Control Hub</span>. \n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#concept_qh5_v5t_mbb","attributes": {"data-id":"concept_qh5_v5t_mbb",},"menu": {"hasChildren":true,},"tocID":"concept_qh5_v5t_mbb-d46e112065","next":"concept_qh5_v5t_mbb-d46e112065",},{"title":"Configuring a Pipeline to Write Statistics","shortdesc":"\n               <p class=\"shortdesc\">You can configure a pipeline to write statistics<span class=\"ph\"> after the <span class=\"ph\">Data Collector</span>             has been registered with <span class=\"ph\">Control Hub</span></span>.\n               </p>\n            ","href":"datacollector\/UserGuide\/DPM\/AggregatedStatistics.html#task_lcd_ng5_xw","attributes": {"data-id":"task_lcd_ng5_xw",},"menu": {"hasChildren":false,},"tocID":"task_lcd_ng5_xw-d46e112509","topics":[]}]});