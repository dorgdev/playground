package idc.tests;

import idc.des.HexInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Some tests for the <code>HexInputStreamTest</code> class.
 */
public class HexInputStreamTest extends TestCase {

  @Test
  public void testSimpleInput() throws IOException {
    String content = "01 234 ABF FFFF";
    int[] expectedValues = new int[] {1, 35, 74, 191, 255, 255};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    HexInputStream hexIS = new HexInputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, hexIS.read());
    }
    assertEquals(-1, hexIS.read());
    hexIS.close();
  }

  @Test
  public void testEOS() throws IOException {
    String content = "01 23 45 67 89 AB CD EF 0";
    int[] expectedValues = new int[] {1, 35, 69, 103, 137, 171, 205, 239};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    HexInputStream hexIS = new HexInputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, hexIS.read());
    }
    // Next read should encounter an invalid EOS.
    try {
      hexIS.read();
      fail("Read should have failed.");
    } catch (IOException e) {
      // Expected behavior.
    }
    hexIS.close();
  }
  
  @Test
  public void testAllValue() throws IOException {
    char[] vals = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', 
                              '8', '9', 'A', 'B', 'C', 'D', 'E' ,'F'};
    StringBuilder builder = new StringBuilder(512);
    for (int i = 0; i < 256; ++i) {
      builder.append(vals[i / 16]);
      builder.append(vals[i % 16]);
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
    HexInputStream hexIS = new HexInputStream(bais);
    for (int i = 0; i < 256; ++i) {
      assertEquals(i, hexIS.read());
    }
    assertEquals(-1, hexIS.read());
    hexIS.close();
  }
  
  @Test
  public void testMixedCase() throws IOException {
    String content = "aBCd eF 0ff0";
    int[] expectedValues = new int[] {171, 205, 239, 15, 240};
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
    HexInputStream hexIS = new HexInputStream(bais);
    for (int expectedValue : expectedValues) {
      assertEquals(expectedValue, hexIS.read());
    }
    assertEquals(-1, hexIS.read());
    hexIS.close();
  }
}
