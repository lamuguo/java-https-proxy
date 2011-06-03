// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;


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
  public static void main(String[] args) throws IOException {
//    Server server = new Server();
//
//    // Create https connector.    
//    SslSocketConnector httpsConnector = new SslSocketConnector();
//    httpsConnector.setPort(8443);
//    httpsConnector.setKeystore("/tmp/https/localKeyStore");
//    httpsConnector.setPassword("123456");
//    httpsConnector.setKeyPassword("123456");
//    server.addConnector(httpsConnector);
//
//    server.setHandler(new ReverseProxyHandler());
//    
//    try {
//      server.start();
//      server.join();
//    } catch (Exception e) {
//      // TODO(xiaofengguo): Auto-generated catch block
//      e.printStackTrace();
//    }
//

//    System.setProperty("javax.net.ssl.keyStore", "/tmp/https/localKeyStore");
//    System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//    System.setProperty("javax.net.debug","all");
    ServerSocketFactory factory = ServerSocketFactory.getDefault();
    ServerSocket socketServer = factory.createServerSocket(9000);
//    LOG.info("enabled protocols: " + Arrays.toString(socketServer.getEnabledProtocols()));
//    LOG.info("supported protocols: " + Arrays.toString(socketServer.getSupportedProtocols()));
//    LOG.info("enable session create: " + socketServer.getEnableSessionCreation());

    Socket socket = socketServer.accept();

    InputStream in = socket.getInputStream();
    OutputStream out = socket.getOutputStream();
    
    Socket sock = new Socket("hudson.xiaofengguo.com", 8080);
    
    OutputStream sockOut = sock.getOutputStream();
    int count = -1;
    byte buf[] = new byte[256 * 1024];
    while ((count = in.read(buf)) > 0) {
      sockOut.write(buf, 0, count);
      LOG.info("Wrote : \"" + new String(buf, 0, count) + "\"");
    }
    
    InputStream sockIn = sock.getInputStream();
    count = -1;
    while ((count = sockIn.read(buf)) > 0) {
      out.write(buf, 0, count);
      LOG.info("Wrote back: \"" + new String(buf, 0, count) + "\"");
    }
    sock.close();    
    socket.close();
  }

}
