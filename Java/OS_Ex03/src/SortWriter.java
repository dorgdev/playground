/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writing thread class. Responsible for fetching sorted arrays (encapsulated
 * as a SortTask) and write them to disk.
 * 
 * @author dor
 */
public class SortWriter implements Runnable {

	/** The queue of sorted tasks to handle */
	private BoundedBuffer<SortTask> queue;
	
	/**
	 * Constructor. Initializes the reference to the sorted queue.
	 * @param sortedQueue Sorted tasks queue
	 */
	public SortWriter(BoundedBuffer<SortTask> sortedQueue) {
		this.queue = sortedQueue;
	}
	
	/**
	 * Runs the thread.  This method dequeues tasks from sortedQueue until a 
	 * <code>null</code> is returned (which means that no more elements will be
	 * available).  For each task, the thread should write its data to a text file
	 * as specified by the return value of SortTask.getDestFile(). In case of
	 * error in writing of some file, this method should display some error message
	 * and continue to the next file.
	 */
	@Override
	public void run() {
		while (true) {
			SortTask task = queue.dequeue();
			if (task == null) {
				// No more tasks to handle. Abort.
				return;
			}
			handleTask(task);
		}
	}

	/**
	 * Handles a single task. Write the sorted values of the task to the destination
	 * file one by one.
	 * @param task The task to handle
	 */
	private void handleTask(SortTask task) {
		// Open the file
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(task.getDestFile()));
		} catch (IOException e) {
			System.err.println("Couldn't open destination file: " + task.getDestFile() +
					"\nError: " + e.getMessage());
			return;
		}
		// Write the sorted values
		int[] data = task.getData();
		for (int i : data) {
			try {
				bw.write(String.valueOf(i));
				bw.newLine();
			} catch (IOException e) {
				System.err.println("Failed writing to file: " + task.getDestFile() +
						"\nError: " + e.getMessage());
				return;
			}
    }
		// Done writing. Close the handle
		try {
	    bw.close();
    } catch (IOException e) {
    	System.err.println("Problem closing the file: " + task.getDestFile());
    }
	}
}
