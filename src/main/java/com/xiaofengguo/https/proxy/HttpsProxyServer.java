// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import java.io.IOException;
import java.util.logging.Logger;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;


/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class HttpsProxyServer {
  private static final Logger LOG = Logger.getLogger(HttpsProxyServer.class.getCanonicalName());

  /**
   * 
   */
  public HttpsProxyServer() {
    // TODO(xiaofengguo): Auto-generated constructor stub
  }

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    Server server = new Server();

    // Create https connector.    
    SocketConnector httpsConnector = new SocketConnector();
    httpsConnector.setPort(8443);
//    httpsConnector.setKeystore("/tmp/https/localKeyStore");
//    httpsConnector.setPassword("123456");
//    httpsConnector.setKeyPassword("123456");
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
