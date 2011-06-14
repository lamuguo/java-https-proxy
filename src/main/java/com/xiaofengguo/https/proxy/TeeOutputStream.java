package com.xiaofengguo.https.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class TeeOutputStream extends OutputStream {
  private static final Logger LOG = Logger.getLogger(TeeOutputStream.class.getCanonicalName());
  OutputStream out1;
  OutputStream out2;

  public TeeOutputStream(OutputStream out1, OutputStream out2) {
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
