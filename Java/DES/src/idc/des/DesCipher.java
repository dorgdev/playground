package idc.des;

import idc.des.DesConfig.BlockMode;
import idc.des.DesConfig.PlainFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DesCipher implements StreamCipherInterface {

  /** The length of a processed block (plain, cipher and key). */
  public static final int BLOCK_SIZE = 8;
  /** The value of the first padding byte. */
  public static final byte FIRST_PADDING_BYTE = (byte)0x80;
  /** The value of the trailing padding bytes. */
  public static final byte TRAIL_PADDING_BYTE = 0;

  /**
   * Create a new DesCipher from the given configuration.
   * @param config The configuration according to which the DES will operate.
   */
  public DesCipher(DesConfig config) {
    this.config = config;
    if (config.getDataCipher() == null) {
      this.dataCipher = new FeistelNetworkDataCipher();
    } else {
      this.dataCipher = config.getDataCipher();
    }
  }
  
  /**
   * Reads the plain and key, and writes the encrypted output to the cipher stream.
   * Will continue encrypting until the end of the stream is reached.
   * <b>Note:</b>Doesn't take care of closing the streams once it is done.
   */
  @Override
  public void encrypt(InputStream plain, InputStream key, OutputStream cipher) throws IOException {
    // Assumes Hex key file.
    key = new HexInputStream(key);
    if (config.getPlainFormat() == PlainFormat.RADIX64) {
      plain = new Base64InputStream(plain);
    }
    // Cipher is Radix64.
    cipher = new Base64OutputStream(cipher);

    // Prepare the process block and assisting values.
    byte[] block = new byte[BLOCK_SIZE];
    long plainVal = 0L;
    long keyVal = 0L; 
    long cipherVal = 0L;  // Reused for IV.
    boolean moreData = true;
    // Process the data till its end.
    while (moreData) {
      // Read the next plain block.
      moreData = readPlain(plain, block);
      plainVal = Utils.toLong(block);
      // Read the next key block.
      readKey(key, block);
      keyVal = Utils.toLong(block);
      // CBC requires XORing the former cipher/iv with the current plain.
      if (config.getBlockMode() == BlockMode.CBC) {
        plainVal ^= cipherVal;
      }
      cipherVal = dataCipher.encrypt(plainVal, keyVal);
      cipher.write(Utils.fromLong(cipherVal));
    }
    // Make sure we dump any final bits.
    ((Base64OutputStream)cipher).finish();
  }

  @Override
  public void decrypt(InputStream cipher, InputStream key, OutputStream plain) throws IOException {
    // Assumes Hex key file.
    key = new HexInputStream(key);
    // Assume Radix64 cipher file.
    cipher = new Base64InputStream(cipher);
    // Check whether the plain format should be Radix64.
    if (config.getPlainFormat() == PlainFormat.RADIX64) {
      plain = new Base64OutputStream(plain);
    }

    // Prepare the process block and assisting values.
    byte[] block = new byte[BLOCK_SIZE];
    long prevCipherVal = 0L;  // Reused for IV.
    long keyVal = 0L;
    long plainVal = 0L;
    // Process the data till its end.
    boolean notDone = readCipher(cipher, block);
    long cipherVal = Utils.toLong(block);
    while (notDone) {
      // Read the next key block.
      readKey(key, block);
      keyVal = Utils.toLong(block);
      // Decrypt the block.
      plainVal = dataCipher.decrypt(cipherVal, keyVal);
      // CBC requires XORing the former cipher/iv with the current plain. We check against
      // the tmpBlock since it starts with null before the first swap.
      if (config.getBlockMode() == BlockMode.CBC) {
        plainVal ^= prevCipherVal;
      }
      // Store the cipher buffer for future use.
      prevCipherVal = cipherVal;
      // Read the next cipher block.
      notDone = readCipher(cipher, block);
      cipherVal = Utils.toLong(block);
      // Check if there's more cipher to process, so we can write the plain buffer.
      if (notDone) {
        plain.write(Utils.fromLong(plainVal));
      }
    }
    // We've reached the end of the cipher. Process the padding and write the result.
    plain.write(cleanPadding(Utils.fromLong(plainVal)));
    // Make sure we finish the Radix64 wirintg properly.
    if (plain instanceof Base64OutputStream) {
      ((Base64OutputStream)plain).finish();
    }
  }

  @Override
  public boolean verify(InputStream plain, InputStream key, InputStream cipher) {
    try {
      // Assumes Hex key file.
      key = new HexInputStream(key);
      // Assume the cipher is Radix64.
      cipher = new Base64InputStream(cipher);
      // Check whether the plain format should be Radix64 as well.
      if (config.getPlainFormat() == PlainFormat.RADIX64) {
        plain = new Base64InputStream(plain);
      }
      // Prepare the process block and assisting values.
      byte[] block = new byte[BLOCK_SIZE];
      long plainVal = 0L;
      long keyVal = 0L;
      long cipherVal = 0L;
      // Start reading the plain, while there's still data to read, make sure 
      // that the cipher also has data available, and that the output of the 
      // encrypting the plain is cipher itself.
      boolean moreData = true;
      // Process the data till its end.
      while (moreData) {
        // Read the next plain block.
        moreData = readPlain(plain, block);
        plainVal = Utils.toLong(block);
        // Read the next key block.
        readKey(key, block);
        keyVal = Utils.toLong(block);
        // CBC requires XORing the former cipher/iv with the current plain.
        if (config.getBlockMode() == BlockMode.CBC) {
          plainVal ^= cipherVal;
        }
        // Calculate the expected result block.
        cipherVal = dataCipher.encrypt(plainVal, keyVal);
        // Now read the cipher block.
        if (!readCipher(cipher, block)) {
          return false;
        }
        // Compare the results.
        if (cipherVal != Utils.toLong(block)) {
          return false;
        }
      }
      // The plain stream should be empty now. Make sure that the cipher stream
      // is empty as well.
      if (cipher.read() != -1) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Reads as much data as possible from the given plain stream, and writes to plain buffer at
   * most plain's capacity. When the data is over, will pad the plain buffer with Des padding 
   * and return false. Otherwise will return true.
   * @param plainStream The plain stream.
   * @param plain The read data will be written to this buffer.
   * @return Whether more data should be expected.
   * @throws IOException In case of a problem reading from a stream.
   */
  private boolean readPlain(InputStream plainStream, byte[] plain) throws IOException {
    int readByte;
    int i = 0;
    for (; (i < plain.length) && ((readByte = plainStream.read()) >= 0); ++i) {
      plain[i] = (byte)readByte;
    }
    if (i == plain.length) {
      // We've reached the end of the buffer. No need for padding.
      return true;
    }
    // We need to pad the rest of the buffer.
    plain[i++] = FIRST_PADDING_BYTE;
    while (i < plain.length) {
      plain[i++] = TRAIL_PADDING_BYTE;
    }
    return false;
  }
  
  /**
   * Read the cipher from the input stream and stores the value in the given buffer. Assuming
   * the input is a block-length multiplication, if the first byte is available, the method
   * assumes the rest of the block should be available as well.
   * Returns true if the buffer was read successfully, or false if the end of stream was met.
   * <b>Note:</b> The input cipher buffer is not modified when returning flase.
   * @param cipherStream The cipher stream to read from.
   * @param cipher The cipher block to fill.
   * @return Whether a new buffer was read successfully.
   * @throws IOException In case of a problem reading from the stream.
   */
  private boolean readCipher(InputStream cipherStream, byte[] cipher) throws IOException {
    // First, check that the cipher has more data.
    int readByte = cipherStream.read();
    if (readByte == -1) {
      return false;
    }
    // We have data to read, read the buffer to its end.
    cipher[0] = (byte)readByte;
    for (int i = 1; i < cipher.length; ++i) {
      readByte = cipherStream.read();
      if (readByte == -1) {
        throw new IOException("Unexpected end of stream encountered.");
      }
      cipher[i] = (byte)readByte;
    }
    return true;
  }
  
  /**
   * Reads the key from the keyStream given, and stores it in the key buffer.
   * @param keyStream The key's source stream.
   * @param key The key buffer to fill.
   * @throws IOException In case of a problem reading from the stream.
   */
  private void readKey(InputStream keyStream, byte[] key) throws IOException {
    int readByte;
    for (int i = 0; i < key.length; ++i) {
      readByte = keyStream.read();
      if (readByte == -1) {
        throw new IOException("Unexpected end of key stream was reached.");
      }
      key[i] = (byte)readByte;
    }
  }
  
  /**
   * Recevies a padded buffer and cleans the data from its end, returning a clean buffer.
   * @param buffer The padded buffer to clean.
   * @return A clean version of the buffer, without the padding.
   * @throws IllegalArgumentException In case of an unpadded buffer given.
   */
  private byte[] cleanPadding(byte[] buffer) throws IllegalArgumentException {
    // Padding length could be (0,buffer-len], so final length is [0,buffer-len).
    int len = buffer.length - 1;
    // Make sure the buffer has a possible padding suffix.
    if ((buffer[len] != FIRST_PADDING_BYTE) && (buffer[len] != TRAIL_PADDING_BYTE)) {
      throw new IllegalArgumentException("Corrupted padded buffer received?");
    }
    // count the padding length.
    while (len > 0 && buffer[len] == TRAIL_PADDING_BYTE) {
      // Found a trailing padding, decrease the length.
      len--;
    }
    // No more trailing padding. The first padding byte should be found now.
    if (buffer[len] != FIRST_PADDING_BYTE) {
      throw new IllegalArgumentException("Buffer missing first padding byte?");
    }
    // Build the result buffer and return.
    byte[] result = new byte[len];
    for (int i = 0; i < len; ++i) {
      result[i] = buffer[i];
    }
    return result;
  }
  
  /** The configuration for the DES operation. */
  private DesConfig config;
  /** The Feistel network used internally for the DES operation. */
  private DataCipherInterface dataCipher;
}
