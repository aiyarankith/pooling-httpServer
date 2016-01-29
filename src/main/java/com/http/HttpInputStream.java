package com.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A Wrapper class for a java.io.InputStream. 
 * 
 * @author Ankith Aiyar.
 *
 */
public class HttpInputStream {

	private final BufferedReader bufferedReader;
	
	public HttpInputStream(InputStream inputStream) {
		this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	public String readHttpHeaderLine() throws IOException {
		return bufferedReader.readLine();
	}

	public boolean ready() throws IOException {
		return bufferedReader.ready();
	}
}
