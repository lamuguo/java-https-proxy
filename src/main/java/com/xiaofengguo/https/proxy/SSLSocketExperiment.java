package com.xiaofengguo.https.proxy;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class SSLSocketExperiment {
  private static final Logger LOG = Logger.getLogger(SSLSocketExperiment.class
      .getCanonicalName());

  public static class TeeStream extends OutputStream {
    OutputStream out1;
    OutputStream out2;

    public TeeStream(OutputStream out1, OutputStream out2) {
      this.out1 = out1;
      this.out2 = out2;
    }

    @Override
    public void write(int b) throws IOException {
      out1.write(b);
      out2.write(b);
    }

    @Override
    public void close() throws IOException {
      out1.close();
      out2.close();
    }

    @Override
    public void flush() throws IOException {
      out1.flush();
      out2.flush();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    // Set default SSL properties
    System.setProperty("javax.net.debug", "ssl");
    System.setProperty("javax.net.ssl.keyStore",
        "/Users/xiaofengguo/temp/mySrvKeystore");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");
    System.setProperty("javax.net.ssl.trustStore",
        "/Users/xiaofengguo/temp/mySrvKeystore");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");

    // Create Jetty Daemon
    createHttpServer(8888);

    // Create a forward ssl
    createForwardSSLService(9999);

    LOG.info("Start to connect HTTP: 8888");
    // Test 8888.
    // URLConnection conn = new URL("http://localhost:8888/").openConnection();
    // conn.setDoOutput(true);
    // conn.connect();
    //
    // LOG.info("Get HTTP result on 8888: " + conn.getContent());

    // test 9999.
    HttpsURLConnection conn2 = (HttpsURLConnection) new URL(
        "https://127.0.0.1:9999").openConnection();
    conn2.setDoOutput(true);
    conn2.connect();
    conn2.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
    conn2.setHostnameVerifier(new HostnameVerifier() {
      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    });
    OutputStream testOut = new BufferedOutputStream(conn2.getOutputStream());
    for (int i = 0; i < 2000; ++i) {
      testOut.write(i % 100);
    }
    testOut.close();
    int resp = conn2.getResponseCode();
    Thread.sleep(1000);

    LOG.info("Response code = " + resp);

    LOG.info("Get HTTPS result on 9999: " + conn2.getContent());
  }

  private static void createForwardSSLService(int port) {
    final int sslPort = port;

    new Thread(new Runnable() {
      public void run() {
        try {
          SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
              .getDefault();
          SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory
              .createServerSocket(sslPort);
          LOG.info("ssl socket waiting on " + sslPort);

          SocketFactory clientFactory = SocketFactory.getDefault();

          while (true) {
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
            
            sslsocket.startHandshake();
            Socket clientSocket = clientFactory.createSocket("127.0.0.1", 8888);

            // Src <=> Proxy <=> Dest
            //
            // inSrc -> Proxy -> outDest
            // outSrc <- Proxy <- inDest
            InputStream inSrc = sslsocket.getInputStream();
            OutputStream outSrc = sslsocket.getOutputStream();
            
            InputStream inDest = clientSocket.getInputStream();
            OutputStream outDest = clientSocket.getOutputStream();

            TeeStream tee = new TeeStream(outDest, System.out);

            IOUtils.copy(inSrc, tee);
            IOUtils.copy(inDest, outSrc);
          }
        } catch (Exception e) {
          LOG.log(Level.SEVERE,
              "SSL forward service is stopped, due to exception", e);
        }
      }
    }).start();
  }

  private static void createHttpServer(int port) {
    final int serverPort = port;

    new Thread(new Runnable() {
      public void run() {
        Server server = new Server(serverPort);
        server.setHandler(new AbstractHandler() {
          public void handle(String target, HttpServletRequest request,
              HttpServletResponse response, int dispatch) throws IOException,
              ServletException {
            LOG.info("Get request: " + request);
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello</h1>");
            ((Request) request).setHandled(true);
          }
        });
        try {
          server.start();
          server.join();
        } catch (Exception e) {
          LOG.log(Level.SEVERE, "Http server stopped due to exception", e);
        }
      }
    }).start();
  }
}
