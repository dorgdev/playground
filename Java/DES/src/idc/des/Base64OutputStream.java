package idc.des;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes base64 output to a wrapped stream, from a regular binary write calls.
 */
public class Base64OutputStream extends OutputStream {

  /**
   * Constructs a new base64 input stream wrapping the given stream.
   * @param stream The wrapped stream.
   */
  public Base64OutputStream(OutputStream stream) {
    this.stream = stream;
    buffer = new byte[3];
    pos = 0;
    finished = false;
  }

  @Override
  public void write(int b) throws IOException {
    if (finished) {
      throw new IOException("Stream was already finished!");
    }
    buffer[pos++] = (byte)b;
    if (pos == buffer.length) {
      flushBuffer();
      pos = 0;
    }
  }

  @Override
  public void close() throws IOException {
    // Make sure we're done.
    finish();
    // Actually close the stream.
    stream.close();
    super.close();
  }

  /**
   * If there's any data left in the buffer, flush it.
   * <b>Note:</b>It's illegal calling "write()" after "finish()".
   * @throws IOException In case of a problem writing the data.
   */
  public void finish() throws IOException {
    if (finished) {
      return;
    }
    finished = true;
    // Flush any remainings by swapping with a temp buffer.
    if (pos > 0) {
      byte[] remaining = new byte[pos];
      for (int i = 0; i < pos; ++i) {
        remaining[i] = buffer[i];
      }
      buffer = remaining;
      flushBuffer();
    }
  }
  
  /**
   * Flushes the build buffer to the wrapped stream.
   * @throws IOException In case of a problem writing the data.
   */
  private void flushBuffer() throws IOException {
    try {
      char[] chars = Base64Converter.toBase64(buffer);
      for (char c : chars) {
        stream.write(c);
      }
    } catch (IllegalArgumentException e) {
      throw new IOException(e);
    }
  }
  
  /** The wrapped stream. */
  private OutputStream stream;
  /** Internal data buffering. */
  byte[] buffer;
  /** Next position for writing. */
  int pos;
  /** Indicates that "finish()" was called and no more writes are permitted. */
  boolean finished;
}
