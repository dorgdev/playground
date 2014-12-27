package idc.tests;

import idc.des.Utils;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests the general purpose methods in the utility class Utils.
 */
public class UtilsTest extends TestCase {

  @Test
  public void testUtilsLongMethods() {
    long l = 0x0001020304050607L;
    byte[] buf = Utils.fromLong(l);
    assertEquals(8, buf.length);
    for (int i = 0; i < buf.length; ++i) {
      assertEquals(buf[i], i);
    }
    assertEquals(l, Utils.toLong(buf));
  }

  @Test
  public void testSetBit() {
    assertEquals(3L, Utils.setBit(1L, 62, true));
    assertEquals(1024L, Utils.setBit(0L, 53, true));
    assertEquals(1025, Utils.setBit(1024L, 63, true));
    assertEquals(1025, Utils.setBit(1025, 3, false));
  }

  @Test
  public void testGetBit() {
    assertEquals(false, Utils.getBit(2L, 63));
    assertEquals(true, Utils.getBit(2L, 62));
  }
}
