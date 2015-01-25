package com.hortonworks.demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		String logEvent = "1388332042000: 172.16.204.101 - - [10/Dec/2014:09:47:22 -0600] \"POST /cgi-bin/rrd.py HTTP/1.1\" 200 100 \"-\" \"Java/1.7.0_67\"";
		String regex = "^(\\S+): (\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\S+) (\\S+) \"([^\"]*)\" \"([^\"]*)\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(logEvent);
		assertTrue(m.matches());
		String remoteAddr = m.group(2);
		String remoteLogname = m.group(3);
		String user = m.group(4);
		String time = m.group(5);
		String request = m.group(6);
		String status = m.group(7);
		String bytesString = m.group(8);
		String referrer = m.group(9);
		String browser = m.group(10);
		assertTrue(remoteAddr.equals("172.16.204.101"));
		assertTrue(browser.equals("Java/1.7.0_67"));
	}
}
