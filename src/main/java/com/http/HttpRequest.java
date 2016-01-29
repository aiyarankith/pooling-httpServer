package com.http;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.apache.http.RequestLine;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

/**
 * An encapsulation of an HTTP request
 * 
 * I have modified the implementation to fit in org.apache.http.message.LineParser 
 * to parse the request line of this HttpRequest.
 * 
 * @author Professor David Bernstein, James Madison University
 * @author Ankith Aiyar
 * 
 */
public class HttpRequest extends HttpMessage {
	
	private String method, queryString, version;
	private URI uri;
	
	//Line Parser for http request parsing.
	private final LineParser lineParser;

	private RequestLine reqLine;
	
	private final HttpInputStream inputStream;
	
	// Constructor
	public HttpRequest(HttpInputStream inputStream) throws IOException {
		super();
		this.inputStream = inputStream;
		queryString = null;
		this.lineParser = BasicLineParser.DEFAULT;
		try {
			
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the name of the HTTP method --> GET, POST, or PUT
	 * 
	 * @return HTTP method
	 */
	public String getMethod() {
		return reqLine.getMethod();
	}

	public String getRequestURI() {
		return reqLine.getUri();
	}

	/**
	 * Read this HttpRequest (up to, but not including, the content).
	 * 
	 * @throws IOException 
	 * 
	 */
	public void read() throws IOException {
		String token;

		reqLine = createRequestLine(inputStream.readHttpHeaderLine());

		// Process headers
		headers = NameValueMapper.createNameValueMap();
		headers.putPairs(inputStream, ":");

		// content length		
		int cl = ((token = headers.getValue("Content-Length")) != null) ? Integer.parseInt(token) : -1;
		setContentLength(cl);
	}

	/**
	 * Creates a RequestLine instance for the specified reqLine.
	 * Note: Creation of the instance will fail if the
	 * request line String is not a valid HTTP request-line. 
	 *
	 * @param reqLine
	 * @return
	 */
	private RequestLine createRequestLine(String reqLine) {
		if (reqLine != null) {
			CharArrayBuffer charBuf = new CharArrayBuffer(reqLine.length() - 1);
			charBuf.append(reqLine);
			ParserCursor cursor = new ParserCursor(0, charBuf.length());
			return lineParser.parseRequestLine(charBuf, cursor);
		}
		return null;
	}

	/**
	 * @return The String representation
	 */
	public String toString() {
		Iterator<String> i;
		String name, s, value;

		s = "Method: \n\t" + getMethod() + "\n";
		s += "URI: \n\t" + getRequestURI() + "\n";

		s += "Parameters:\n" + queryString + "\n";

		s += "Headers:\n";
		i = getHeaderNames();
		while (i.hasNext()) {
			name = i.next();
			value = headers.getValue(name);
			s += "\t" + name + "\t" + value + "\n";
		}

		return s;
	}
}
