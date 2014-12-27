/* 
 * Operating Systems - Exercise 2.
 * Student's Name: Dor Gross
 * Student's ID:   039344999
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Searches for a given string in a file, from a specific offset.
 * 
 * Note: According to what was written in the course forum:
 * http://moodle.idc.ac.il/mod/forum/discuss.php?d=10966&parent=15099
 * "You may assume that a pattern is a whole word, and that it appears as a 
 * whole word in the file" - therefore the class searches the pattern in a
 * buffer, assuming it's a word in the file. 
 * 
 * @author dor
 */
public class StringSearcher implements Runnable {

	/** The file to search the pattern in */
	private File inputFile;
	/** The offset inside the file to start the search from */
	private long offset;
	/** The size of chunk inside the file to search the pattern in */
	private long chunkSize;
	/** The maximal length of each buffer's read operation */
	private int bufferSize;
	/** The pattern to look for in the file */
	private String pattern;
	/** The number of occurrences of the pattern in the file */
	private int occurrences;
	
	/**
	 * Creates a new {@link StringSearcher} instance.
	 * @param file The file to read from
	 * @param offset The offset within the file to search from
	 * @param size The size of chunk to search in within the file
	 * @param pattern The pattern to look for
	 * @param buffSize The maximal length of each buffer's read operation
	 */
	public StringSearcher(File file, long offset, long size, 
			String pattern, int buffSize) {
		this.inputFile = file;
		this.offset = offset;
		this.chunkSize = size;
		this.bufferSize = buffSize;
		this.pattern = pattern;
		this.occurrences = 0;
	}

	/**
	 * @return The number of occurrences of the given pattern in the given file
	 */
	public int getCount() {
		return occurrences;
	}
	
	@Override
	public void run() {
		// Make sure the buffer length is not shorter than the length of the
		// given pattern
		if (bufferSize < pattern.length()) {
		  System.err.println("Couldn't search a pattern of length " +
		  		pattern.length() + " with a buffer of length " + bufferSize);
			return;
		}
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(inputFile, "r");
		} catch (FileNotFoundException e) {
			System.err.println("Error: Couldn't open the file: " + inputFile);
			return;
		}
		try {
			file.seek(offset);
			long leftToRead = chunkSize;
			// Starting with no know matched prefixes
			int matchedBefore = 0;
			// Reads a buffer, and scans it for the pattern
			while (leftToRead > 0) {
				// Make sure we never read more than the chunk's size
				int toRead = (leftToRead < bufferSize) ? (int)leftToRead : bufferSize;
				byte[] buffer = new byte[toRead];
				int readSize = file.read(buffer);
				if (readSize <= 0) {
					// Reached EOF
					return;
				}
				leftToRead -= readSize;
				matchedBefore = scanBuffer(buffer, readSize, matchedBefore);
			}
			// Handle a pattern between chunks
			if (matchedBefore > 0) {
				byte[] leftToCheck = new byte[pattern.length() - matchedBefore];
				if (file.read(leftToCheck) != leftToCheck.length) {
					// There's not enough data for a whole check
					return;
				}
				scanBuffer(leftToCheck, leftToCheck.length, matchedBefore);
			}
		} catch (IOException e) {
			System.err.println("Error while reading from file: " + e.getMessage());
		} finally {
			//  Make sure we close the random access file
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					System.err.println("Error: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Scans the given buffer for the given pattern. Increases the number of
	 * occurrences with each instance of the pattern in the buffer.
	 * If the given buffer ends with a prefix of the pattern, returns the length
	 * of that prefix. Otherwise, returns 0.
	 * @param buffer A buffer to scan for the pattern
	 * @param dataLen The length of the data to check
	 * @param assumeInitialMatch Start scanning the buffer with the assumption
	 * of already matched characters before (i.e., from previous buffers).
	 * @return Length of pattern's prefix in the given buffer's suffix.
	 */
	private int scanBuffer(byte[] buffer, int dataLen, int assumeInitialMatch) {
		int matched = assumeInitialMatch;
		boolean wordDoesntMatch = false;
		for (int i = 0; i < dataLen; ++i) {
			if (!Character.isLetter(buffer[i])) {
				// Proceed to the next word
				matched = 0;
				wordDoesntMatch = false;
				continue;
			}
			// Word doesn't match - should proceed to the next word
			if (wordDoesntMatch) {
				continue;
			}
			if (buffer[i] == pattern.charAt(matched)) {
				matched++;
				if (matched == pattern.length()) {
					// It's a match!
					occurrences++;
					matched = 0;
				}
			} else {
				wordDoesntMatch = true;
			}
		}
		return matched;
	}
	
	/**
	 * Searches for a string pattern in a file, while using several threads to do
	 * it in parallel.
	 * 
	 * Expects the following array of arguments as its input:
	 *  (1) Filename - to be scanned.
	 *  (2) Pattern (Word) - to look for.
	 *  (3) Threads# - The number of threads to use.
	 *  (4) Buffer Size - The max size of buffer to read in each read operation.
	 */
	public static void main(String[] args) {
		// Check the input arguments:
		// (1) Check the number of arguments
		if (args.length != 4) {
			System.err.println("Usage: " +
					"java StringSearcher file pattern threads buff-size");
			return;
		}
		String pattern = args[1];
		String filename = args[0];
		String threadNumStr = args[2];
		String buffSizeStr = args[3];
		// (2) Check the filename is a valid one
		File file = new File(filename);
		if (!file.exists() || !file.canRead()) {
			System.err.println("Error: Couldn't open the file: " + filename);
			return;
		}
		// (3) Check the number of threads and the length of the buffer are valid
		int threadsNum;
		int buffSize;
		try {
			threadsNum = Integer.parseInt(threadNumStr);
			if (threadsNum < 1) {
				System.err.println("Error: Threads' number should be positive!");
				return;
			}
		} catch (NumberFormatException e) {
			System.err.println(
					"Error: Illegal value given for threads' number: " + threadNumStr);
			return;
		}
		try {
			buffSize = Integer.parseInt(buffSizeStr);
			if (threadsNum < 1) {
				System.err.println("Error: Read buffer's size should be positive!");
				return;
			}
		} catch (NumberFormatException e) {
			System.err.println(
					"Error: Illegal value given for the buffer's size: " + buffSizeStr);
			return;
		}
		// Get the size of the file and the size of the chunks
		long totalSize = file.length();
		long chunkSize = totalSize / threadsNum +
				// Include file's tail
				((totalSize % threadsNum > 0) ? 1 : 0);
		// Initialize threads and searchers
		Thread[] threads = new Thread[threadsNum];
		StringSearcher[] searchers = new StringSearcher[threadsNum];
		for (int i = 0; i < threadsNum; ++i) {
			searchers[i] = new StringSearcher(file, i * chunkSize, chunkSize, 
					pattern, buffSize);
			threads[i] = new  Thread(searchers[i]);
		}
		// Run all threads, wait for them to complete
		long startTime = System.currentTimeMillis();
		for (Thread thread : threads) {
	    thread.start();
    }
		for (Thread thread : threads) {
	    try {
	    	thread.join();
	    } catch (InterruptedException e) {
	    	// Ignored: Interrupt assumes the thread is done (even if interrupted).
	    }
    }
		long endTime = System.currentTimeMillis();
		// Sum and print the results
		int counts = 0;
		for (StringSearcher searcher : searchers) {
	    counts += searcher.getCount();
    }
		System.out.println("Found " + counts + " occurrence(s) of the pattern " +
				"in the file. Runtime: " + (endTime - startTime) + " milliseconds.");
	}
}
