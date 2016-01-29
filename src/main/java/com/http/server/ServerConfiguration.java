package com.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.http.MIMETyper;
import com.http.methods.HttpMethodHandlerInterface;

/**
 * This class contains all the necessary information to bootstrap the http server.
 * 
 * @author Ankith Aiyar
 */

public class ServerConfiguration {

	private static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);

	private static String ELEMENT_SUPPORTED_METHODS = "supportedmethods";
	private static String ELEMENT_SUPPORTED_METHOD = "supportedmethod";
	private static final String ELEMENT_CLASS = "class";
	private static final String ATTR_NAME = "name";

	/**
	 * Host from configuration file. Default host localhost
	 */
	private static final String HOST = "webserver.host";
	
	/**
	 * Port from configuration file. Default port 0.
	 */
	private static final String PORT = "webserver.port";

	/**
	 * Root directory of the webserver.
	 */
	private static final String ROOT_DIR = "webserver.webroot";

	private final Properties prop;
	private List<HttpMethodHandlerInterface> handlers;
	
	public ServerConfiguration() {
		handlers = new ArrayList<HttpMethodHandlerInterface>();
		prop = new Properties();
	}

	public int getPort() {
		return Integer.parseInt(prop.getProperty(PORT, "0"));
	}

	public String getWebRoot() {
		return prop.getProperty(ROOT_DIR);
	}

	public String getHost() {
		return prop.getProperty(HOST, "localhost");
	}

	public List<HttpMethodHandlerInterface> getHandlers() {
		return Collections.unmodifiableList(handlers);
	}

	public void load(InputStream inputStream) throws IOException {
		prop.load(inputStream);
	}
	
	/**
	 * 
	 * @param 
	 * @throws SAXException
	 * if the document cannot be parsed.
	 */
	public void parse(InputStream is) throws SAXException {
		try {
			Element config = DomUtil.parseDocument(is).getDocumentElement();
			if (config == null) {
				log.warn("Missing mandatory configuration field");
				return;
			}
			Element el = DomUtil.getChildElement(config, ELEMENT_SUPPORTED_METHODS, null);
			if (el != null) {
				ElementIterator handlerElements = DomUtil.getChildren(el, ELEMENT_SUPPORTED_METHOD, null);
				while (handlerElements.hasNext()) {
					Element handler = handlerElements.nextElement();
					HttpMethodHandlerInterface instance = buildClassFromConfig(handler);
					if (instance != null) {
						instance.init(this);
						handlers.add(instance);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			throw new SAXException(e.getMessage());
		} catch (IOException e) {
			throw new SAXException(e.getMessage());
		}
	}

	private static HttpMethodHandlerInterface buildClassFromConfig(Element parent) {
		// Source:- org.apache.jackrabbit.webdav.simple.ResourceConfig 
		// Tweeked for our needs
		HttpMethodHandlerInterface instance = null;
		Element classElem = DomUtil.getChildElement(parent, ELEMENT_CLASS, null);
		if (classElem != null) {
			// contains a 'class' child node
			try {
				String className = DomUtil.getAttribute(classElem, ATTR_NAME, null);
				if (className != null) {

					Class<?> classN = Class.forName(className);

					if (HttpMethodHandlerInterface.class.isAssignableFrom(classN)) {
						instance = (HttpMethodHandlerInterface) classN.newInstance();
					} else {
						log.warn(className + " must implement the HttpMethodHandler interface");
					}
				}
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
		return instance;
	}

}
