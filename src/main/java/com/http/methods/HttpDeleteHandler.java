package com.http.methods;

import java.io.IOException;
import java.util.Iterator;

import com.http.HttpConstants;
import com.http.HttpRequest;
import com.http.HttpResponse;
import com.http.server.ServerConfiguration;

/**
 *  A DELETE request handler. This implementation will do a check on 
 *  the request to test if the authorization header field is set. 
 *  Otherwise deleting the resource will fail.
 *  
 */
public class HttpDeleteHandler implements HttpMethodHandlerInterface {

	private ServerConfiguration config;
	
	public void init(ServerConfiguration config) {
		this.config = config;
	}

	/**
	 * @see HttpMethodHandlerInterface#handle(HttpRequest, HttpResponse)
	 */
	public boolean handle(HttpRequest request, HttpResponse response) {
		if (canHandle(request)) {
			if (isAuthorizationPresent(request)) {
				handleDeleteReq(request, response);
			} else {
				response.sendError(HttpResponse.SC_UNAUTHORIZED);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the authorization header is present in the request.
	 * 
	 * @param request 
	 * @return            True, if the authorization header is present
	 * 				      in the request and false otherwise.
	 */
	private boolean isAuthorizationPresent(HttpRequest request) {
		Iterator<String> it = request.getHeaderNames();
		while (it.hasNext()) {
			String headerName = it.next();
			if (headerName.equals("Authorization")) {				
				return true;
			}
		}
		return false;
	}
	
	public void handleDeleteReq(HttpRequest request, HttpResponse response) {
		// TODO: delete the resource
		
		response.setStatus(HttpResponse.SC_OK);
		try {
			response.write();
		} catch (IOException e) {
			response.sendError(HttpResponse.SC_INTERNAL_ERROR);
		}
		
	}
	
	public boolean canHandle(HttpRequest request) {
		return request.getMethod().equals(HttpConstants.METHOD_DELETE);
	}
	
}
