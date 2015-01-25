package com.hortonworks.demo.bolts;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hortonworks.demo.topologies.schemes.AccessLogEvent;

public class SolrBolt implements IRichBolt {
	private static final Logger LOG = LoggerFactory.getLogger(SolrBolt.class);
	private OutputCollector collector;
	private Properties config;
	private SolrServer server = null;
	private String solrUrl;

	public SolrBolt(Properties config) {
		this.config = config;
	}

	public void cleanup() {
		// TODO Auto-generated method stub
	}

	public void execute(Tuple input) {
		AccessLogEvent event = new AccessLogEvent(input);
		index(event);
		collector.ack(input);
	}

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;

		this.solrUrl = config.getProperty("solr.server.url");
		server = new HttpSolrServer(solrUrl);
		(new Thread(new CommitThread(server))).start();
	}

	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
	}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void index(AccessLogEvent accessLogEvent) {
		try {
			LOG.info("Starting process to solr index document["+accessLogEvent+"]");
			UpdateResponse response = server.addBean(accessLogEvent);
			LOG.info("Indexed document with id: " + accessLogEvent.getId()
					+ " status: " + response.getStatus());
		} catch (IOException e) {
			LOG.error("Could not index document: " + accessLogEvent.getId()
					+ " " + e.getMessage());
			e.printStackTrace();
		} catch (SolrServerException e) {
			LOG.error("Could not index document: " + accessLogEvent.getId()
					+ " " + e.getMessage());
			e.printStackTrace();
		}
	}

	class CommitThread implements Runnable {
		SolrServer server;

		public CommitThread(SolrServer server) {
			this.server = server;
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(15000);
					server.commit();
					LOG.info("Committing Index");
				} catch (InterruptedException e) {
					LOG.error("Interrupted: " + e.getMessage());
					e.printStackTrace();
				} catch (SolrServerException e) {
					LOG.error("Error committing: " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					LOG.error("Error committing: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}
