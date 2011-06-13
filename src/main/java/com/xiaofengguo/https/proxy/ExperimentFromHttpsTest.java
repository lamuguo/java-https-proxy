package com.xiaofengguo.https.proxy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExperimentFromHttpsTest {
  private static final Logger LOG = Logger
      .getLogger(ExperimentFromHttpsTest.class.getCanonicalName());

  private static File store;
  static {
    try {
      store = File.createTempFile("key_store", "bks");
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed on create temp keystore file", e);
    }
  }

  public void setUp() throws Exception {
    if (store != null) {
      String ksFileName = "org/apache/harmony/luni/tests/key_store."
          + KeyStore.getDefaultType().toLowerCase();
      InputStream in = getClass().getClassLoader().getResourceAsStream(
          ksFileName);
      FileOutputStream out = new FileOutputStream(store);
      BufferedInputStream bufIn = new BufferedInputStream(in, 8192);
      while (bufIn.available() > 0) {
        byte[] buf = new byte[128];
        int read = bufIn.read(buf);
        out.write(buf, 0, read);
      }
      bufIn.close();
      out.close();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
//    setUp();
  }

}
