SET solr.collection 'access_logs';
SET hbase.zookeeper.quorum 'bimota.hortonworks.local'; 

REGISTER /home/paul/hadoop-lws-job-1.2.0-0-0.jar;
REGISTER /home/paul/datafu-1.2.0.jar;
REGISTER /usr/lib/pig/lib/piggybank.jar;
REGISTER /usr/lib/hbase/lib/*.jar;

DEFINE EXTRACT org.apache.pig.piggybank.evaluation.string.RegexExtractAll;
DEFINE CustomFormatToISO org.apache.pig.piggybank.evaluation.datetime.convert.CustomFormatToISO();
DEFINE MD5 datafu.pig.hash.MD5();

RAW_LOGS = LOAD '/user/paul/data/apache/access' USING TextLoader as (line:chararray);

ACCESS_LOGS = FOREACH RAW_LOGS GENERATE MD5(line) as (id:chararray), 
    FLATTEN( 
      EXTRACT(line, '^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] "(.+?)" (\\S+) (\\S+) "([^"]*)" "([^"]*)"')
    ) 
    as (
      remoteAddr:    chararray, 
      remoteLogname: chararray, 
      user:          chararray, 
      time:          chararray, 
      request:       chararray,
      status:        chararray, 
      bytes_string:  chararray, 
      referrer:      chararray, 
      browser:       chararray
  );
ACCESS_LOGS_CLEAN = FILTER ACCESS_LOGS by time is not null;
STORE ACCESS_LOGS_CLEAN INTO 'hbase://access_logs' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage ('request:remoteAddr request:remoteLogname request:user request:time request:request request:status request:bytes_string request:referrer request:browser');

ACCESS_LOGS_SOLR = FOREACH ACCESS_LOGS_CLEAN GENERATE id, 'remoteAddr', remoteAddr, 'remoteLogname', remoteLogname, 'time', CustomFormatToISO(time, 'dd/MMM/yyyy:HH:mm:ss Z') as time, 'request', request, 'status', status, 'bytes_string', bytes_string, 'referrer', referrer, 'browser', browser;
STORE ACCESS_LOGS_SOLR into 'http://bimota.hortonworks.local:8983/solr/' using com.lucidworks.hadoop.pig.SolrStoreFunc();
