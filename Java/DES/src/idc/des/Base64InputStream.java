package idc.des;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an input which is base64 decoded stream, and outputs its binary value.
 */
public class Base64InputStream extends InputStream {

  /**
   * Constructs a new base64 input stream wrapping the given stream.
   * @param stream The wrapped stream.
   */
  public Base64InputStream(InputStream stream) {
    this.stream = stream;
    this.data = null;
    this.pos = 0;
  }
  
  @Override
  public int read() throws IOException {
    if (pos % 3 == 0) {
      pos = 0;
      char[] chars = new char[4];
      int val = stream.read();
      if (val == -1) {
        return -1;
      }
      chars[0] = (char)val;
      chars[1] = nextChar();
      chars[2] = nextChar();
      chars[3] = nextChar();
      try {
        data = Base64Converter.fromBase64(chars);
      } catch (IllegalArgumentException e) {
        throw new IOException(e);
      }
    }
    if (pos >= data.length) {
      return -1;
    }
    // Make sure the values returns are in [0, 255].
    return data[pos++] & 0xff;
  }

  @Override
  public void close() throws IOException {
    stream.close();
    super.close();
  }

  /**
   * Reads the next characater from the wrapped stream, and makes sure the end of
   * stream was not yet reached.
   * @return The next character from the wrapped stream.
   * @throws IOException In case of problems reading from the stream.
   */
  private char nextChar() throws IOException {
    int readValue = stream.read();
    if (readValue == -1) {
      throw new IOException("Unexpected end of stream reached!");
    }
    return (char)readValue;
  }
  
  /** The wrapped stream. */
  private InputStream stream;
  /** The current in-memory buffer. */
  byte[] data;
  /** The position of the next character to read from the internal buffer. */
  int pos;
}
