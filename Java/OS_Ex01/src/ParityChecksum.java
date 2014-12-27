import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * OS exercise 1.
 * Name: Dor Gross
 * ID:   039344999
 */

/**
 * Performs a parity checksum over a file.
 * 
 * @author dor
 */
public class ParityChecksum {

	/** Hexadecimal values list */
	private final char[] HEX_VALUES = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };	
	/** The length of the checksum (in bytes) */
	private final int CHECKSUM_LEN = 16;
	
	/** The file on which the class works */
	private File file;
	/** The length of the file's reading buffer */
	private int bufferSize;
	/** The final checksum calculated */
	private byte[] finalChecksum;
	
	/**
	 * Constructs a new {@link ParityChecksum} class.
	 * @param file The file which the class will work on.
	 * @param bufferSize The length of the buffer read from the file.
	 */
	public ParityChecksum(File file, int bufferSize) {
		this.file = file;
		this.bufferSize = bufferSize;
		this.finalChecksum = new byte[CHECKSUM_LEN];
	}
	
	/**
	 * Calculates the checksum of the file.
	 * 
	 * @throws IOException In case of an IOException while reading the file.
	 */
	public void calcChecksum() throws IOException {
		byte[] bytes = new byte[bufferSize];
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			int checksumIndex = 0;
			int readBytes = in.read(bytes);
			while (readBytes != -1) {
				// Go over the data and XOR it with known checksum so far
				for (int i = 0; i < readBytes; ++i) {
					finalChecksum[checksumIndex++] ^= bytes[i];
					// Make sure we update the right byte in the final checksum
					if (checksumIndex == finalChecksum.length) {
						checksumIndex = 0;
					}
				}
				// Try to read the next buffer from the file
				readBytes = in.read(bytes);
			}
		} finally {
			// Make sure we close the input stream properly
			if (in != null) {
				in.close();
			}
		}
	}
	
	/**
	 * @return A String representation of the calculated checksum.
	 */
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < finalChecksum.length; ++i) {
			int value = finalChecksum[i];
			// Byte's values are [-128, 127], we will use positive values only
			if (value < 0) {
				value += 256;
			}
			str.append(HEX_VALUES[value / 16]).append(HEX_VALUES[value % 16]);
		}
		return str.toString();
	}
	
	/**
	 * Main method.
	 * 
	 * @param args A list containing the name of the file to checksum and the
	 *        length of the checksum buffer.
	 */
	public static void main(String[] args) {
		// Check for correct number of argument
		if (args.length != 2) {
			System.err.println(
					"Usage: java ParityChecksum <filename> <buffer length>");
			return;
		}
		try {
			// Make sure the given file exists
			File file = new File(args[0]);
			if (!file.exists()) {
				throw new IllegalArgumentException(
						"File not found: " + args[0]);
			}
			// Parse the length of the buffer to be used
			int length = Integer.parseInt(args[1]);
			// Run the checksum and calculate its runtime
			ParityChecksum pc = new ParityChecksum(file, length);
			long runtime = System.currentTimeMillis();
			pc.calcChecksum();
			runtime = System.currentTimeMillis() - runtime;
			// Print the result
			System.out.println(pc.toString());
			System.out.println("Total time: " + runtime + " milliseconds");
		} catch (IllegalArgumentException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
