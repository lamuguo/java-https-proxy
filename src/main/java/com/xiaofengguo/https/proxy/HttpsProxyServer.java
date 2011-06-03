// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


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

    System.setProperty("javax.net.ssl.keyStore", "/tmp/https/localKeyStore");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//    System.setProperty("javax.net.debug","all");
    SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    SSLServerSocket socketServer = (SSLServerSocket) factory.createServerSocket(9000);
    LOG.info("enabled protocols: " + Arrays.toString(socketServer.getEnabledProtocols()));
    LOG.info("supported protocols: " + Arrays.toString(socketServer.getSupportedProtocols()));
    LOG.info("enable session create: " + socketServer.getEnableSessionCreation());

    SSLSocket socket = (SSLSocket) socketServer.accept();
//    socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
//      public void handshakeCompleted(HandshakeCompletedEvent arg0) {
//        LOG.info("Handshake complete");
//      }
//    });
//    socket.startHandshake();
    
//    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//    String line;
//    while ((line = reader.readLine()) != null) {
//      LOG.info("got line : \"" + line + "\"");
//    }

    final String[] HEX = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    int count = -1;
    byte[] buf = new byte[256 * 1024];
    while (((count = System.in.read(buf)) > 0)) {
      String str = new String();
      for (int i = 0; i < count; ++i) {
        str = str + HEX[buf[i]>>4 & 0xf] + HEX[buf[i] & 0xf];
      }
      LOG.info("Read :\"" + str + "\"");
    }
    LOG.info("Count = " + count);
    socket.close();
  }

}
