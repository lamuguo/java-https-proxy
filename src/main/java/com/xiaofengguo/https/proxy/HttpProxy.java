// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class HttpProxy {
  private static final Logger LOG = Logger.getLogger(HttpProxy.class.getCanonicalName());

  /**
   * 
   */
  public HttpProxy() {
    // TODO(xiaofengguo): Auto-generated constructor stub
  }

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws Exception {
    System.setProperty("java.net.debug","all");
    System.setProperty("javax.net.ssl.keyStore", "/tmp/https/localKeyStore");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");

    // start a proxy server on port 8888
    Server proxy = new Server();
//    SelectChannelConnector connector = new SslSelectChannelConnector();
    SslSocketConnector connector = new SslSocketConnector();
    connector.setPort(8443);
    connector.setKeystore("/tmp/https/localKeyStore");
    connector.setTruststore("/tmp/https/localKeyStore");
    connector.setPassword("123456");
    connector.setKeyPassword("123456");
    connector.setTrustPassword("123456");
//    // Connector connector = new SocketConnector();
//    connector.setPort(8443);
    proxy.addConnector(connector);

    Context context = new Context(proxy,"/",0);
    context.addServlet(new ServletHolder(new AsyncProxyServlet.Transparent(null, "hudson.xiaofengguo.com", 8080)), "/");
//    context.addServlet(new ServletHolder(new AsyncProxyServlet()), "/");
    
    proxy.start();
    proxy.join();
  }

  
}
