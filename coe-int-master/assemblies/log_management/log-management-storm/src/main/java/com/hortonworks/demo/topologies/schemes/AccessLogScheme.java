package com.hortonworks.demo.topologies.schemes;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class AccessLogScheme implements Scheme {
	private static final Logger LOG = LoggerFactory
			.getLogger(AccessLogScheme.class);

	public List<Object> deserialize(byte[] bytes) {
		try {
			String logEvent = new String(bytes, "UTF-8");
			LOG.debug(logEvent);
			String regex = "^(\\S+): (\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\S+) (\\S+) \"([^\"]*)\" \"([^\"]*)\"";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(logEvent);
			LOG.debug(Boolean.toString(m.matches()));
			String receivedTime = m.group(1);
			String remoteAddr = m.group(2);
			String remoteLogname = m.group(3);
			String user = m.group(4);
			String time = m.group(5);
			String request = m.group(6);
			String status = m.group(7);
			String bytesString = m.group(8);
			String referrer = m.group(9);
			String browser = m.group(10);
			String rowKey = remoteAddr + ":" + receivedTime;			
 			return new Values(rowKey, remoteAddr, remoteLogname, user, time, request,
					status, bytesString, referrer, browser);
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage());
		} catch (IllegalStateException e) {
			LOG.error("Could not find a match in tuple: " + new String(bytes));
		}
		return null;
	}

	public Fields getOutputFields() {
		return new Fields("rowKey", "remoteAddr", "remoteLogname", "user", "time",
				"request", "status", "bytes_string", "referrer", "browser");
	}

}
