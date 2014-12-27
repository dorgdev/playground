/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

import java.io.File;

/**
 * A sort task descriptor.
 * 
 * @author dor
 */
public class SortTask {

	/** The character marking the extension position */
	public static final char EXTENSION_MARK = '.';
	/** The sorted files' extension */
	public static final String SORTED_EXTENSION = ".sorted";

	/** The data to be sorted by the task */
	private int[] data;
	/** The source file's location */
	private File source;
	/** The destination file's location */
	private File dest;
	
	/**
	 * Constructor. Initializes the sort task.
	 * @param data Data to sort (as read from file)
	 * @param source File descriptor of source file
	 */
	public SortTask(int[] data, File source) {
		this.data = data;
		this.source = source;

		// Assumes the source file has a ".unsorted" extension:
		int extensionIndex = source.getName().lastIndexOf(EXTENSION_MARK);
		String destFileName = source.getParent() + File.separatorChar +
			source.getName().substring(0, extensionIndex) + SORTED_EXTENSION;
		dest = new File(destFileName);
	}
	
	/**
	 * Returns the data array of this task
	 * @return Integers array which contains the data
	 */
	public int[] getData() {
		return data;
	}
	
	/**
	 * Sets new data for the task to hold
	 * @param data The new data to hold
	 */
	public void setData(int[] data) {
		this.data = data;
  }

	/**
	 * Returns the destination file descriptor. Destination file should be in the
	 * same directory as the source file, and have the same name, except for the
	 * extension: while source file has an extension .unsorted, destination file
	 * has the extension .sorted.
	 * 
	 * @return Destination file
	 */
	public File getDestFile() {
		return dest;
	}
	
	/**
	 * Returns the source file descriptor
	 * @return Source file
	 */
	public File getSourceFile() {
		return source;
	}
}
