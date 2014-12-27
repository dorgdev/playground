package idc.des;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class takes care of reading a Hexadecimal stream and output its binary content.
 */
public class HexInputStream extends InputStream {

  /** Mapping from a Hex character to its corresponding int value. */
  private static final Map<Character, Integer> hexToInt = new HashMap<Character, Integer>(16);
  static {
    char[] vals = new char[] {'0', '1', '2', '3', '4', '5', '6', '7',
                              '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    for (int i = 0; i < vals.length; i++) {
      hexToInt.put(vals[i], i);
    }
  }

  /**
   * Build a new hex stream wrapping another stream.
   * @param stream The wrapped stream.
   */
  public HexInputStream(InputStream stream) {
    this.stream = stream;
  }

  @Override
  public int read() throws IOException {
    // Read the most significant bits first.
    int msb = readSingleLetter();
    if (msb == -1) { 
      return -1;
    }
    // Least significant bits can't be EOS, as the stream will be illegal.
    int lsb = readSingleLetter();
    if (lsb == -1) {
      throw new IOException("Unexpected end of stream was reached.");
    }
    return (msb << 4) + lsb;
  }
  
  /**
   * Make sure we close the internal stream.
   * @throws IOException In case of a problem closing the internal stream.
   */
  @Override
  public void close() throws IOException {
    stream.close();
  }

  /**
   * Reads a single hex letter from the input, and returns a number in [0,15] matching the
   * hex value of the letter.
   * @return The int value of the read letter in [0,15].
   * @throws IOException In case of a problem reading from the wrapped input, or in case of
   * an invalid hex input.
   */
  private int readSingleLetter() throws IOException {
    int data = stream.read();
    while (data == ' ' || data == '\n') {  // Ignore whitespaces.
      data = stream.read();
    }
    // Check we didn't reach the end of the input.
    if (data == -1) {
      return data;
    }
    // Check it's a valid character.
    char letter = (char)data;
    if (hexToInt.containsKey(letter)) {
      return hexToInt.get(letter);
    }
    // Or its upper case version of it...
    char upper = Character.toUpperCase(letter);
    if (hexToInt.containsKey(upper)) {
      return hexToInt.get(upper);
    }
    throw new IOException("Unknown character found: '" + letter + "'");
  }
  
  /** The wrapped internal stream. */
  private InputStream stream;
}
