
public class Enc {

  public enum NetworkEncryptionType { NO_ENCRYPTION, SSL, IPSEC }
  public enum BlockCipherType { CBC, OFB, ECB }
  public enum MinKeyLength { KEY_64, KEY_128, KEY_192, KEY_256 }
  
  public class EncryptionOptions {
    // ...
    
    NetworkEncryptionType networkEncType;
    BlockCipherType blockCipherType;
    MinKeyLength minKeyLen;
  }
  
  public class FileTransfer {
    // ...
    EncryptionOptions encOptions;
  }

  public class ImageDownloader {
    // ...
    EncryptionOptions encOptions;
  }

  public class MessageSender {
    // ...
    EncryptionOptions encOptions;
  }
}
