// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;


/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class HttpProxy implements Proxy {
  private static final Logger LOG = Logger.getLogger(HttpProxy.class.getCanonicalName());
  
  private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;
  
  /**
   * This is a workable version but slow, on thinking about nio solutions.
   * 
   * @author xiaofengguo@google.com (Xiaofeng Guo)
   *
   */
  private class ProxyRunnable implements Runnable {
    protected final Socket socket;
    private final SocketFactory clientFactory = SocketFactory.getDefault();

    public ProxyRunnable(Socket socket) {
      this.socket = socket;
    }
    public void run() {
      try {
        LOG.info("Connect to " + remoteHostname + ":" + remotePort);
        Socket clientSocket = clientFactory.createSocket(remoteHostname, remotePort);
        final InputStream inSrc = socket.getInputStream();
        final OutputStream outSrc = socket.getOutputStream();
        final InputStream inDest = clientSocket.getInputStream();
        final OutputStream outDest = clientSocket.getOutputStream();
        LOG.info("6");

        executor.execute(new Runnable() {
          public void run() {
            try {
              IOUtils.copy(inSrc, new TeeOutputStream(outDest, System.out));
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "Errors in forwarding request", e);
            }
          }
        });
        LOG.info("7");
        IOUtils.copy(inDest, new TeeOutputStream(outSrc, System.out));
        LOG.info("8");
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Error in http forwarding", e);
      }
    }
  }
  
  private class NonBlockingProxyRunnable extends ProxyRunnable {
    public NonBlockingProxyRunnable(Socket socket) {
      super(socket);
    }

    public void run() {
      try {
        Socket clientSocket = SocketFactory.getDefault().createSocket(remoteHostname, remotePort);
        final InputStream inSrc = socket.getInputStream();
        final OutputStream outSrc = socket.getOutputStream();
        final InputStream inDest = clientSocket.getInputStream();
        final OutputStream outDest = clientSocket.getOutputStream();
        
        executor.execute(new Runnable() {
          public void run() {
            try {
              NonBlockingCopyUtils.copy(inSrc, new TeeOutputStream(outDest, System.out));
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "Errors in forwarding request", e);
            }
          }
        });

        NonBlockingCopyUtils.copy(inDest, outSrc);
      } catch (UnknownHostException e) {
        // TODO(xiaofengguo): Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO(xiaofengguo): Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
//  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final ExecutorService executor = Executors.newFixedThreadPool(100);

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
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        executor.execute(new NonBlockingProxyRunnable(socket));
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Errors in accepting calls", e);
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Proxy proxy = createProxy(9998, "hudson.xiaofengguo.com", 8080);
    proxy.start();
  }
}
