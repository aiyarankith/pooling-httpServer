package com.http;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Properties;

/**
 * An encapsulation of an HTTP response
 *
 * This class extends the HttpMessage class and adds certain featurs: 
 * 		Read status codes from properties files --> statuscode.properties
 *
 * @author Professor David Bernstein, James Madison University
 * @author Ankith Aiyar
 * 
 * @version 0.2
 */
public class HttpResponse extends HttpMessage {

	private int status;
	private NumberFormat nf;
	private final HttpOutputStream outputStream;
	
	private static Properties statusPhrases = new Properties();
	
	static {
        try {
        	statusPhrases.load(HttpResponse.class.getClassLoader().getResourceAsStream("statuscode.properties"));
        } catch (IOException e) {
        	// log error.
        	System.out.println(e.getMessage());
        }
	}
        
	public static final int SC_ACCEPTED = 202;
	public static final int SC_BAD_GATEWAY = 502;
	public static final int SC_BAD_REQUEST = 400;
	public static final int SC_CREATED = 201;
	public static final int SC_FORBIDDEN = 403;
	public static final int SC_INTERNAL_ERROR = 500;
	public static final int SC_MOVED = 301;
	public static final int SC_NO_RESPONSE = 204;
	public static final int SC_NOT_FOUND = 404;
	public static final int SC_NOT_IMPLEMENTED = 501;
	public static final int SC_OK = 200;
	public static final int SC_PARTIAL_INFORMATION = 203;
	public static final int SC_PAYMENT_REQUIRED = 402;
	public static final int SC_SERVICE_OVERLOADED = 503;
	public static final int SC_UNAUTHORIZED = 401;

	// Default constructor
	public HttpResponse(HttpOutputStream outputStream) {
		super();
		nf = NumberFormat.getIntegerInstance();
		status = SC_OK;
		this.outputStream = outputStream;
	}

	/**
	 * 
	 * @return The status associated with HttpResponse
	 */
	public int getStatus() {
		return status;
	}

	/** 
	 * @param sc
	 *       
	 * @return Associated default message with a status code
	 */
	public static String getStatusMessage(int sc) {
		return statusPhrases.getProperty(sc+"", "");
	}

	/**
	 * Send an error response to the client.
	 * 
	 * @param sc
	 */
	public void sendError(int sc) {
		String errorHTML;

		errorHTML = "<HTML><BODY><H1>HTTP Error " + sc + " - " + getStatusMessage(sc) + "</H1></BODY></HTML>\r\n";

		setStatus(sc);
		setContent(errorHTML.getBytes());

		try {
			write();
		} catch (IOException ioe) {
			// Nothing can be done
		}
	}
	
	/** 
	 * @param sc
	 */
	public void setStatus(int sc) {
		status = sc;
	}

	/**
	 * 
	 * @return String representation of the object
	 */
	public String toString() {
		Iterator<String> i;
		String name, s, value;

		s = "Status: \n\t" + status + "\n";
		s += "Headers:\n";
		i = getHeaderNames();
		while (i.hasNext()) {
			name = i.next();
			value = headers.getValue(name);
			s += "\t" + name + "\t" + value + "\n";
		}

		return s;
	}

	/**
	 * Write HttpResponse
	 * 
	 */
	public void write() throws IOException {
		try {
			writeStatusLine();
			writeHeaders();

			if (content != null) {
				outputStream.write(content);
			}
		} finally {
			outputStream.flush();
			outputStream.close();
		}
	}

	/**
	 * Write the headers to outputStream
	 *
	 */
	private void writeHeaders() throws IOException {
		Iterator<String> i;
		String name, value;

		i = headers.getNames();
		while (i.hasNext()) {
			name = i.next();
			value = headers.getValue(name);

			if ((value != null) && (!value.equals(""))) {
				outputStream.printHeaderLine(name, value);
			}
		}
		outputStream.printEOL();
		outputStream.flush();
	}

	/**
	 * Write the status line to outputStream
	 * 
	 */
	private void writeStatusLine() throws IOException {
		
		// checks for http version --> HTTP/1.0, HTTP/1.1
		outputStream.print(HttpConstants.HTTP_VERSION);
		outputStream.print(" ");

		nf.setMaximumIntegerDigits(3);
		nf.setMinimumIntegerDigits(3);
		outputStream.print(nf.format(status));
		outputStream.print(" ");

		outputStream.print(getStatusMessage(status));
		outputStream.printEOL();

		outputStream.flush();
	}
}
