// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import org.mortbay.jetty.Server;

/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 *
 */
public class JettyExperiment {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    Server server = new Server(8888);
    server.start();
    server.stop();
  }
}
