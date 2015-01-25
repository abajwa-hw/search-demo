package com.hortonworks.demo.bolts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class SimpleAccessLogRuleBolt implements IRichBolt {
	private static final Logger LOG = LoggerFactory
			.getLogger(SimpleAccessLogRuleBolt.class);
	private OutputCollector collector;

	public void cleanup() {
	}

	public void execute(Tuple input) {
		String status = input.getStringByField("status");
		String request = input.getStringByField("request");
		LOG.debug("Status: " + status);
		if (status.equals("404")) {
			LOG.debug("Emitting new alert for: " + input.getMessageId());
			List<Object> alertTuple = new ArrayList<Object>();
			alertTuple.add("404");
			alertTuple.add(request);
			collector.emit(input, alertTuple);
		}
		collector.ack(input);
	}

	public void prepare(Map conf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	public void declareOutputFields(OutputFieldsDeclarer declaror) {
		declaror.declare(new Fields("key", "message"));
	}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
