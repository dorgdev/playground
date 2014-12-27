/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

import java.util.Arrays;

/**
 * Sorting thread class. Responsible for fetching unsorted arrays (encapsulated
 * as a SortTask) and sort them, then enqueue them for writing.
 * 
 * @author dor
 */
public class Sorter implements Runnable {

	/** Queue to read unsorted <code>SortTask</code> from */
	private BoundedBuffer<SortTask> unsortedQueue;
	/** Queue to write the sorted <code>SortTask</code> to */
	private BoundedBuffer<SortTask> sortedQueue;
	
	/**
	 * Constructor. Initializes references to shared queues.
	 * @param unsorterQueue Queue of unsorted tasks (input)
	 * @param sortedQueue Queue for sorted tasks to be written (output)
	 */
	public Sorter(BoundedBuffer<SortTask> unsorterQueue,
			BoundedBuffer<SortTask> sortedQueue) {
			this.unsortedQueue = unsorterQueue;
			this.sortedQueue = sortedQueue;
	}
	
	/**
	 * Runs the thread.  This method dequeues tasks from unsortedQueue until a
	 * <code>null</code> is returned (which means that no more elements will be
	 * available). For each task, the thread sorts its data and then enqueue
	 * it to the sortedQueue.
	 */
	@Override
	public void run() {
		while (true) {
			// Read a task from the unsorted queue
			SortTask task = unsortedQueue.dequeue();
			if (task == null) {
				// No more data expected from the queue. Abort.
				return;
			}
			// Sort the task's data
			Arrays.sort(task.getData());
			// And enqueue it in the sorted queue
			sortedQueue.enqueue(task);
		}
	}
}
