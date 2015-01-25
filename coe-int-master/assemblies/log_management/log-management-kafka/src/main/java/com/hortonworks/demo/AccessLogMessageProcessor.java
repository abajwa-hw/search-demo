package com.hortonworks.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.log4j.Logger;

import com.thilinamb.flume.sink.MessagePreprocessor;

/**
 * Hello world!
 * 
 */
public class AccessLogMessageProcessor implements MessagePreprocessor {
	Logger logger = Logger.getLogger(AccessLogMessageProcessor.class);
	public static final String TOPIC = "access_logs";

	public String extractKey(Event event, Context context) {
		String message = new String(event.getBody());
		String key = null;
		try {
			key = message.split(" ")[0];
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.warn("Could not obtain IP Address!");
		}
		return key;
	}

	public String extractTopic(Event event, Context context) {
		return TOPIC;
	}

	public String transformMessage(Event event, Context context) {
		String messageBody = new String(event.getBody());
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/Y:k:m:s");
			Date date = sdf.parse(messageBody.split(" ")[3].replaceAll(
					"\\[|\\]", ""));
			messageBody = date.getTime() + ": " + messageBody;
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.warn("Could not obtain log timestamp!");
		} catch (ParseException ex) {
			logger.warn("Could not parse log timestamp!");
		}
		return messageBody;
	}
}
