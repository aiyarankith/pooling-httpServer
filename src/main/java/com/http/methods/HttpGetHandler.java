package com.http.methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;

import ch.qos.logback.classic.Logger;

import com.http.HttpConstants;
import com.http.HttpRequest;
import com.http.HttpResponse;
import com.http.MIMETyper;
import com.http.server.ServerConfiguration;

/**
 *  This class simply implements the GET request of the http server.
 *  It reads the url and returns the file present at the particular url.
 *  
 */
public class HttpGetHandler implements HttpMethodHandlerInterface {

	private ServerConfiguration config;
	
	private static final int EOF = -1;
	/**
	 * @see HttpMethodHandlerInterface#init(ServerConfiguration)
	 */
	public void init(ServerConfiguration config) {		
		this.config = config;
	}
	
	/**
	 * @see HttpMethodHandlerInterface#handle(HttpRequest, HttpResponse)
	 */
	public boolean handle(HttpRequest request, HttpResponse response) {
		if (canHandle(request)) {
			handleGetReq(request, response);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks to see if this server can handle the request.
	 * 
	 * @param request
	 * 
	 * @return GET --> true, Not GET --> false
	 */
	private boolean canHandle(HttpRequest request) {
		return request.getMethod().equals(HttpConstants.METHOD_GET);
	}
	
	private void handleGetReq(HttpRequest request, HttpResponse response) {
		MIMETyper mt = MIMETyper.createInstance();
		
		FileInputStream fis = null;
		File file = new File(config.getWebRoot(), request.getRequestURI());
		
		int size = (int) file.length();		
		
		byte[] b = new byte[size];
		
		if (file.exists()) {
			try {
				fis = new FileInputStream(file);
				System.out.println(mt.getContentTypeFor(file.getName()));
				response.setContentType(mt.getContentTypeFor(file.getName()));
				response.setContentLength(size);
				int ch = fis.read(b, 0, b.length);
				while (ch != EOF) {
					response.setContent(b);
					ch = fis.read(b, 0, b.length);
				}				
				
				response.write();
			} catch (FileNotFoundException e) {
				response.sendError(HttpResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				response.sendError(HttpResponse.SC_INTERNAL_ERROR);
			}
		} else {
			// file does not exist
			response.sendError(HttpResponse.SC_NOT_FOUND);
		}
	}	

}
