/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

import java.io.File;

/**
 * Main executable class.
 * 
 * @author dor
 */
public class SortFiles {

	/** Capacity of bounded-buffers (queues) used */
	public static final int BOUNDED_BUFFER_CAPACITY = 10;
	/** The amount of arguments the <code>main</code> method expects */
	public static final int ARGS_COUNT = 3;
	/** The index of the work dir in the main's arguments */
	public static final int DIRECTORY_ARG = 0;
	/** The index of the sorters' number in the main's arguments */
	public static final int NUM_SORTERS_ARG = 1;
	/** The index of the writers' number in the main's arguments */
	public static final int NUM_WRITERS_ARG = 2;
	
	/**
	 * Main method. Runs the sorting process. The general flow is:
	 *   1. Parse arguments, check for errors, etc.
	 *   2. Create the shared queues
	 *   3. Initialize the SortReader
	 *   4. Start the SortReader thread
	 *   5. Start the sorting threads
	 *   6. Start the writing threads
	 *   7. Join SortReader thread
	 *   8. Join sorting threads
	 *   9. Set the sorted queue as done (after all sorting threads joined)
	 *   10. Join writing threads
	 *   
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		// Check validity of the arguments
		if (args.length != ARGS_COUNT) {
			showUsage("Please supply exactly " + ARGS_COUNT + " arguments.");
			return;
		}
		int numSorters;
		int numWriters;
		File dir = new File(args[DIRECTORY_ARG]);
		// Check directory
		if (!dir.isDirectory()) {
			showUsage("Please supply a valid directory.");
			return;
		}
		// Check number of sorter
		try {
			numSorters = Integer.parseInt(args[NUM_SORTERS_ARG]);
		} catch (NumberFormatException e) {
			showUsage("Please supply a numeric number of sorters.");
			return;
		}
		// Check number of writers
		try {
			numWriters = Integer.parseInt(args[NUM_WRITERS_ARG]);
		} catch (NumberFormatException e) {
			showUsage("Please supply a numeric number of writers.");
			return;
		}
		// Create the shared queues
		BoundedBuffer<SortTask> unsorted = new BoundedBuffer<SortTask>(BOUNDED_BUFFER_CAPACITY);
		BoundedBuffer<SortTask> sorted = new BoundedBuffer<SortTask>(BOUNDED_BUFFER_CAPACITY);
		// Init the sort reader
		SortReader reader = SortReader.getInstance();
		reader.init(dir, unsorted);
		// Init the sorters and writers
		Thread[] sorters = new Thread[numSorters];
		for (int i = 0; i < sorters.length; ++i) {
			sorters[i] = new Thread(new Sorter(unsorted, sorted));
		}
		Thread[] writers = new Thread[numWriters];
		for (int i = 0; i < writers.length; ++i) {
			writers[i] = new Thread(new SortWriter(sorted));
		}
		// Start them all
		reader.start();
		for (int i = 0; i < sorters.length; ++i) {
			sorters[i].start();
		}
		for (int i = 0; i < writers.length; ++i) {
			writers[i].start();
		}
		// Wait for the reader to finish
		try {
			reader.join();
		} catch (InterruptedException e) {
			// Ignores. Assume the reader was done.
		}
		// Mark the unsorted queue as done.  Wait for all the sorters to finish
		unsorted.setDone();
		for (Thread sorter : sorters) {
			try {
	      sorter.join();
      } catch (InterruptedException e) {
      	// Ignores. Assume the sorter was done.
      }
    }
		// All sorters are done. Mark the sorted queue as done and wait for writers
		sorted.setDone();
		for (Thread writer : writers) {
			try {
	      writer.join();
      } catch (InterruptedException e) {
      	// Ignores. Assume the writer was done.
      }
    }
		System.out.println("Done writing sorted files. Goodbye :)");
	}

	/**
	 * Prints the error message given, and shows the usage line.
	 * @param message An error message to display
	 */
	private static void showUsage(String message) {
		System.err.println("Error: " + message);
		System.err.println("Usage: java SortFiles directory num-sorters num-writers");
	}
}
