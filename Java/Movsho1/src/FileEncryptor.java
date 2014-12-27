import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Building Secure Applications - Programming exercise 1.
 * @author Dor Gross, 039344999 
 */
public class FileEncryptor {
	
	/** The size of the buffer used to read from a file (1 MB). */
	public static final int FILE_BUFF_SIZE = 1 << 20;
	/* Below are all the options' names available for the user. */
  public static final String OPT_SIG_ALIAS = "sigalias";
  public static final String OPT_SIG_PASS = "sigpass";
  public static final String OPT_DIGEST = "digest";
  public static final String OPT_SIG_ALG = "sigalg";
  public static final String OPT_KEY_ALIAS = "keyalias";
  public static final String OPT_KEY_PASS = "keypass";
  public static final String OPT_KEY_ALG = "keyalg";
  public static final String OPT_SYM_ALG = "symalg";
  public static final String OPT_BLOCK = "block";
  public static final String OPT_KEYSTORE = "keystore";
  public static final String OPT_KEYSTORE_PROV = "keystore_provider";
  public static final String OPT_SYM_KEY_PROV = "symkey_provider";
  public static final String OPT_DIGEST_PROV = "digest_provider";
  public static final String OPT_BLOCK_PROV = "block_provider";
  public static final String OPT_KEY_PROV = "key_provider";
  public static final String OPT_SIG_PROV = "sig_provider";
  /* Below are all the options' default values. */
  public static final String DEF_SIG_ALIAS = "Signature";
  public static final String DEF_DIGEST = "SHA1";
  public static final String DEF_SIG_ALG = "DSA";
  public static final String DEF_KEY_ALIAS = "AsymmetricKey";
  public static final String DEF_KEY_ALG = "RSA";
  public static final String DEF_SYM_ALG = "AES";
  public static final String DEF_BLOCK = "AES/CBC/PKCS5Padding";
  public static final String DEF_KEYSTORE = "JKS";
  public static final String DEF_KEYSTORE_PROV = "SUN";
  public static final String DEF_SYM_KEY_PROV = "SunJCE";
  public static final String DEF_DIGEST_PROV = "SUN";
  public static final String DEF_BLOCK_PROV = "SunJCE";
  public static final String DEF_KEY_PROV = "SunJCE";
  public static final String DEF_SIG_PROV = "SUN";
	
	/**
	 * A structured class for holding the different configuration used for
	 * encryption/decryption of files.
	 */
	public static class Configuration implements Serializable {
		/** Used for serialization verification. */
    private static final long serialVersionUID = -4596175223058505693L;
    
		/** The length of the cipher. */
		public byte[] signature;
		/** The IV used in the file encryption. */
		public byte[] iv;
		/** An encrypted key, used for encrypting the file. */
		public byte[] key;
		/** The encryption/decryption parameters. */
		public Properties params;
	}
	
	/**
	 * Main entry point for the program. Expects the following arguments:
	 * <ol>
	 *  <li>Mode. Should be "enc" or "dec" (for encryption/decryption modes).
	 *  <li>Input file.
	 *  <li>Output file.
	 *  <li>Configuration file.
	 *  <li>Key Store file.
	 *  <li>Key Store password.
	 * </ol>
	 */
	public static void main(String[] args) {
		// Check for valid usage.
		if (args.length < 6) {
			// Incorrect number of arguments.
			usage("Not enough argument specified.");
		} else if (!args[0].equals("dec") && !args[0].equals("enc")) {
			// Invalid work mode.
			usage("Invalid operation. Use \"dec\" or \"enc\".");
		}
    System.out.println("Validating given arguments.");
		boolean enc = args[0].equals("enc");
		File inFile = new File(args[1]);
		File outFile = new File(args[2]);
		File configFile = new File(args[3]);
		File keyStoreFile = new File(args[4]);
		char[] password = args[5].toCharArray();
    Properties prop = parseOptions(args);
		// Check the given files.
		if (!isReadable(inFile)) {
			usage("Bad input file: " + inFile.getAbsolutePath());
		} else if (!enc && !isReadable(configFile)) {
			usage("Bad config file: " + configFile.getAbsolutePath());
		} else if (!isReadable(keyStoreFile)) {
			usage("Bad keystore file: " + keyStoreFile.getAbsolutePath());
		}
		
		KeyStore keyStore = null;
		try {
			// Check the key store password is valid.
		  System.out.println("Loading keystore file.");
		  keyStore = KeyStore.getInstance(prop.getProperty(OPT_KEYSTORE, DEF_KEYSTORE),
		                                  prop.getProperty(OPT_KEYSTORE_PROV, DEF_KEYSTORE_PROV));
			keyStore.load(new FileInputStream(keyStoreFile), password);
		} catch (Exception e) {
			System.err.println("Couldn't load the key store: " + e.getMessage());
			System.exit(2);
		}
		try {
			// Perform the work itself.
			if (enc) {
				encrypt(inFile, outFile, configFile, keyStore, prop);
			} else {
				decrypt(inFile, outFile, configFile, keyStore, prop);
			}
		} catch (Exception e) {
			System.err.println("Operation failed: " + e.getMessage());
			System.exit(3);
		}
	}
	
	/**
	 * Parses the options given as arguments, and creates a Properties holding
	 * all the values.
	 * @param args The application arguments.
	 * @return A properties instance with the optional values.
	 */
	private static Properties parseOptions(String[] args) {
	  Properties prop = new Properties();
    prop.setProperty(OPT_KEY_PASS, args[5]);
    prop.setProperty(OPT_SIG_PASS, args[5]);
	   for (int i = 6; i < args.length; ++i) {
	     int eqIndex = args[i].indexOf('='); 
	     if (eqIndex < 1) {
	       continue;
	     }
	     prop.setProperty(args[i].substring(0, eqIndex), args[i].substring(eqIndex + 1));
	   }
	   return prop;
	}
	
	/**
	 * Checks if the given file is readable.
	 * @param file The file to check.
	 * @return True iff the file exists, it is not a  directory, and it's 
	 * 				 readable by the user.
	 */
	public static boolean isReadable(File file) {
		return (file.exists() && !file.isDirectory() && file.canRead());
	}
	/**
	 * Prints the program's usage and exits with an error return code.
	 * @param errorMsg An error message describing the cause for the failure.
	 */
	public static void usage(String errorMsg) {
		StringBuilder msg = new StringBuilder();
		msg.append("Error: ").append(errorMsg).append("\n\n")
		    .append("Usage: \n")
		    .append("  java FileEncryptor <enc|dec> <source> <target> <config> ")
		    .append("<keystore> <keystore password> [[option=value] ...]\n\n")
		    .append("Valid options: \n")
        .append(" sigalias: The signature-key-entry alias.\n")
        .append(" sigpass:  Specific password for the signature-key-entry.\n")
        .append(" digest:   The algorithm used for plain data digestion.\n")
        .append(" sigalg:   The algorithm to use for signing.\n")
        .append(" keyalias: The key-key-entry alias.\n")
        .append(" keypass:  Specific password for the key-key-entry.\n")
        .append(" keyalg:   The algorithm to use for encrypting the key.\n")
		    .append(" symalg:   The symmetric algorithm to use.\n")
		    .append(" block:    The block encryption type to use.\n")
		    .append(" keystore: The type of keystore used.\n")
		    .append("\n")
        .append(" keystore_provider: An optional provider for the keystore.\n")
        .append(" symkey_provider:   An optional provider for the sym key gen.\n")
        .append(" digest_provider:   An optional provider for the digest algo.\n")
        .append(" block_provider:    An optional provider for the block enc.\n")
        .append(" key_provider:      An optional provider for the key cipher.\n")
        .append(" sig_provider:      An optional provider for the signature.\n")
		    .append("\n")
		    .append("Note: When decrypting, given values override the stored ones.");
		System.err.println(msg.toString());
		System.exit(1);
	}
	
	/**
	 * Encrypts the given input file, and writes the encrypted data to the given
	 * output file. Writes the encryption configuration to the configuration file
	 * while using the given key store for its private and public key.
	 * @param inFile The file to encrypt.
	 * @param outFile The encrypted file to generate.
	 * @param configFile The configuration file to generate.
	 * @param keyStore A KeyStore holding the asymmetric keys for the job.
	 * @param prop The application's properties.
	 */
	public static void encrypt(File inFile, File outFile, File configFile, 
			KeyStore keyStore, Properties prop) throws Exception {
    System.out.println("Starting encryption mode.");
    
		Configuration config = new Configuration();
		// (1) Get all the required keys.
    System.out.println("Generating/reading keys.");
		// Generate the symmetric key (for the content encryption).
		KeyGenerator keyGen = KeyGenerator.getInstance(
		    prop.getProperty(OPT_SYM_ALG, DEF_SYM_ALG),
		    prop.getProperty(OPT_SYM_KEY_PROV, DEF_SYM_KEY_PROV));
		SecretKey symmetricKey = keyGen.generateKey();
		// Read the asymmetric key (for the symmetric key encryption).
		KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
        keyStore.getEntry(prop.getProperty(OPT_KEY_ALIAS, DEF_KEY_ALIAS),
        		new KeyStore.PasswordProtection(prop.getProperty(OPT_KEY_PASS).toCharArray()));
    PublicKey keyEncPublicKey = pkEntry.getCertificate().getPublicKey();
		// Read the digital signature key (for the digestion verification).
		pkEntry = (KeyStore.PrivateKeyEntry)
		    keyStore.getEntry(prop.getProperty(OPT_SIG_ALIAS, DEF_SIG_ALIAS),
		        new KeyStore.PasswordProtection(prop.getProperty(OPT_SIG_PASS).toCharArray()));
		PrivateKey signaturePrivateKey = pkEntry.getPrivateKey();
		// Clean the properties from the passwords (SHOULD NOT be saved to disk!!).
    prop.remove(OPT_KEY_PASS);
    prop.remove(OPT_SIG_PASS);

    // (2) Prepare the digestion instance.
		MessageDigest md = MessageDigest.getInstance(
		    prop.getProperty(OPT_DIGEST, DEF_DIGEST),
		    prop.getProperty(OPT_DIGEST_PROV, DEF_DIGEST_PROV));
		
		// (3) Prepare the cipher to be used for the encryption itself.
		Cipher blockCipher = Cipher.getInstance(prop.getProperty(OPT_BLOCK, DEF_BLOCK),
		                                        prop.getProperty(OPT_BLOCK_PROV, DEF_BLOCK_PROV));
		blockCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
		config.iv = blockCipher.getIV();
		
		// (4) Read the file content, and digest its content while encrypting it
		//     and writing it to a file.
    System.out.println("Encrypting the file content.");
		byte[] readBuff = new byte[FILE_BUFF_SIZE];
		FileInputStream fis = new FileInputStream(inFile);
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(outFile), blockCipher);
		int len;
		while ((len = fis.read(readBuff)) > 0) {
			md.update(readBuff, 0, len);
			cos.write(readBuff, 0, len);
			len = fis.read(readBuff);
		}
		fis.close();
		cos.flush();
		cos.close();
		
		// (5) Encrypt the symmetric key and store it.
    System.out.println("Encrypting the symmetric key.");
		Cipher keyCipher = Cipher.getInstance(prop.getProperty(OPT_KEY_ALG, DEF_KEY_ALG),
		                                      prop.getProperty(OPT_KEY_PROV, DEF_KEY_PROV));
		keyCipher.init(Cipher.ENCRYPT_MODE, keyEncPublicKey);
		keyCipher.update(symmetricKey.getEncoded());
		config.key = keyCipher.doFinal();
		
		
		// (6) Sign the digest value and store the signature.
		System.out.println("Signing the plain data.");
		byte[] digest = md.digest();
		Signature signature = Signature.getInstance(prop.getProperty(OPT_SIG_ALG, DEF_SIG_ALG),
		                                            prop.getProperty(OPT_SIG_PROV, DEF_SIG_PROV));
		signature.initSign(signaturePrivateKey);
		signature.update(digest);
		config.signature = signature.sign();
		
		// (7) Write the configuration to a file.
		System.out.println("Saving configuration to file.");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(configFile));
		config.params = prop;
		oos.writeObject(config);
		oos.close();

		// Done.
    System.out.println("Encryption process done successfully!");
	}
	
	/**
	 * Decrypts the given input file, and writes the decrypted data to the given
	 * output file. Reads the encryption configuration from the configuration
	 * file while using the given key store for its private and public key.
	 * @param inFile The encrypted file to read.
	 * @param outFile The decrypted file to generate.
	 * @param configFile The configuration file to use for the decryption.
	 * @param keyStore A KeyStore holding the asymmetric keys for the job.
	 * @param prop The application's properties.
	 */
	public static void decrypt(File inFile, File outFile, File configFile, 
			KeyStore keyStore, Properties prop) throws Exception {
	  System.out.println("Starting decryption mode.");
		// (1) Read the configuration from the file.
    System.out.println("Reading/validating stored configuration.");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile));
		Configuration config = (Configuration)ois.readObject();
		ois.close();
		for (Object propKey : config.params.keySet()) {
		  if (prop.containsKey(propKey)) {
		    continue;
		  }
		  prop.put(propKey, config.params.get(propKey));
		}
		
		// (2) Get the required keys from the key store.
    System.out.println("Reading keys from keystore.");
		// Read the asymmetric key (for the symmetric key encryption).
		KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
        keyStore.getEntry(prop.getProperty(OPT_KEY_ALIAS, DEF_KEY_ALIAS),
        		new KeyStore.PasswordProtection(prop.getProperty(OPT_KEY_PASS).toCharArray()));
    PrivateKey keyEncPrivateKey = pkEntry.getPrivateKey();
		// Read the digital signature key (for the digestion verification).
		pkEntry = (KeyStore.PrivateKeyEntry)
		    keyStore.getEntry(prop.getProperty(OPT_SIG_ALIAS, DEF_SIG_ALIAS),
		        new KeyStore.PasswordProtection(prop.getProperty(OPT_SIG_PASS).toCharArray()));
		PublicKey signaturePublicKey = pkEntry.getCertificate().getPublicKey();

		// (3) Decrypt the symmetric key, so we could use it for the file reading.
    System.out.println("Decrypting symmetric key.");
		Cipher keyCipher = Cipher.getInstance(prop.getProperty(OPT_KEY_ALG, DEF_KEY_ALG),
		                                      prop.getProperty(OPT_KEY_PROV, DEF_KEY_PROV));
		keyCipher.init(Cipher.DECRYPT_MODE, keyEncPrivateKey);
		byte[] keyBytes = keyCipher.doFinal(config.key);
		
		// (4) Build the symmetric key from the decrypted bytes.
		SecretKeySpec symmetricKey =
		    new SecretKeySpec(keyBytes, prop.getProperty(OPT_SYM_ALG, DEF_SYM_ALG));
		
		// (5) Read the data from the source file while digesting its content.
    System.out.println("Decrypting the file content.");
		MessageDigest md = MessageDigest.getInstance(
		    prop.getProperty(OPT_DIGEST, DEF_DIGEST),
		    prop.getProperty(OPT_DIGEST_PROV, DEF_DIGEST_PROV));
		Cipher blockCipher = Cipher.getInstance(prop.getProperty(OPT_BLOCK, DEF_BLOCK),
		                                        prop.getProperty(OPT_BLOCK_PROV, DEF_BLOCK_PROV));
		blockCipher.init(Cipher.DECRYPT_MODE, symmetricKey, new IvParameterSpec(config.iv));
		FileInputStream fis = new FileInputStream(inFile);
		CipherInputStream cis = new CipherInputStream(fis, blockCipher);
		FileOutputStream fos = new FileOutputStream(outFile);

		byte[] readBuff = new byte[FILE_BUFF_SIZE];
		int len;
		while ((len = cis.read(readBuff))> 0) {
			md.update(readBuff, 0, len);
			fos.write(readBuff, 0, len);
		}
		cis.close();
		fos.close();
		byte[] digest = md.digest();
		
		// (6) Validate the signed digest.
    System.out.println("Validating signed data.");
		Signature signature = Signature.getInstance(prop.getProperty(OPT_SIG_ALG, DEF_SIG_ALG),
		                                            prop.getProperty(OPT_SIG_PROV, DEF_SIG_PROV));
		signature.initVerify(signaturePublicKey);
		signature.update(digest);
		boolean verified = signature.verify(config.signature);
		
		// (7) Delete the output file in case the signature could not be verified
		//     so we won't leave garbage behind.
		if (!verified) {
			outFile.delete();
			System.err.println("Failed to verify the signature!");
		} else {
	    System.out.println("Decryption process done successfully!");
		}
	}
}
