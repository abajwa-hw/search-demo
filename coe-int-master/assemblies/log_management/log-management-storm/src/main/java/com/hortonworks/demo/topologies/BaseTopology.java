package com.hortonworks.demo.topologies;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTopology {
	private static final Logger LOG = LoggerFactory
			.getLogger(BaseTopology.class);

	protected Properties topologyConfig;

	public BaseTopology(String configFileLocation) throws Exception {

		topologyConfig = new Properties();
		try {
			topologyConfig.load(new FileInputStream(configFileLocation));
		} catch (FileNotFoundException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		}
	}
}
