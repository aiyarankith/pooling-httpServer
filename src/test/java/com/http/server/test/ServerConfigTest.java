package com.http.server.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.http.methods.HttpMethodHandlerInterface;
import com.http.server.ServerConfiguration;

public class ServerConfigTest extends TestCase {

    private static final String SINGLE_HANDLER_XML_CONFIG = 
    		"<config>\n" +            
            "    <supportedmethods>\n"+
            "        <supportedmethod>\n" +
            "            <class name=\"com.http.methods.HttpGetHandler\" />\n" +
            "        </supportedmethod>\n" +
            "    </supportedmethods>" +
            "</config>";
    
    private static final String MALFORMED_XML_CONFIG = 
    		"<config>\n" +            
            "    <supportedmethods>\n"+
            "        <supportedmethod>\n" +
            "            <class name=\"com.http.methods.HttpDeleteHandler\" />\n" +
            "        </supportedmethods>\n" +
            "    </supportedmethods>" +
            "";
    
    private ServerConfiguration config = null;
    
    @Override
    public void setUp() throws Exception {
    	config = new ServerConfiguration();
    }
    
    @Test
    public void testSingleHandlerConfiguration() throws Exception {   	 
    	InputStream is = new ByteArrayInputStream(SINGLE_HANDLER_XML_CONFIG.getBytes("UTF-8"));
    	List<HttpMethodHandlerInterface> handlers = config.getHandlers();

    	config.parse(is);    	     	 

    	assertNotNull(handlers);    	 
    	assertEquals(1, handlers.size());
    	assertEquals("com.http.methods.HttpGetHandler", handlers.get(0).getClass().getName());      	    	
    }
    
    @Test
    public void testMalformConfiguration() throws Exception {
    	try {
        	InputStream is = new ByteArrayInputStream(MALFORMED_XML_CONFIG.getBytes("UTF-8"));
        	config.parse(is);
        	fail("Must not get here");
    	} catch (SAXException e) {
    		// success
    	}
    }   
 }
