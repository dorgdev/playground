/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This thread is responsible to read all '.unsorted' files from a given directory.
 * For each file, it creates a <code>SortTask</code> object, and enqueues the task to the unsorted queue.
 * 
 * This class is a singleton: There can be at most one instance of this class in the whole
 * system. To achieve this, the constructor of this class is set as <code>private</code>,
 * so only the code in this class may call it. Also, the static method <code>getInstance</code>
 * is defined to allow global access to the only instance.
 */
public class SortReader extends Thread {

	/** The extension of unsorted integers' files */
	public static final String UNSORTED_EXTENSION = ".unsorted";
	
	/** The directory the reader will look the file's in */
	private File directory;
	/** The queue holding all the <code>SortTask</code> instances */
	private BoundedBuffer<SortTask> queue;
	
	/**
	 * Private constructor - forces any creation of instances of this class
	 * to be done from within this file only.
	 */
	private SortReader() {
		directory = null;
		queue = null;
	}
	
	// Singleton variables
	private static SortReader _instance = null;
	private static Object _lock = new Object();
	
	/**
	 * Returns the single instance of this class.
	 * If no such instance exists, it creates one.
	 * Note: This method must be thread-safe!
	 * 
	 * @return Instance of <code>SortReader</code>
	 */
	public static SortReader getInstance() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					_instance = new SortReader();
				}
			}
		}
		return _instance;
	}

	/**
	 * Initializes the instance of <code>SortReader</code>.
	 * Specifically, this method sets the directory to look for files in, and
	 * the reference to the unsorted queue.
	 * 
	 * @param dir Directory to look for files in
	 * @param unsortedQueue Queue of unsorted files tasks
	 */
	public void init(File dir, BoundedBuffer<SortTask> unsortedQueue) {
		queue = unsortedQueue;
		directory = dir;
	}
	
	/**
	 * <code>SortReader</code> thread start point.
	 * This method lists the files to be sorted, then reads each one as an integer array,
	 * and creates a new <code>SortTask</code> which is enqueued to the unsorted task.
	 * When done, this method MUST set the unsorted queue as done.
	 */
	@Override
	public void run() {
		// Make sure the values are legal before we iterate over the file
		if (directory == null){
			System.err.println("SortReader was not initialized properly - missing directory.");
		} else if (queue == null) {
			System.err.println("SortReader was not initialized properly - missing queue.");
		} else if (!directory.isDirectory()) {
			System.err.println("SortReader was not initialized properly - illegal directory given");
		}
		// Get all the unsorted file in the directory
		File[] unsortedFiles = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(UNSORTED_EXTENSION);
			}
		});
		// Iterate over the file and fill the queue
		for (File unsortedFile : unsortedFiles) {
	    int[] data = readFile(unsortedFile);
	    if (data == null) {
	    	// An error occurred, skip the file
	    	continue;
	    }
	    // Enqueue a new task
	    queue.enqueue(new SortTask(data, unsortedFile));
    }
	}
	
	/**
	 * Reads an integer file and returns all the values it holds (unsorted).
	 * Returns <code>null</code> in case of a problem.
	 * @param unsortedFile The unsorted file to read
	 * @return The values in the file, or <code>null</code> if an error occurs
	 */
	private int[] readFile(File unsortedFile) {
		// Open the file for reading
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(unsortedFile));
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't not open file: " + unsortedFile +
					"\nError: " + e.getMessage());
			return null;
		}
		// Read the content to a temporary list
		List<Integer> vals = new ArrayList<Integer>();
		String line = null;
		try {
			while (reader.ready()) {
				line = reader.readLine();
				vals.add(Integer.parseInt(line));
			}
		} catch (IOException e) {
			System.err.println("Couldn't read from file: " + unsortedFile +
					"\nError: " + e.getMessage());
			return null;
		} catch (NumberFormatException e) {
			System.err.println("Illegal file format. Expects only an integer in each" +
					"line. Found the value <" + line + "> in file: " + unsortedFile +
					"\nError: " + e.getMessage());
			return null;
		}
		// Done reading, convert to an int[]
		int[] data = new int[vals.size()];
		for (int i = 0; i < data.length; ++i) {
			data[i] = vals.get(i);
		}
		// Close the handle and return the read data
		try {
			reader.close();
		} catch (IOException e) {
			// Nothing we can really do, we read the file already so we can return
			// its content already. Only print the error:
			System.err.println("Failed closing the file: " + unsortedFile + 
					" - The error was ignored.");
		}
		return data;
	}
}
