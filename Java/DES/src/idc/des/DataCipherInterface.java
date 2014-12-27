package idc.des;

/**
 * Describes a class which handles encrypting and decrypting data.
 */
public interface DataCipherInterface {

  /**
   * Encrypts a given block of data using the given key, and returns encrypted data.
   * @param plain The plain data to encrypt
   * @param key The key used for the encryption
   * @return The encrypted data
   */
  public long encrypt(long plain, long key); 
  
  /**
   * Decrypts a given block of cipher data using the given key, and returns plain data.
   * @param plain The cipher data to decrypt
   * @param key The key used for the decryption
   * @return The plain data
   */
  public long decrypt(long cipher, long key); 
}
