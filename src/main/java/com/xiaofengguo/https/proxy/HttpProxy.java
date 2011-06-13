// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class HttpProxy implements Proxy {
  private static final Logger LOG = Logger.getLogger(HttpProxy.class.getCanonicalName());

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
  private final ServerSocket serverSocket;
  private final String remoteHostname;
  private final int remotePort;
  
  public HttpProxy(ServerSocket serverSocket, String remoteHostname, int remotePort) {
    this.serverSocket = serverSocket;
    this.remoteHostname = remoteHostname;
    this.remotePort = remotePort;
  }

  public static Proxy createProxy(int localPort, String remoteHostname,
      int remotePort) throws IOException {
    LOG.info("Create proxy service on port " + localPort + ", and forward to "
        + remoteHostname + ":" + remotePort);
    ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
    ServerSocket serverSocket = serverSocketFactory.createServerSocket(localPort);
    return new HttpProxy(serverSocket, remoteHostname, remotePort);
  }

  public void start() {
    SocketFactory clientFactory = SocketFactory.getDefault();
    while (true) {
      try {
        LOG.info("1");
        Socket socket = serverSocket.accept();
        Socket clientSocket = clientFactory.createSocket(remoteHostname, remotePort);
        InputStream inSrc = socket.getInputStream();
        LOG.info("Here");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inSrc));
        String line = null;
        while ((line = reader.readLine()) != null) {
          LOG.info(line);
        }
        LOG.info("END");
//        OutputStream outSrc = socket.getOutputStream();
//        InputStream inDest = clientSocket.getInputStream();
//        OutputStream outDest = clientSocket.getOutputStream();
//        
//        TeeStream tee = new TeeStream(outDest, System.out);
//        IOUtils.copy(inSrc, tee);
//        IOUtils.copy(inDest, outSrc);
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Error in http forwarding", e);
      }
    }
  }
}
