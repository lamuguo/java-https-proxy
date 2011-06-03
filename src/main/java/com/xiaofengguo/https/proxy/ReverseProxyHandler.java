// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.mortbay.jetty.servlet.ServletHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class ReverseProxyHandler extends ServletHandler {
  private static final Logger LOG = Logger.getLogger(ReverseProxyHandler.class.getCanonicalName());

  private static final int DEFAULT_BUF_SIZE = 256 * 1024;
  
  public ReverseProxyHandler() {
  }

  /* (non-Javadoc)
   * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
   */
  public void handle(String target, HttpServletRequest request, HttpServletResponse response,
      int dispatch) throws IOException, ServletException {
    LOG.info("It is here");
    InputStream in = request.getInputStream();
    OutputStream out = response.getOutputStream();
    
    Socket sock = new Socket("hudson.xiaofengguo.com", 8080);
    
    OutputStream sockOut = sock.getOutputStream();
    int count = -1;
    byte buf[] = new byte[DEFAULT_BUF_SIZE];
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
  }
}
