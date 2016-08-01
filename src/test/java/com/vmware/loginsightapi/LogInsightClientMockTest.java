/**
 * Copyright © 2016 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 * Some files may be comprised of various open source software components, each of which
 * has its own license that is located in the source code of the respective component.
 */
package com.vmware.loginsightapi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.loginsightapi.core.MessageQueryResponse;

@RunWith(MockitoJUnitRunner.class)
public class LogInsightClientMockTest {
	
	private LogInsightClient client;
	private final static Logger logger = LoggerFactory.getLogger(LogInsightClientMockTest.class);
	
	private final static String SERVER_RESPONSE_EXPECTED = "{\"userId\":\"7506ecf5-cd7a-4ae3-88b7-f72fc1955c73\",\"sessionId\":\"qyOLWEe7f/GjdM1WnczrCeQure97B/NpTbWTeqqYPBd1AYMf9cMNfQYqltITI4ffPMx822Sz9i/X47t8VwsDb0oGckclJUdn83cyIPk6WlsOpI4Yjw6WpurAnv9RhDsYSzKhAMzskzhTOJKfDHZjWR5v576WwtJA71wqI7igFrG91LG5c/3GfzMb68sUHF6hV+meYtGS4A1y/lUItvfkqTTAxBtTCZNoKrvCJZ4R+b6vuAAYoBNSWL7ycIy2LsALrVFxftAkA8n9DBAZYA9T5A==\",\"ttl\":1800}";
	@Mock
	private LogInsightConnectionStrategy<CloseableHttpAsyncClient> connectionStrategy;
	@Mock
	private CloseableHttpAsyncClient asyncHttpClient;
	String host;
	String user;
	String password;

	@Before
	public void setUp() {
		//Load the properties file for loginsight connection.
		Properties connectionConfig = new Properties();
		try {
			connectionConfig.load(getClass().getResourceAsStream("/config.properties"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		host = connectionConfig.getProperty(LogInsightConnectionConfig.LOGINSIGHT_HOST);
		user = connectionConfig.getProperty(LogInsightConnectionConfig.LOGINSIGHT_USER);
		password = connectionConfig.getProperty(LogInsightConnectionConfig.LOGINSIGHT_PASSWORD);
		if (StringUtils.isEmpty(password)) {
			password = System.getenv(LogInsightConnectionConfig.LOGINSIGHT_PASSWORD);
		}
		when(connectionStrategy.getHttpClient()).thenReturn(asyncHttpClient);
		client = new LogInsightClient(connectionStrategy, connectionConfig);
	}
	
	@Test
	public void testConnect() {
		HttpResponse response = mock(HttpResponse.class);
		Future<HttpResponse> future = ConcurrentUtils.constantFuture(response);
		when(asyncHttpClient.execute(any(HttpUriRequest.class), any(FutureCallback.class))).thenReturn(future, null);
		HttpEntity httpEntity = mock(HttpEntity.class);
		when(response.getEntity()).thenReturn(httpEntity);
		StatusLine statusLine = mock(StatusLine.class);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);
		try {
			InputStream inputStream = IOUtils.toInputStream(SERVER_RESPONSE_EXPECTED, "UTF-8");
			when(httpEntity.getContent()).thenReturn(inputStream);			
			client.connect(user, password);
			assertEquals("Invalid session id!!", "qyOLWEe7f/GjdM1WnczrCeQure97B/NpTbWTeqqYPBd1AYMf9cMNfQYqltITI4ffPMx822Sz9i/X47t8VwsDb0oGckclJUdn83cyIPk6WlsOpI4Yjw6WpurAnv9RhDsYSzKhAMzskzhTOJKfDHZjWR5v576WwtJA71wqI7igFrG91LG5c/3GfzMb68sUHF6hV+meYtGS4A1y/lUItvfkqTTAxBtTCZNoKrvCJZ4R+b6vuAAYoBNSWL7ycIy2LsALrVFxftAkA8n9DBAZYA9T5A==", client.getSessionId());
		} catch (Exception e) {
			System.out.println("Exception raised " + ExceptionUtils.getStackTrace(e));
		}
	}
	
	@Test
	public void testMessageQuery() {
		long startTime = System.nanoTime();
		List<FieldConstraint> constraints = RequestBuilders.constraint().eq("vclap_caseid", "1423244")
				.gt("timestamp", "0").build();
		MessageQueryBuilder mqb = (MessageQueryBuilder) RequestBuilders.messageQuery().limit(100)
				.setConstraints(constraints);
		HttpResponse response = mock(HttpResponse.class);
		Future<HttpResponse> future = ConcurrentUtils.constantFuture(response);
		HttpGet request = mock(HttpGet.class);
		Header [] contentTypeHeaders = new Header[1];
		contentTypeHeaders[0] = new Header() {

			@Override
			public String getName() {
				return "Content-Type";
			}

			@Override
			public String getValue() {
				return "application/json";
			}

			@Override
			public HeaderElement[] getElements() throws ParseException {
				return null;
			}
			
		};
		when(request.getHeaders("Content-Type")).thenReturn(contentTypeHeaders);
		Assert.assertNotNull(request.);
		when(asyncHttpClient.execute(any(HttpUriRequest.class), any(FutureCallback.class))).thenReturn(future, null);
		
		MessageQueryResponse messages = client.messageQuery(mqb.toUrlString());
		Assert.assertTrue("Invalid number of messages", messages.getEvents().size() <= 100);
		logger.debug("Returned " + messages.getEvents().size() + " messages");
		long duration = System.nanoTime() - startTime;
		logger.info("duration=" + duration);
	}



}