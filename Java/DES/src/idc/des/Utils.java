package idc.des;

import java.nio.ByteBuffer;

/**
 * This class holds some utility methods used in the project.
 */
public final class Utils {

  /** A private CTOR to prevent instanciation. */
  private Utils() {}

  /**
   * Converting a given <code>byte[]</code> into a <code>long</code> value.
   * <b>Note:</b>Assumes the the buffer is 8 bytes long (long's size).
   * @param bytes The data to convert.
   * @return The <code>long</code> value represented by the given buffer.
   */
  public static long toLong(byte[] bytes) {
    long number = 0;
    // Constructs a new long number from given input
    for (int i = 0; i < bytes.length; i++) {
      // Move the MSBs upwords.
      number <<= Byte.SIZE;
      // Expand single byte into unsigned long.
      number |= ((long) bytes[i]) & 0xFF;
    }
    return number;
  }
  
  /**
   * Converting a given <code>long</code> into a <code>byte[]</code> value.
   * @param value The data to convert.
   * @return The <code>byte[]</code> representing the given value.
   */
  public static byte[] fromLong(long value) {
    return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
  }

  /**
   * Extracts a specific byte from given number starting at given position
   * @param number the number to extract byte from
   * @param pos the position from which to take 8 bits
   * @return specific byte from given number
   */
  public static byte getByte(long number, int pos) {
    number >>>= (Long.SIZE - ((pos + 1) * Byte.SIZE));
    return (byte) (number & 0xFF);
  }
  
  /**
   * Sets a specific byte in given number to requested input
   * @param number the number to be modified
   * @param b the byte to set into number
   * @param pos the position of the first bit to be modified
   * @return the modified number
   */
  public static long setByte(long number, byte b, int pos) {
    number &= ~(0xFL << Long.SIZE - ((pos + 1) * Byte.SIZE));
    long expanded = ((long) b) & 0xFF;
    number |= (expanded << Long.SIZE - ((pos + 1) * Byte.SIZE));
    return number;
  }

  /**
   * Gets the n'th bit in the given long value (usually 64-bit).
   * @param input The examine value.
   * @param pos The location in the bit value (msb to lsb).
   * @return True if the bit was on, false otherwise.
   */
  public static boolean getBit(long number, int pos) {
    // Compare result to 0 to spare 1 CPU unit.
    return ((number >>> (Long.SIZE - pos - 1)) & 1L) != 0;
  }

  /**
   * Sets the requested bit in the given long value (suualy 64-bit).
   * @param input The value to modify.
   * @param pos The position in the value to be modified.
   * @param value The modified version of the given value. 
   */
  public static long setBit(long input, int pos, boolean value) {
    // Create a mask covering only the single affected bit.
    long mask = (value ? 1L : 0L) << (Long.SIZE - pos - 1);
    // Keep all unrelated bits, and mask the last one.
    return (input & ~mask) | mask;
  }
}
