package com.xiaofengguo.https.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class HttpProxyTest extends TestCase {
  private static final Logger LOG = Logger.getLogger(HttpProxyTest.class.getCanonicalName());
  private int httpServerPort = 0;
  private int proxyServerPort = 0;
  private Thread httpServer = null;
  private Thread proxyServer = null;
  
  /**
   * Returns an unused port for testing.
   * 
   * TODO(lamuguo): Move this function to base proj.
   * Stolen from dotnetman-testing (http://dotnetman-testing.googlecode.com)
   * @throws IOException
   */
  private static int getUnusedPort() throws IOException {
    Socket s = new Socket();
    s.bind(null);

    try {
      return s.getLocalPort();
    } finally {
      s.close();
    }
  }

  @Override
  protected void setUp() throws Exception {
    System.out.println("help");
    LOG.info("help");
    super.setUp();
    
    httpServerPort = getUnusedPort();
    proxyServerPort = getUnusedPort();
    httpServer = startHttpService(httpServerPort);
    proxyServer = startProxyService(proxyServerPort, httpServerPort);
  }

  private Thread startProxyService(final int localPort, final int remotePort) throws InterruptedException {
    Thread server = new Thread(new Runnable() {
      public void run() {
        Proxy proxy = null;
        try {
           proxy = HttpProxy.createProxy(localPort, "127.0.0.1", remotePort);
        } catch (IOException e) {
          LOG.log(Level.SEVERE, "Failed in creating proxyServer", e);
        }
        proxy.start();
      }
    });
    server.start();
    waitTillPortReady(localPort);
    return server;
  }

  private Thread startHttpService(final int port) throws InterruptedException {
    Thread server = new Thread(new Runnable() {
      public void run() {
        Server server = new Server(port);
        LOG.info("Start http service on port: " + port);
        server.addHandler(new AbstractHandler() {
          public void handle(String target, HttpServletRequest request,
              HttpServletResponse response, int dispatch) throws IOException,
              ServletException {
            LOG.info("Get request: \"" + request);
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("<html><body>hello</body></html>");
          }
        });
        try {
          server.start();
        } catch (Exception e) {
          LOG.log(Level.SEVERE, "Jetty server down", e);
        }
      }
    });
    server.start();
    waitTillPortReady(port);

    return server;
  }

  private void waitTillPortReady(int port) throws InterruptedException {
    while (true) {
      try {
        Socket clientSocket = SocketFactory.getDefault().createSocket("127.0.0.1", port);
        clientSocket.close();
        break;
      } catch (IOException e) {
        LOG.info("Fail to connect port " + port);
        Thread.sleep(500);
      }
    }
  }

  @Override
  protected void tearDown() throws Exception {
    LOG.info("Tear down");
    if (httpServer != null) {
      httpServer.join();
    }
    if (proxyServer != null) {
      proxyServer.join();
    }

    super.tearDown();
  }

  public void testConnect() throws Exception {
//    URLConnection conn = new URL("http://localhost:" + proxyServerPort + "/").openConnection();
//    conn.setDoOutput(true);
//    conn.connect();
//    assertEquals("", IOUtils.toString(conn.getInputStream()));
  }
}
