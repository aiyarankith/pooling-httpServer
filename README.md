# pooling-httpServer

Multi-threaded http web-server with thread pooling
---

Java is used here, as the core language to create and implement a HTTP server. The main API used for creating the server is oracle java.net package. Here the `HttpMethodHandlerInterface` is used so that the server configurations can be modified easily just by changing the properties and adding more features to it. Currently the only supported methods are GET and DELETE. The server also handles thread-pool which is offered by `java.util` package for concurrency management to avoid overhead in threads creation.  

The `src/main` folder contains the java source files while the `src/test` folder contains some junit tests for multithreaded server implementation. 

The server design is an extension of __Dr. David Bernstein's__ lecture on [Design and Implementation of an HTTP Server - A Simple  Network Application in Java](https://users.cs.jmu.edu/bernstdh/web/common/lectures/slides_http-server-example_java.php). 

I have tried to extend the implementation by adding these features to the base code: 

1. The `ServerConfig` class which is mainly used to configure the server properties which is read from the .properties file.
2. Use of `maven` for dependency management.
3. Made use of `SLF4` for logging 
4. Implementing the `Get Handler` and `Delete Handler` methods. 
5. Implemented the extension of `HTTP keep-alive` behavior.
 
Libraries:
---
The following libraries are used:

1. `httpclient 4.3.6`: HTTP client for multi-threading
2. `jackrabbit-webdav 2.8.0`: DomUtil class
3. `slf4j`: logging
4. `ch.qos.logback`: log-back support.
5. `pom.xml`: maven 
6. `junit 4.0`: Unit Testing

Running:
---
Command Prompt:
curl request example
`curl -H "Accept: application/json" -H "Content-Type: application/xml" -X GET http://localhost:3000/json.json`

