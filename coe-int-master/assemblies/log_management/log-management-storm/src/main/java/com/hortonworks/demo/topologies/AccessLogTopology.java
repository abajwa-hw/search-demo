package com.hortonworks.demo.topologies;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.TimedRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hdfs.common.rotation.MoveFileAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.bolt.selector.DefaultTopicSelector;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import com.hortonworks.demo.bolts.SimpleAccessLogRuleBolt;
import com.hortonworks.demo.bolts.SolrBolt;
import com.hortonworks.demo.topologies.schemes.AccessLogScheme;

public class AccessLogTopology extends BaseTopology {
	private static final String KAFKA_SPOUT = "kafkaSpout";
	private static final String HDFS_BOLT = "hdfsBolt";
	private static final String HBASE_BOLT = "hbaseBolt";

	private static final Logger LOG = LoggerFactory
			.getLogger(AccessLogTopology.class);

	/**
	 * @param configFileLocation
	 * @throws Exception
	 */
	public AccessLogTopology(String configFileLocation) throws Exception {
		super(configFileLocation);
	}

	public static void main(String[] args) throws Exception {
		String configFileLocation = args[0];
		AccessLogTopology topology = new AccessLogTopology(configFileLocation);
		topology.buildAndSubmit();
	}

	/**
	 * @throws Exception
	 */
	private void buildAndSubmit() throws Exception {
		/*
		 * This conf is for Storm and it needs be configured with things like
		 * the following: Zookeeper server, nimbus server, ports, etc... All of
		 * this configuration will be picked up in the ~/.storm/storm.yaml file
		 * that will be located on each storm node.
		 */
		Config conf = new Config();
		conf.setDebug(true);

		TopologyBuilder builder = new TopologyBuilder();

		/* Setup a Kafka Spout to ingest events from */
		configureKafkaSpout(builder);

		/* Setup an HDFSBolt to send every parsed event to HDFS */
		configureHDFSBolt(builder);

		/* Setup an HBase Bolt to send every parsed event to an HBase table */
		configureHBaseBolt(builder, conf);

		/* Setup a custom Solr Bolt to index every parsed event to a Solr Core */
		configureSolrIndexingBolt(builder);

		configureSimpleRuleBolt(builder, conf);

		/*
		 * Set the number of workers that will be spun up for this topology.
		 * Each worker represents a JVM where executor thread will be spawned
		 * from
		 */
		Integer topologyWorkers = Integer.valueOf(topologyConfig
				.getProperty("storm.topology.workers"));
		conf.put(Config.TOPOLOGY_WORKERS, topologyWorkers);

		try {
			StormSubmitter.submitTopology("access-logs-processor", conf,
					builder.createTopology());
		} catch (Exception e) {
			LOG.error("Error submiting Topology", e);
		}

	}

	/**
	 * @param builder
	 * @return
	 */
	private int configureKafkaSpout(TopologyBuilder builder) {
		KafkaSpout kafkaSpout = constructKafkaSpout();

		int spoutCount = Integer.valueOf(topologyConfig
				.getProperty("spout.thread.count"));
		int boltCount = Integer.valueOf(topologyConfig
				.getProperty("bolt.thread.count"));

		builder.setSpout(KAFKA_SPOUT, kafkaSpout, spoutCount);
		return boltCount;
	}

	/**
	 * @param builder
	 */
	private void configureHDFSBolt(TopologyBuilder builder) {
		String rootPath = topologyConfig.getProperty("hdfs.path");
		String prefix = topologyConfig.getProperty("hdfs.file.prefix");
		String fsUrl = topologyConfig.getProperty("hdfs.url");

		Float rotationTimeInMinutes = Float.valueOf(topologyConfig
				.getProperty("hdfs.file.rotation.time.minutes"));

		RecordFormat format = new DelimitedRecordFormat()
				.withFieldDelimiter("|");

		// Synchronize data buffer with the filesystem every 1000 tuples
		SyncPolicy syncPolicy = new CountSyncPolicy(1000);

		// Rotate data files when they reach five MB
		// FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f,
		// Units.MB);

		// Rotate every X minutes
		FileRotationPolicy rotationPolicy = new TimedRotationPolicy(
				rotationTimeInMinutes, TimedRotationPolicy.TimeUnit.MINUTES);

		FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath(
				rootPath + "/staging").withPrefix(prefix);

		MoveFileAction moveFileAction = new MoveFileAction()
				.toDestination(rootPath + "/raw");

		// Instantiate the HdfsBolt
		HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(fsUrl)
				.withFileNameFormat(fileNameFormat).withRecordFormat(format)
				.withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy)
				.addRotationAction(moveFileAction);

		int hdfsBoltCount = Integer.valueOf(topologyConfig
				.getProperty("hdfsbolt.thread.count"));
		builder.setBolt(HDFS_BOLT, hdfsBolt, hdfsBoltCount).shuffleGrouping(
				KAFKA_SPOUT);
	}

	/**
	 * @param builder
	 */
	private void configureHBaseBolt(TopologyBuilder builder, Config config) {
		Map<String, Object> hbaseConf = new HashMap<String, Object>();
		hbaseConf.put("hbase.rootdir",
				topologyConfig.getProperty("hbase.rootdir"));
		config.put("hbase.conf", hbaseConf);
		SimpleHBaseMapper mapper = new SimpleHBaseMapper()
				.withRowKeyField("rowKey")
				.withColumnFields(
						new Fields("remoteAddr", "user", "browser",
								"remoteLogname", "time", "request", "status",
								"bytes_string", "referrer"))
				.withColumnFamily("request");

		HBaseBolt hbase = new HBaseBolt("access_logs", mapper)
				.withConfigKey("hbase.conf");
		builder.setBolt(HBASE_BOLT, hbase, 1).shuffleGrouping(KAFKA_SPOUT);
	}

	private void configureSolrIndexingBolt(TopologyBuilder builder) {
		boolean isIndexingEnabled = Boolean.valueOf(
				topologyConfig.getProperty("solr.index.enable")).booleanValue();
		if (isIndexingEnabled) {
			LOG.info("Solr indexing enabled");
			int solrBoltCount = Integer.valueOf(topologyConfig
					.getProperty("solr.bolt.thread.count"));
			SolrBolt solrBolt = new SolrBolt(topologyConfig);
			builder.setBolt("solr_indexer_bolt", solrBolt, solrBoltCount)
					.shuffleGrouping(KAFKA_SPOUT);
		} else {
			LOG.info("Solr indexing turned off");
		}
	}

	/**
	 * Construct the KafkaSpout which comes from the jar storm-kafka-0.8-plus
	 * 
	 * @return
	 */
	private KafkaSpout constructKafkaSpout() {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		return kafkaSpout;
	}

	/**
	 * Construct
	 * 
	 * @return
	 */
	private SpoutConfig constructKafkaSpoutConf() {
		BrokerHosts hosts = new ZkHosts(
				topologyConfig.getProperty("kafka.zookeeper.host.port"));
		String topic = topologyConfig.getProperty("kafka.topic");
		String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
		String consumerGroupId = topologyConfig
				.getProperty("kafka.consumer.group.id");

		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot,
				consumerGroupId);

		/*
		 * Custom TruckScheme that will take Kafka message of single truckEvent
		 * and emit a 2-tuple consisting of truckId and truckEvent. This
		 * driverId is required to do a fieldsSorting so that all driver events
		 * are sent to the set of bolts
		 */
		spoutConfig.scheme = new SchemeAsMultiScheme(new AccessLogScheme());

		return spoutConfig;
	}

	private void configureSimpleRuleBolt(TopologyBuilder builder, Config config) {
		Map<String, Object> brokerConf = new HashMap<String, Object>();
		brokerConf.put("metadata.broker.list",
				topologyConfig.getProperty("kafka.bolt.broker-list"));
		brokerConf.put("request.required.acks", "1");
		brokerConf.put("serializer.class", "kafka.serializer.StringEncoder");
		config.put("kafka.broker.properties", brokerConf);

		int ruleBoltCount = Integer.valueOf(topologyConfig
				.getProperty("kafka.bolt.thread.count"));
		SimpleAccessLogRuleBolt ruleBolt = new SimpleAccessLogRuleBolt();
		builder.setBolt("rule_bolt", ruleBolt, ruleBoltCount).shuffleGrouping(
				KAFKA_SPOUT);
		KafkaBolt kafkaBolt = new KafkaBolt().withTopicSelector(
				new DefaultTopicSelector(topologyConfig
						.getProperty("kafka.bolt.topic")))
				.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());
		builder.setBolt("kafka_alert_bolt", kafkaBolt).shuffleGrouping(
				"rule_bolt");
	}
}
