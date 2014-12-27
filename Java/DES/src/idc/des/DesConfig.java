package idc.des;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * Defines the configuration needed for the DES cipher mechanism.
 */
public class DesConfig {

  /** The default comment inserted into a confugration file. */
  public static final String DEFAULT_CONFIG_COMMENT = 
      "This file was created automatically for DES configuration.";
  /** The config's file tag for specifying the DES operation. */
  public static final String OP_TAG = "op";
  /** The config's file tag for specifying plain input format. */
  public static final String FORMAT_TAG = "format";
  /** The config's file tag for specifying the block mode. */
  public static final String MODE_TAG = "mode";

  /** Helper enum for handling the DES operation modes. */
  public enum Op {
    ENCRYPT, DECRYPT, VERIFY
  }

  /** Helper enum for handling the plain formats. */
  public enum PlainFormat {
    ASCII, RADIX64
  }

  /** Helper enu, for handling the block modes. */
  public enum BlockMode {
    CBC, ECB
  }

  /**
   * Reads the DES configuration from the input file, and returns a valid instance
   * holding the values read.
   * @param reader The reading stream.
   * @return A valid <code>DesConfig</code> instance.
   * @throws IOException In case of problems reading from the stream.
   * @throws IllegalArgumentException In case of malformed configuration.
   */
  public static DesConfig readConfig(Reader reader) throws IOException, IllegalArgumentException {
    Properties properties = new Properties();
    properties.load(reader);

    // Validate the content of the configuration.
    if (!properties.containsKey(OP_TAG)) {
      throw new IllegalArgumentException("Configuration is missing a tag: " + OP_TAG);
    } else if (!properties.containsKey(FORMAT_TAG)) {
      throw new IllegalArgumentException("Configuration is missing a tag: " + FORMAT_TAG);
    } else if (!properties.containsKey(MODE_TAG)) {
      throw new IllegalArgumentException("Configuration is missing a tag: " + MODE_TAG);
    }
    // Initialize the values.
    Op op = Op.valueOf(properties.getProperty(OP_TAG));
    PlainFormat format = PlainFormat.valueOf(properties.getProperty(FORMAT_TAG));
    BlockMode mode = BlockMode.valueOf(properties.getProperty(MODE_TAG));
    
    return new DesConfig(op, format, mode);
  }

  /**
   * Creating a <code>DesConfig</code> instance with the default values:
   *  Operation:    Encrypt
   *  Plain Format: ASCII
   *  Block Mode:   CBC
   */
  public DesConfig() {
    this(Op.ENCRYPT, PlainFormat.ASCII, BlockMode.CBC);
  }
  
  /**
   * Constructing a new <code>DesConfig</code> instance with its initial values.
   * @param op Configured DES operation.
   * @param plainFormat Configured plain format.
   * @param blockMode Configured block mode.
   */
  public DesConfig(Op op, PlainFormat plainFormat, BlockMode blockMode) {
    this.op = op;
    this.plainFormat = plainFormat;
    this.blockMode = blockMode;
    this.dataCipher = null;
  }

  public void writeConfig(Writer writer) throws IOException {
    Properties properties = new Properties();
    // Prepare the properties for writing.
    properties.put(OP_TAG, op.name());
    properties.put(FORMAT_TAG, plainFormat.name());
    properties.put(MODE_TAG, blockMode.name());

    // Write them to the output.
    properties.store(writer, DEFAULT_CONFIG_COMMENT);
  }

  /**
   * @return The configured DES operation.
   */
  public Op getOp() {
    return op;
  }
  
  /**
   * @return The configured plain format.
   */
  public PlainFormat getPlainFormat() {
    return plainFormat;
  }

  /**
   * @return The configured block mode.
   */
  public BlockMode getBlockMode() {
    return blockMode;
  }
  
  /**
   * @return The <code>DataCipherInterface</code> to use (for testing).
   */
  public DataCipherInterface getDataCipher() {
    return dataCipher;
  }
  
  /**
   * Sets the <code>DataCipherInterface</code> to use for tests.
   * @param dataCipher The instance to use.
   */
  public void setDataCipher(DataCipherInterface dataCipher) {
    this.dataCipher = dataCipher;
  }
  
  /** The configured DES operation. */
  private Op op;
  /** The configured plain format. */
  private PlainFormat plainFormat;
  /** The configured block mode. */
  private BlockMode blockMode;
  /** A possible <code>DataCipherInterface</code> to use internally (for testing). */
  private DataCipherInterface dataCipher;
}
