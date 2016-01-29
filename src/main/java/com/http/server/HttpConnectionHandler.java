package com.http.server;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.http.HttpInputStream;
import com.http.HttpOutputStream;
import com.http.HttpRequest;
import com.http.HttpResponse;
import com.http.methods.HttpMethodHandlerInterface;

/**
 * Handle an HTTP 1.1 connection in a new thread of execution
 * 
 * The updated version includes a pluggable support for http request handling.
 * Hence this server would be able to handle any http requests by just configuring 
 * the server properties.
 * 
 * @author Professor David Bernstein, James Madison University
 * @author Ankith Aiyar
 * 
 */
class HttpConnectionHandler implements Runnable {

	private Socket socket;
	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
	private final ServerConfiguration config;
	
	private HttpRequest httpRequest;
	private HttpResponse httpResponse;
	
	/**	 
	 * @param
	 *            The TCP socket for the connection
	 * @param
	 * 			  The Server's configuration
	 */
	public HttpConnectionHandler(Socket s, ServerConfiguration config) {
		socket = s;
		this.config = config;
	}

	/**
	 * The entry point for the thread
	 */
	public void run() {		

		String method;

		try {
			HttpRequest request = getRequest();
			HttpResponse response = getResponse();

			try {
				// Read request
				request.read();

				// Determine the method to use
				method = request.getMethod();

				// Respond to the request
				if ((request == null) || (method == null)) {
					response.sendError(HttpResponse.SC_BAD_REQUEST);
				} else {
					handle(request, response);
				}
			} catch (Exception e) {
				response.sendError(HttpResponse.SC_INTERNAL_ERROR);
			}
		} catch (IOException e) {
			log.error("Failed to get request and response. Socket closing.");
		} finally {
			//close();
		}
	}
	
	public HttpRequest getRequest() throws IOException {
		if (httpRequest == null) {
			httpRequest = new HttpRequest(getInputStream());
		}
		return httpRequest;
	}
	
	public HttpResponse getResponse() throws IOException {
		if (httpResponse == null) {
			httpResponse = new HttpResponse(getOutputStream());
		}
		return httpResponse;
	}
	
	private HttpInputStream getInputStream() throws IOException {
		return new HttpInputStream(socket.getInputStream());
	}
	
	private HttpOutputStream getOutputStream() throws IOException {
		return new HttpOutputStream(socket.getOutputStream());
	}
	/**
	 * Handles the HTTP request. 
	 */
	private void handle(HttpRequest request, HttpResponse response) {
		for (HttpMethodHandlerInterface handler : config.getHandlers()) {
			if (handler.handle(request, response)) {
				return;
			}
		} 
		response.sendError(HttpResponse.SC_NOT_IMPLEMENTED);
	}
	/**
	 * HttpConnection keep-alive header.
	 * 
	 * This method is used for server persistent behavior.
	 * 
	 */
	public boolean keepAlive() throws IOException {
		String keepAlive = getRequest().getHeader("Connection");
		return (keepAlive == null) ? false : keepAlive.equalsIgnoreCase(keepAlive);
	}
		
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			log.error("Error while closing the socket connection");
		}
		
	}
}
