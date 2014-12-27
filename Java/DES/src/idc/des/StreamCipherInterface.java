package idc.des;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a symmetric cipher algorithm class which may encrypt or decrypt data.
 */
public interface StreamCipherInterface {

  /**
   * Encrypts the data from the given stream using the given key and writes the outcome
   * to the cipher stream. Continues until no more plain data is available.
   * 
   * @param plain The plain data to encrypt
   * @param key The key used for for encryption
   * @param cipher The encrypted data
   */
  public void encrypt(InputStream plain, InputStream key, OutputStream cipher) throws IOException;

  /**
   * Decrypts the cipher from the given stream using the given key and writes the outcome
   * to the plain stream. Continues until no more cipher data is available.
   * 
   * @param cipher The encrypted data to decrypt
   * @param key The key used for for encryption
   * @param plain The plain data
   */
  public void decrypt(InputStream cipher, InputStream key, OutputStream plain) throws IOException;

  /**
   * Verifies, using the given key, that the plain data is encrypted to the data available
   * in the cipher stream. Returns true if it does, false otherwise.
   * 
   * @param plain The plain data to encrypt
   * @param key The key used for for encryption
   * @param cipher The encrypted data
   */
  public boolean verify(InputStream plain, InputStream key, InputStream  cipher);
}
