package com.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * HTTP server
 * 
 * Note: This version has been extended to support any type of HTTP request. The
 * extension mechanism uses 'configuring server over modification of code'. To achieve this,
 * configure an implementation of the HttpMethodHandler interface. 
 * 
 * I have replaced the implementation of java.util.logging with slf4j.
 * 
 * @author Professor David Bernstein, James Madison University
 * @author Ankith Aiyar
 * 
 */
public class HttpServer {

	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
	
	/**
	 * Configuration parameters for the server. Host, Port and web-root.
	 */
	private static final String WEBSERVER_PROP = "webserver.properties";
	private static final String WEBSERVER_HTTP_HANDLERS = "http_method_handlers.xml";

	private final ExecutorService threadPool;
	private final ServerSocket serverSocket;

	private ServerConfiguration config;

	/**
	 * Default COnstructor
	 */
	public HttpServer() throws IOException {
		// Initialize Server configuration.
		init();

		serverSocket = new ServerSocket(config.getPort());
		
		log.info("Created Server " + config.getHost());
		log.info("Server Socket " + config.getPort());

		threadPool = Executors.newCachedThreadPool();

		serverSocket.setSoTimeout(10000);
	}

	/**
	 * Entry Point
	 * 
	 * @param args
	 *            
	 */
	public static void main(String[] args) {
		HttpServer server = null;

		try {
			server = new HttpServer();
			server.start();
			
		} catch (IOException e) {
			log.error("Failed to create Server "+e);
		} finally {
			if (server != null) {
				server.stopThreadPool();
			}
		}
	}	

	/**
	 * Accepts connection from clients. 
	 */
	public void start() {
		try {
			Socket socket;
			HttpConnectionHandler conn;
						
			while (true) {
				try {
					socket = serverSocket.accept();
					conn = new HttpConnectionHandler(socket, config);
						
					// Add the connection to a BlockingQueue<Runnable> object
					// and, ultimately, call it's run() method in a thread
					// in the pool
					threadPool.submit(conn);
				} catch (SocketTimeoutException ste) {
					// do nothing
				} catch (IOException e) {
					log.warn("Fatal error: "+e.getMessage());
				}
			}
		} finally {
			stopThreadPool();
		}
	}

	/**
	 * Http Server Initialized. Includes reading the configuration to bootstrap
	 * the server alongside reading the http handler xml configuration file as a 
	 * resource.
	 */
	private void init() {
		
		InputStream webserverStream = HttpServer.class.getClassLoader().getResourceAsStream(WEBSERVER_PROP);
		InputStream xmlStream = HttpServer.class.getClassLoader().getResourceAsStream(WEBSERVER_HTTP_HANDLERS);
		
		if (webserverStream != null && xmlStream != null) {
			config = new ServerConfiguration();
			try {
				config.load(webserverStream);
				config.parse(xmlStream);
			} catch (IOException e) {
				log.warn("fatal error "+e.getMessage());
			} catch (SAXException e) {
				log.debug(e.getMessage());
			}			
		} else {
			log.debug("Failed to load the configuration file for this server. A default host and port will be used!");
		}
	}
	
	/**
	 * Stop the threads in the pool
	 */
	private void stopThreadPool() {

		threadPool.shutdown();

		try {
			// Wait for existing connections to complete
			if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();

				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
					log.info("Could not stop thread pool.");
				}
			}
		} catch (InterruptedException ie) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}
