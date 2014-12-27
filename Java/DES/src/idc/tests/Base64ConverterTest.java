package idc.tests;


import idc.des.Base64Converter;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Some tests for the <code>Base64Converter</code> class.
 */
public class Base64ConverterTest extends TestCase {

  @Test
  public void testSimpleBytesToBase64Conversion() {
    // No "=" needed.
    String data = "This is a simple message";
    String expected = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdl";
    String result = String.valueOf(Base64Converter.toBase64(data.getBytes()));
    assertEquals(expected, result);
    // A single "=" suffix.
    data = "This is a simple message!";
    expected = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdlIQ==";
    result = String.valueOf(Base64Converter.toBase64(data.getBytes()));
    assertEquals(expected, result);
    // Double "==" suffix.
    data = "This is a simple message!!";
    expected = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdlISE=";
    result = String.valueOf(Base64Converter.toBase64(data.getBytes()));
    assertEquals(expected, result);
  }

  @Test
  public void testSimpleBase64ToBytesConversion() {
    // No "=" needed.
    String data = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdl";
    String expected = "This is a simple message";
    byte[] resultBytes = Base64Converter.fromBase64(data.toCharArray());
    String result = new String(resultBytes);
    assertEquals(expected, result);
    // A single "=" suffix.
    data = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdlIQ==";
    expected = "This is a simple message!";
    resultBytes = Base64Converter.fromBase64(data.toCharArray());
    result = new String(resultBytes);
    assertEquals(expected, result);
    // Double "==" suffix.
    data = "VGhpcyBpcyBhIHNpbXBsZSBtZXNzYWdlISE=";
    expected = "This is a simple message!!";
    resultBytes = Base64Converter.fromBase64(data.toCharArray());
    result = new String(resultBytes);
    assertEquals(expected, result);
  }
  
  @Test
  public void testInvalidBase64Inputs() {
    String data;
    // Illegal length.
    data = "Not+A+Valid+Length";
    try {
      Base64Converter.fromBase64(data.toCharArray());
      fail("This should have failed.");
    } catch (Exception e) { }
    // Invalid padding locations - in the middle.
    data = "Padding+in=the+middle+++";
    assertEquals(0, data.length() % 4);
    try {
      Base64Converter.fromBase64(data.toCharArray());
      fail("This should have failed.");
    } catch (Exception e) { }
    // Invalid padding locations - suffix of 3.
    data = "cant+have+3+padding+in+the+suffix===";
    assertEquals(0, data.length() % 4);
    try {
      Base64Converter.fromBase64(data.toCharArray());
      fail("This should have failed.");
    } catch (Exception e) { }
    // Invalid characters.
    data = "this data contains spaces which are invalid+";
    assertEquals(0, data.length() % 4);
    try {
      Base64Converter.fromBase64(data.toCharArray());
      fail("This should have failed.");
    } catch (Exception e) { }
  }
  
  @Test
  public void testEdgeCases() {
    // toBase64: empty or null inputs.
    char[] chars = Base64Converter.toBase64(null);
    assertNotNull(chars);
    assertEquals(0, chars.length);

    chars = Base64Converter.toBase64(new byte[] {});
    assertNotNull(chars);
    assertEquals(0, chars.length);

    // fromBase64: empty or null inputs.
    byte[] bytes = Base64Converter.fromBase64(null);
    assertNotNull(bytes);
    assertEquals(0, bytes.length);

    bytes = Base64Converter.fromBase64(new char[] {});
    assertNotNull(bytes);
    assertEquals(0, bytes.length);
}
}
