package com.http.methods;

import com.http.HttpRequest;
import com.http.HttpResponse;
import com.http.MIMETyper;
import com.http.server.ServerConfiguration;

public interface HttpMethodHandlerInterface {

	/** 
	 * @param config		The server configuration
	 */
	public void init(ServerConfiguration config);
	
	/**
	 * Handle and service the given HTTP request.
	 * 
	 * @param request		The request to handle.
	 * @param response		The response to send to the client.
	 * @return				true, if this handler can handle the request
	 * 						and false otherwise.
	 */
	public boolean handle(HttpRequest request, HttpResponse response);
}
