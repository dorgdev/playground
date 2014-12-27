/*
 * Operating Systems - Exercise 3
 * Student's Name: Dor Gross
 * Student's Id:   039344999
 */

/**
 * A bounded buffer is a FIFO queue with a limited number of elements that can be stored in it.
 * This data structure supports two main operations: enqueue and dequeue. As this data structure
 * supports multi-threading, these operations block the calling thread if the buffer is full (in enqueue)
 * or empty (in dequeue).
 *
 * @param <T> Type of elements
 */
public class BoundedBuffer<T> {

	/** A lock for handling concurrent requests */
	private Object internalLock;
	/** The data held by the instance */
	private T[] data;
	/** A flag marking the queue is empty and no more work should be supplied */
	private boolean done;
	/** The index of the next dequeue operation */
	private int dequeueIndex;
	/** The index of the next enqueue operation */
	private int enqueuIndex;
	
	/**
	 * Constructor. Initializes a new bounded buffer with a given capacity.
	 * @param capacity Capacity
	 */
	@SuppressWarnings("unchecked")
	public BoundedBuffer(int capacity) {
		internalLock = new Object();
		data = (T[]) new Object[capacity];
		done = false;
		dequeueIndex = 0;
		enqueuIndex = 0;
		// Clears the queue
		for (int i = 0; i < data.length; ++i) {
			data[i] = null;
		}
	}
	
	/**
	 * Dequeues next element from the queue.
	 * This method blocks if the buffer is empty, or returns <code>null</code> if it is empty
	 * and the <code>done</code> flag is set.
	 *  
	 * @return First element of queue, or <code>null</code> if queue is empty and set as done.
	 */
	public T dequeue() {
		synchronized (internalLock) {
			while (true)	 {
				if (data[dequeueIndex] != null) {
					// There's data available to dequeue
					T dequeued = data[dequeueIndex];
					data[dequeueIndex] = null;
					dequeueIndex = (dequeueIndex + 1) % data.length;
					// Make sure we wake the enqueue operation if it waits for free space
					internalLock.notifyAll();
					return dequeued;
				} else {
					if (done) {
						// No data available and the queue is done. Time to stop.
						return null;
					}
					try {
						// Wait for available data
						internalLock.wait();
					} catch (InterruptedException e) {
						// Ignores interrupted exceptions
					}
				}
			}
		}
	}

	/**
	 * Enqueues an element as the last one in queue.
	 * This method blocks if the buffer is full.
	 * 
	 * @param item Element to enqueue
	 */
	public void enqueue(T item) {
		synchronized (internalLock) {
			while (!done) {
				if (enqueuIndex == dequeueIndex && data[enqueuIndex] != null) {
					// The queue is full.  Wait until someone reads some data.
					try {
						internalLock.wait();
					} catch (InterruptedException e) {
						// Ignores interrupted exceptions
					}
				} else {
					// There's place for more in the queue
					data[enqueuIndex] = item;
					enqueuIndex = (enqueuIndex + 1) % data.length;
					// Notify any dequeue operation waiting for more elements
					internalLock.notifyAll();
					return;
				}
			}
    }
	}

	/**
	 * Returns the capacity of the buffer.
	 * 
	 * @return Capacity of the buffer
	 */
	public int getCapacity() {
		// Number of elements never changes, doesn't need locking
		return data.length;
	}

	/**
	 * Returns current size of the queue.
	 * 
	 * @return Number of elements currently in queue
	 */
	public int getSize() {
		synchronized (internalLock) {
			if (enqueuIndex == dequeueIndex) {
				// The queue could be full or empty, return the appropriate number
				return (data[enqueuIndex] == null) ? 0 : data.length;
			}
			return (enqueuIndex - dequeueIndex) % data.length;
    }
	}
	
	/**
	 * Sets the queue as done and releases any thread waiting for a dequeue operation.
	 */
	public void setDone() {
		synchronized (internalLock) {
			done = true;
			// Make sure all waiting thread are notified about the change
			internalLock.notifyAll();
    }
	}
}
