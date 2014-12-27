package idc.tests;


import idc.des.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Some tests for the <code>Base64OutputStream</code> class.
 */
public class Base64OutputStreamTest extends TestCase {

  @Test
  public void testSimpleOutput() throws IOException {
    byte[] content = new byte[] {116, 101, 115, 116, 32, 109, 101,
                                 32, 112, 108, 101, 97, 115, 101};
    String expected = "dGVzdCBtZSBwbGVhc2U=";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Base64OutputStream b64OS = new Base64OutputStream(baos);
    for (byte b : content) {
      b64OS.write(b);
    }
    b64OS.finish();
    assertEquals(expected, baos.toString());
    b64OS.close();
  }

  @Test
  public void testEarlyFinish() throws IOException {
    byte[] content = new byte[] {116, 101, 115, 116, 32, 109, 101,
                                 32, 112, 108, 101, 97, 115, 101};
    String earlyExpected = "dGVzdCBtZSBwbGVh";
    String expected = "dGVzdCBtZSBwbGVhc2U=";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Base64OutputStream b64OS = new Base64OutputStream(baos);
    for (byte b : content) {
      b64OS.write(b);
    }
    assertEquals(earlyExpected, baos.toString());  // Missing the final conversion.
    b64OS.finish();
    assertEquals(expected, baos.toString());  // Fully converted.
    // Writing more should fail.
    try {
      b64OS.write(40);
      fail("Should have thrown an exception!");
    } catch (IOException e) {}
    b64OS.close();
  }
}
