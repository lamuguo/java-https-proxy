// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class ReverseProxyHandler extends AbstractHandler {

  /* (non-Javadoc)
   * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
   */
  public void handle(String target, HttpServletRequest request, HttpServletResponse response,
      int dispatch) throws IOException, ServletException {
    RequestDispatcher dispatcher = request.getRequestDispatcher("/hudson");
    dispatcher.forward(request, response);
  }
}
