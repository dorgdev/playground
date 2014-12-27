package idc.des;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that takes care of converting Base64 to bytes and vice versa.
 */
public final class Base64Converter {

  /** Mapping from int (index) to a Base64 char. */
  private static char[] intToChar = new char[] {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '+', '/'
  };
  
  /** Mapping from a Base64 char to its value. */
  private static Map<Character, Integer> charToInt = new HashMap<Character, Integer>();
  static {
    for (int i = 0; i < intToChar.length; ++i) {
      charToInt.put(intToChar[i], i);
    }
  }

  /** The padding character. */
  private static final char PADDING = '=';
  
  /**
   * Converts the given data into a base64.
   * <b>Note:</b>Will require O(data.length) space for the returned value. 
   * @param data The data to convert to base64.
   * @return The base64 version of the given data.
   */
  public static char[] toBase64(byte[] data) {
    // Simple check for edge cases.
    if (data == null || data.length == 0) {
      return new char[0];
    }
    int cur = 0;
    StringBuilder builder = new StringBuilder();
    while (cur < data.length) {
      int val = data[cur++] & 0xff;
      // First letter.
      builder.append(intToChar[fixNegativeValue(val >> 2)]);
      // Second letter.
      val = (val % 4) << 8;
      if (cur == data.length) {
        builder.append(intToChar[fixNegativeValue(val >> 4)]);
        builder.append(PADDING);
        builder.append(PADDING);
        break;
      }
      val += data[cur++] & 0xff;
      builder.append(intToChar[fixNegativeValue(val >> 4)]);
      // third latter.
      val = (val % 16) << 8;
      if (cur == data.length) {
        builder.append(intToChar[fixNegativeValue(val >> 6)]);
        builder.append(PADDING);
        break;
      }
      val += data[cur++] & 0xff;
      builder.append(intToChar[fixNegativeValue(val >> 6)]);
      // Forth latter.
      builder.append(intToChar[fixNegativeValue(val % 64)]);
    }
    return builder.toString().toCharArray();
  }
  
  /**
   * Converts the given base64 data into binary data.
   * <b>Note:</b>Will require O(data.length) space for the returned value.
   * @param data The base64 data to convert to binary.
   * @return The binart decode data.
   * @throws IllegalArgumentException In case of invalid input (not base64 format).
   */
  public static byte[] fromBase64(char[] data) throws IllegalArgumentException {
    // Simple check for edge cases.
    if (data == null || data.length == 0) {
      return new byte[0];
    }
    if (data.length % 4 != 0) {
      // Invalid base64 length.
      throw new IllegalArgumentException("Illegal data length: " + data.length);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int i = 0;
    while (i < data.length) {
      int numBytes = 3;
      // First letter.
      char c = data[i++];
      validBase64Char(c, false);
      int val = charToInt.get(c) << 18;
      // Second letter.
      c = data[i++];
      validBase64Char(c, false);
      val += charToInt.get(c) << 12;
      // Third letter.
      c = data[i++];
      validBase64Char(c, true);
      if (c == PADDING) {
        // Some verifications.
        if (data[i++] != PADDING || i != data.length) {
          throw new IllegalArgumentException("Found invalid padding.");
        }
        numBytes = 1;
      } else {
        val += charToInt.get(c) << 6;
        // Forth letter.
        c = data[i++];
        validBase64Char(c, true);
        if (c == PADDING) {
          // Some verifications.
          if (i != data.length) {
            throw new IllegalArgumentException("Found invalid padding.");
          }
          numBytes = 2;
        } else {
          val += charToInt.get(c);
        }
      }
      // Now translate the number back to bytes.
      int mod = 1 << 16;
      while (numBytes-- > 0) {
        baos.write(val / mod);
        val %= mod;
        mod >>= 8;
      }
    }
    return baos.toByteArray();
  }
  
  /**
   * Makes sure the given character is a valid base64 format letter.
   * @param c The character to check
   */
  private static void validBase64Char(char c, boolean allowPadding) {
    if (c == PADDING) {
      if (allowPadding) {
        return;
      }
      throw new IllegalArgumentException("Found invalid padding.");
    }
    if (!charToInt.containsKey(c)) {
      throw new IllegalArgumentException("Invalid character found: " + c);
    }
  }

  /**
   * When a negative value is given, fix it and return the result.
   * @param v A possible negative value.
   * @return The positive version of the value.
   */
  private static int fixNegativeValue(int v) {
    if (v >= 0) {
      return v;
    }
    return v + intToChar.length;
  }
  
  /** Private CTOR to prevent instantiation. */
  private Base64Converter() {}
  
}
