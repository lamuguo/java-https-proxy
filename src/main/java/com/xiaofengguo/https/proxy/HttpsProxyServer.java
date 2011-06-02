// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;

/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class HttpsProxyServer {

  /**
   * 
   */
  public HttpsProxyServer() {
    // TODO(xiaofengguo): Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Server server = new Server();

    // Create https connector.    
    SslSocketConnector httpsConnector = new SslSocketConnector();
    httpsConnector.setPort(8443);
    httpsConnector.setKeystore("/tmp/https/localKeyStore");
    httpsConnector.setPassword("123456");
    httpsConnector.setKeyPassword("123456");
    server.addConnector(httpsConnector);
    
    server.setHandler(new ReverseProxyHandler());
    
    try {
      server.start();
      server.join();
    } catch (Exception e) {
      // TODO(xiaofengguo): Auto-generated catch block
      e.printStackTrace();
    }
  }

}
