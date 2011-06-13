package com.xiaofengguo.https.proxy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
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

  private static final String KEY_STORE_PATH = "/Users/xiaofengguo/temp/mySrvKeystore";
  private static final String KS_PASSWORD = "123456";
  /**
   * TODO(lamuguo):
   * - Write a blog of certificate_unknown.
   * - Make HTTP proxy run out result.
   * - Make HTTPs proxy run out result.
   * - Make program exit correctly.
   * 
   * @param args
   */
  public static void main(String[] args) {
    Thread httpServer = null;
    Thread forwardServer = null;
    try {
      // Set default SSL properties
      String type = KeyStore.getDefaultType();
      LOG.info("ks type = " + type + ", path = " + KEY_STORE_PATH);

//      System.setProperty("javax.net.debug", "all");

      System.setProperty("javax.net.ssl.keyStoreType", type);
      System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
      System.setProperty("javax.net.ssl.keyStorePassword", KS_PASSWORD);

      System.setProperty("javax.net.ssl.trustStoreType", type);
      System.setProperty("javax.net.ssl.trustStore", KEY_STORE_PATH);
      System.setProperty("javax.net.ssl.trustStorePassword", KS_PASSWORD);

      // Create Jetty Daemon
      httpServer = createHttpServer(8888);
      // Create a forward ssl
      forwardServer = createForwardSSLService(9999);

      LOG.info("Start to connect HTTP: 8888");
      // Test 8888.
      // URLConnection conn = new
      // URL("http://localhost:8888/").openConnection();
      // conn.setDoOutput(true);
      // conn.connect();
      //
      // LOG.info("Get HTTP result on 8888: " + conn.getContent());

      // test 9999.
      LOG.info("1");
      HttpsURLConnection conn2 = (HttpsURLConnection) new URL(
          "https://127.0.0.1:9999").openConnection();
      conn2.setHostnameVerifier(new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
      LOG.info("2");
//      conn2.setDoInput(true);
      conn2.setDoOutput(true);
      LOG.info("3");
      conn2.connect();
      LOG.info("4");
//      conn2.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory
//          .getDefault());
      OutputStream testOut = new BufferedOutputStream(conn2.getOutputStream());
      for (int i = 0; i < 2000; ++i) {
        testOut.write(i % 100);
      }
      testOut.close();
      int resp = conn2.getResponseCode();
      Thread.sleep(1000);

      LOG.info("Response code = " + resp);

      LOG.info("Get HTTPS result on 9999: " + conn2.getContent());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unknown", e);
    } finally {
      try {
        forwardServer.join(1000);
        httpServer.join(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
//      System.exit(0);
    }
  }

  private static Thread createForwardSSLService(int port) {
    final int sslPort = port;

    Thread forwardServer = new Thread(new Runnable() {
      public void run() {
        try {
          SSLContext ctx = getContext();
          ServerSocket sslServerSocket = ctx.getServerSocketFactory().createServerSocket(sslPort);
          LOG.info("ssl socket waiting on " + sslPort);

          SocketFactory clientFactory = SocketFactory.getDefault();

          while (true) {
            SSLSocket sslsocket = (SSLSocket) sslServerSocket.accept();

//            sslsocket.startHandshake();
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

      private SSLContext getContext() throws Exception {
        String type = KeyStore.getDefaultType();
        SSLContext ctx;

        String keyStore = KEY_STORE_PATH;
        File keyStoreFile = new File(keyStore);

        FileInputStream fis = new FileInputStream(keyStoreFile);

        KeyStore ks = KeyStore.getInstance(type);
        ks.load(fis, KS_PASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, KS_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        ctx = SSLContext.getInstance("TLSv1");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ctx;
      }
    });
    forwardServer.start();
    return forwardServer;
  }

  private static Thread createHttpServer(int port) {
    final int serverPort = port;

    Thread httpServer = new Thread(new Runnable() {
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
    });
    httpServer.start();
    return httpServer;
  }
}
