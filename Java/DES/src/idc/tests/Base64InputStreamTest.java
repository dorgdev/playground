package idc.tests;


import idc.des.Base64InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Some tests for the <code>Base64InputStream</code> class.
 */
public class Base64InputStreamTest extends TestCase {

  @Test
  public void testSimpleInput() throws IOException {
    String content = "dGVzdCBtZSBwbGVhc2U=";
    int[] expectedValues = new int[] {116, 101, 115, 116, 32, 109, 101, 
                                      32, 112, 108, 101, 97, 115, 101};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    Base64InputStream b64IS = new Base64InputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, b64IS.read());
    }
    assertEquals(-1, b64IS.read());
    b64IS.close();
  }

  @Test
  public void testEOS() throws IOException {
    String content = "dGVzdCBtZSBwbGVhc2Uh2+";
    int[] expectedValues = new int[] {116, 101, 115, 116, 32, 109, 101, 32,
                                      112, 108, 101, 97, 115, 101, 33};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    Base64InputStream b64IS = new Base64InputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, b64IS.read());
    }
    // Next read should encounter an invalid EOS.
    try {
      b64IS.read();
      fail("Read should have failed.");
    } catch (IOException e) {
      // Expected behavior.
    }
    b64IS.close();
  }

  @Test
  public void testInvalidBase64() throws IOException {
    String content = "this is not a valid base64 string. It has spaces.";
    int[] expectedValues = new int[] {182, 24, 172};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    Base64InputStream b64IS = new Base64InputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, b64IS.read());
    }
    // Next read should encounter an invalid EOS.
    try {
      b64IS.read();
      fail("Read should have failed.");
    } catch (IOException e) {
      // Expected behavior.
    }
    b64IS.close();
  }
}
