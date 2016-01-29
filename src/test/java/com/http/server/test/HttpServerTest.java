package com.http.server.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import net.iharder.Base64;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.junit.Test;

import com.http.HttpResponse;

public class HttpServerTest extends TestCase {

	private HttpClient client;
	private HostConfiguration hostConf;
	
	private static final String HOST = "localhost";
	private static final int PORT = 1234;
	private static final int CONNECTION_PER_HOST = 50;
	private static final int TOTAL_CONNECTION = 50;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		MultiThreadedHttpConnectionManager connectionMngr = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();		
		
		hostConf = new HostConfiguration();
		hostConf.setHost(HOST, PORT);
		
		params.setMaxConnectionsPerHost(hostConf, CONNECTION_PER_HOST);
		params.setMaxTotalConnections(TOTAL_CONNECTION);
		connectionMngr.setParams(params);
		
		client = new HttpClient(connectionMngr);
		client.setHostConfiguration(hostConf);	
	}

	@Test
	public void testGetExistingRequest() throws Exception {
		String uri = "/sample.txt";
		GetMethod method = new GetMethod(uri);
		try {
			int status = client.executeMethod(method);
			assertEquals(HttpResponse.SC_OK, status);
		} finally {
			method.releaseConnection();
		}

	}

	@Test
	public void testGetWithAuthentication() throws Exception {
		String uri = "/sample.txt";
		DeleteMethod method = new DeleteMethod(uri);
		try {
			// Execute a DELETE request without the Authorization header set.
			int status = client.executeMethod(method);
			assertEquals(HttpResponse.SC_UNAUTHORIZED, status);
			
			String encoding = Base64.encodeBytes("username:password".getBytes());
			method.setRequestHeader(new Header("Authorization", "Basic "+encoding));

			// Execute a DELETE request with the Authorization header set.
			status = client.executeMethod(method);
			assertEquals(HttpResponse.SC_OK, status);
		} finally {
			method.releaseConnection();
		}
	}

	// -----------------------< Concurrent request Test >---
	
	@Test
	public void testConcurrentGetRequests() throws Exception {
		
		// return success.
		ConcurrentGet getTask = new ConcurrentGet("json.json");
		submit(1000, HttpResponse.SC_OK, getTask);
	}

	/**
	 * All executing threads should return a 501 (Not Implemented) status code
	 * because the HttpServer does not yet provide this support.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConcurrentOptionsRequests() throws Exception {
		ConcurrentOptions optionsTask = new ConcurrentOptions("sample.txt");
		submit(500, HttpResponse.SC_NOT_IMPLEMENTED, optionsTask);
	}

	private final class ConcurrentGet implements Callable<Integer> {

		private final String uri;
		ConcurrentGet(String uri) {
			this.uri = uri;
		}
		
 		public Integer call() throws Exception {
			HttpMethodBase method = null;
			try {
				method = new GetMethod(uri);
				int status = client.executeMethod(method);
				return status;
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
	}
	
	/**
	 * Concurrent OPTIONS request execution.
	 *
	 */
	private final class ConcurrentOptions implements Callable<Integer> {

		private final String uri;
		
		ConcurrentOptions(String uri) {
			this.uri = uri;
		}
		
		public Integer call() throws Exception {
			HttpMethodBase method = null;
			try {
				method = new OptionsMethod(uri);
				int status = client.executeMethod(method);
				return status;
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
		
	}
	
	private void submit(int threadCount, int expectedStatus, Callable<Integer> task) throws Exception {		
		List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		List<Future<Integer>> futures = executorService.invokeAll(tasks);

		// # of successfully executed tasks(futures) should match threadCount.
		assertEquals(threadCount, futures.size());

		List<Integer> statusCodes = new ArrayList<Integer>(futures.size());
		for (Future<Integer> future : futures) {
			if (future.isDone()) {
				statusCodes.add(future.get());
			}
		}

		assertEquals(threadCount, statusCodes.size());

		for (int actualStatus : statusCodes) {
			assertEquals(expectedStatus, actualStatus);
		}

	}
}