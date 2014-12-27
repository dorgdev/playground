package idc.des;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main class for running DES encryption/decryption/verification.
 */
public class Main {

  // Some default values for the input files.
  /** A default filename for the plain input/output. */
  public static final String DEFAULT_PLAIN_FILE = "p.txt";
  /** A default filename for the key input */
  public static final String DEFAULT_KEY_FILE = "k.txt";
  /** A default filename for the cipher input/output. */
  public static final String DEFAULT_CIPHER_FILE = "c.txt";
  /** A default filename for the configuration input. */
  public static final String DEFAULT_CONFIG_FILE = "cfg.txt";
  
  /**
   * Main method. Requires the following arguments:
   *  1. Input filename.
   *  2. Output filename (second input for verify).
   *  3. Key filename.
   *  4. Configuration filename.
   * <b>Note:</b>The order of the parameters is significant.
   * @param args The program's arguments.
   */
  public static void main(String[] args) {
    // Handle arguments.
    boolean useDefault = false;
    if (args.length == 0) {
      System.out.println("Using the default values: " + DEFAULT_PLAIN_FILE + ", " +
          DEFAULT_CIPHER_FILE + ", " + DEFAULT_KEY_FILE + " and " + DEFAULT_CONFIG_FILE);
      useDefault = true;
    } else if (args.length != 4) {
      usageAndExit(1, "Please use exactly 4 arguments for the program!");
      return;  // Unneceesary. For safety only.
    }
    String filename1 = useDefault ? DEFAULT_PLAIN_FILE : args[0];
    String filename2 = useDefault ? DEFAULT_CIPHER_FILE : args[1];
    String keyFilename = useDefault ? DEFAULT_KEY_FILE : args[2];
    String configFilename = useDefault ? DEFAULT_CONFIG_FILE : args[3];

    InputStream key = null;
    InputStream inFile1 = null;
    InputStream inFile2 = null;
    OutputStream outFile = null;
    try {
      // Read the configuration.
      DesConfig config = DesConfig.readConfig(new FileReader(configFilename));
      // Open the key streams.
      key = new FileInputStream(keyFilename);
      // Create the DES cipher.
      DesCipher des = new DesCipher(config);
      // Peform the required operation.
      switch (config.getOp()) {
        case ENCRYPT:
          System.out.println("Encrypting...");
          inFile1 = new FileInputStream(filename1);
          outFile = new FileOutputStream(filename2);
          des.encrypt(inFile1, key, outFile);
          System.out.println("Done!");
          break;
        case DECRYPT:
          System.out.println("Decrypting...");
          inFile1 = new FileInputStream(useDefault ? DEFAULT_CIPHER_FILE : filename1);
          outFile = new FileOutputStream(useDefault ? DEFAULT_PLAIN_FILE : filename2);
          des.decrypt(inFile1, key, outFile);
          System.out.println("Done!");
          break;
        case VERIFY:
          System.out.println("Verifying...");
          inFile1 = new FileInputStream(filename1);
          inFile2 = new FileInputStream(filename2);
          if (des.verify(inFile1, key, inFile2)) {
            System.out.println("Verified successfully!");
          } else {
            System.out.println("Verification failed!");
          }
          break;
        default:
          throw new IllegalArgumentException("Unknown DES operation set: " + config.getOp());
      }
    } catch (Exception e) {
      e.printStackTrace();
      usageAndExit(2, e.getMessage());
    } finally {
      safeCloseInputStream(key);
      safeCloseInputStream(inFile1);
      safeCloseInputStream(inFile2);
      safeCloseOutputStream(outFile);
    }
  }

  /**
   * A helper method used for delivering failures and exit.
   * @param errorCode The error code to return (differentiating between errors).
   * @param msg The error message to show along with the usage.
   */
  private static void usageAndExit(int errorCode, String msg) {
    System.err.println("\nError: " + msg + "\n");
    System.err.println("Usage: java " + Main.class.getCanonicalName());
    System.out.println("   or: java " + Main.class.getCanonicalName() + 
        " <input file> <output file | second input file> <key file> <config file>");
    System.exit(errorCode);
  }

  /**
   * Tries closing a given stream safetly. 
   * @param stream The stream to close. Null is a valid input.
   */
  private static void safeCloseInputStream(InputStream stream) {
    if (stream == null) {
      return;
    }
    try {
      stream.close();
    } catch (IOException e) {
      // Nothing much to do here...
    }
  }

  /**
   * Tries closing a given stream safetly. 
   * @param stream The stream to close. Null is a valid input.
   */
  private static void safeCloseOutputStream(OutputStream stream) {
    if (stream == null) {
      return;
    }
    try {
      stream.close();
    } catch (IOException e) {
      // Nothing much to do here...
    }
  }
}
