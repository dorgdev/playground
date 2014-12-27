package idc.tests;


import idc.des.Base64OutputStream;
import idc.des.DataCipherInterface;
import idc.des.DesCipher;
import idc.des.DesConfig;
import idc.des.DesConfig.BlockMode;
import idc.des.DesConfig.Op;
import idc.des.DesConfig.PlainFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests the DES encryption to see that it works as intended.
 */
public class DesTest extends TestCase {

  /**
   * A simple testing data cipher which performs a simple XOR for crypting.
   */
  private class TestXorDataCipher implements DataCipherInterface {
    @Override
    public long encrypt(long plain, long key) {
      return plain ^ key;
    }
    
    @Override
    public long decrypt(long cipher, long key) {
      return cipher ^ key;
    }
  }

  /** A sample key to be used in the test. */
  public static final String KEY = 
      "da380dff9b9ea5946b0324f3f782605599f09a172efc6a85884adf5eaf293af8b8cdba9759e" +
      "f28723928371ec1880262e862873a6ad9f8b6ebca7adef7c8672fe109e160722689ac5dbd05" +
      "aa6111404fa0f3f45f63cf6e664a02d5b2f51f30ebb01273e6cbb393728926865d2e6e5164d" +
      "cddd8a922289abb36f8decb67a213d6cc68a0a2f60a67ed74a01dc58e87d4767f420bf5bdc6";

  /**
   * Converts the given input to base64. Helper for comparing DES operation on ASCII and
   * RADIX64 in parallel.
   * @param input The input to translate.
   * @return The Radix64 output.
   * @throws IOException Shouldn't happen, for test correctness only.
   */
  private String toRadix64(String input) throws IOException {
    return toRadix64(input.getBytes());
  }

  /**
   * Converts the given input to base64. Helper for comparing DES operation on ASCII and
   * RADIX64 in parallel.
   * @param input The input to translate.
   * @return The Radix64 output.
   * @throws IOException Shouldn't happen, for test correctness only.
   */
  private String toRadix64(byte[] input) throws IOException {
    ByteArrayOutputStream inner = new ByteArrayOutputStream();
    Base64OutputStream b64wrapper = new Base64OutputStream(inner);
    b64wrapper.write(input);
    b64wrapper.finish();
    b64wrapper.close();
    return inner.toString();
  }
  
  /**
   * A helper method for the tests.
   * @param config The config to set for the Des cipher.
   * @param input The input (plain or cipher).
   * @param expectedOutput The expected result of the operation (or the cipher
   * for verification).
   * @throws Exception In case of an error in the process.
   */
  private void runDes(BlockMode mode, PlainFormat format, String plain) throws Exception {
    runDes(mode, format, plain, KEY);
  }

  /**
   * A helper method for the tests.
   * @param config The config to set for the Des cipher.
   * @param input The input (plain or cipher).
   * @param key The key to use. Should be in HEX format.
   * @param expectedOutput The expected result of the operation (or the cipher
   * for verification).
   * @throws Exception In case of an error in the process.
   */
  private void runDes(BlockMode mode, PlainFormat format, String plain, String key)
      throws Exception {
    DesConfig config = new DesConfig(Op.ENCRYPT, format, mode);
    DesCipher des = new DesCipher(config);

    // Start with encryption.
    InputStream inStream = new ByteArrayInputStream(plain.getBytes());
    InputStream keyStream = new ByteArrayInputStream(key.getBytes());
    OutputStream outStream = new ByteArrayOutputStream();
    des.encrypt(inStream, keyStream, outStream);
    String output = outStream.toString();

    // Now make sure the validation works as well.
    inStream = new ByteArrayInputStream(plain.getBytes());
    keyStream = new ByteArrayInputStream(KEY.getBytes());
    InputStream resStream = new ByteArrayInputStream(output.getBytes());
    assertTrue(des.verify(inStream, keyStream, resStream));

    // And decrypt back to the original plain.
    inStream = new ByteArrayInputStream(output.getBytes());
    keyStream = new ByteArrayInputStream(KEY.getBytes());
    outStream = new ByteArrayOutputStream();
    des.decrypt(inStream, keyStream, outStream);
    assertEquals(plain, outStream.toString());
  }
  
  //
  // Tests for successful operations.
  //

  @Test
  public void testEmptyBuffer() throws Exception {
    String plain = "";
    String b64plain = toRadix64(plain);

    runDes(BlockMode.ECB, PlainFormat.ASCII, plain);
    runDes(BlockMode.ECB, PlainFormat.RADIX64, b64plain);

    runDes(BlockMode.CBC, PlainFormat.ASCII, plain);
    runDes(BlockMode.CBC, PlainFormat.RADIX64, b64plain);
  }

  @Test
  public void testPartialBuffer() throws Exception {
    String plain = "Half";
    String b64plain = toRadix64(plain);
    
    runDes(BlockMode.ECB, PlainFormat.ASCII, plain);
    runDes(BlockMode.ECB, PlainFormat.RADIX64, b64plain);

    runDes(BlockMode.CBC, PlainFormat.ASCII, plain);
    runDes(BlockMode.CBC, PlainFormat.RADIX64, b64plain);
  }

  @Test
  public void testOneBuffer() throws Exception {
    String plain = "FullBuff";
    String b64plain = toRadix64(plain);

    runDes(BlockMode.ECB, PlainFormat.ASCII, plain);
    runDes(BlockMode.ECB, PlainFormat.RADIX64, b64plain);

    runDes(BlockMode.CBC, PlainFormat.ASCII, plain);
    runDes(BlockMode.CBC, PlainFormat.RADIX64, b64plain);
  }

  @Test
  public void testFewBuffers() throws Exception {
    String plain = "More than one buffer...";
    String b64plain = toRadix64(plain);

    runDes(BlockMode.ECB, PlainFormat.ASCII, plain);
    runDes(BlockMode.ECB, PlainFormat.RADIX64, b64plain);

    runDes(BlockMode.CBC, PlainFormat.ASCII, plain);
    runDes(BlockMode.CBC, PlainFormat.RADIX64, b64plain);
  }
  
  //
  // Tests for expected failures.
  //

  @Test
  public void testKeyTooShort() throws IOException {
    // No key at all.
    try {
      runDes(BlockMode.ECB, PlainFormat.ASCII, "Text", "");
      fail("Shouldn't get here...");
    } catch (Exception e) { /* Should get here */ }

    // Not enough for one block.
    try {
      runDes(BlockMode.ECB, PlainFormat.ASCII, "Text", "0123");
      fail("Shouldn't get here...");
    } catch (Exception e) { /* Should get here */ }
    
    // Exactly one block, but should be 2... 
    try {
      runDes(BlockMode.ECB, PlainFormat.ASCII, "TextText", "0011223344556677");
      fail("Shouldn't get here...");
    } catch (Exception e) { /* Should get here */ }

    // More than a block. Missing key to finish. 
    try {
      runDes(BlockMode.ECB, PlainFormat.ASCII, "TextTextText", "00112233445566778899");
      fail("Shouldn't get here...");
    } catch (Exception e) { /* Should get here */ }
  }
  
  @Test
  public void testIncorrectPadding() throws IOException {
    DesConfig config = new DesConfig(Op.ENCRYPT, PlainFormat.ASCII, BlockMode.ECB);
    config.setDataCipher(new TestXorDataCipher());
    // No trailing padding
    try {
      String cipher = toRadix64(new byte[] { 0x11, 0x22, 0x33, 0x44, 0x11, 0x22, 0x33, 0x44});
      DesCipher des = new DesCipher(config);
      des.decrypt(
          new ByteArrayInputStream(cipher.getBytes()), 
          new ByteArrayInputStream(KEY.getBytes()), 
          new ByteArrayOutputStream());
      fail("Shouldn't et here...");
    } catch (Exception e) { /* Should get here */ }
    // Trailing padding without first padding byte
    try {
      String cipher = toRadix64(new byte[] { 0x11, 0x22, 0x33, 0x44, 0x00, 0x00, 0x00, 0x00});
      DesCipher des = new DesCipher(config);
      des.decrypt(
          new ByteArrayInputStream(cipher.getBytes()), 
          new ByteArrayInputStream(KEY.getBytes()), 
          new ByteArrayOutputStream());
      fail("Shouldn't et here...");
    } catch (Exception e) { /* Should get here */ }
    // Only trailing padding
    try {
      String cipher = toRadix64(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0});
      DesCipher des = new DesCipher(config);
      des.decrypt(
          new ByteArrayInputStream(cipher.getBytes()), 
          new ByteArrayInputStream("00000000000000000000000000000000".getBytes()), 
          new ByteArrayOutputStream());
      fail("Shouldn't et here...");
    } catch (Exception e) { /* Should get here */ }
  }
}
