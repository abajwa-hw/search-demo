package com.hortonworks.demo.topologies.schemes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.solr.client.solrj.beans.Field;

import backtype.storm.tuple.Tuple;

/**
 * @author paul
 * 
 */
public class AccessLogEvent {
	@Field
	String id = new String();
	@Field
	String remoteAddr = new String();
	@Field
	String remoteLogname = new String();
	@Field
	String user = new String();
	@Field
	String time = new String();
	@Field
	String request = new String();
	@Field
	String status = new String();
	@Field
	String bytes_string = new String();
	@Field
	String referrer = new String();
	@Field
	String browser = new String();

	public AccessLogEvent(Tuple input) {
		id = input.getStringByField("rowKey");
		remoteAddr = input.getStringByField("remoteAddr");
		remoteLogname = input.getStringByField("remoteLogname");
		user = input.getStringByField("user");
		time = convertDateForSolr(input.getStringByField("time"));
		request = input.getStringByField("request");
		status = input.getStringByField("status");
		bytes_string = input.getStringByField("bytes_string");
		referrer = input.getStringByField("referrer");
		browser = input.getStringByField("browser");
	}

	public AccessLogEvent(String rowKey, String remoteAddr,
			String remoteLogname, String user, String time, String request,
			String status, String bytes_string, String referrer, String browser) {
		super();
		this.id = rowKey;
		this.remoteAddr = remoteAddr;
		this.remoteLogname = remoteLogname;
		this.user = user;
		this.time = time;
		this.request = request;
		this.status = status;
		this.bytes_string = bytes_string;
		this.referrer = referrer;
		this.browser = browser;
	}

	private String convertDateForSolr(String date) {
		String originalFormat = "d/MMM/Y:k:m:s";
		String targetFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		SimpleDateFormat sdf = new SimpleDateFormat(originalFormat);
		try {
			Date parsedDate = sdf.parse(date);
			sdf = new SimpleDateFormat(targetFormat);
			return sdf.format(parsedDate);
		} catch (ParseException e) {
			return null;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteLogname() {
		return remoteLogname;
	}

	public void setRemoteLogname(String remoteLogname) {
		this.remoteLogname = remoteLogname;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBytes_string() {
		return bytes_string;
	}

	public void setBytes_string(String bytes_string) {
		this.bytes_string = bytes_string;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

}
