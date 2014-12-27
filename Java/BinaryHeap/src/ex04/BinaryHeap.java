/*
 * This file is part of "Data Structure - Exercise 4"
 */
package ex04;

import java.util.Arrays;

/**
 * The BinaryHeap class holds element in a minimum binary heap, enable 
 * performing various operations on the elements in the heap and on the heap
 * itself.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class BinaryHeap {

	//////////////////////////////////////////////////////////////////////
	// Static and constant definitions
	
	/** Maximum heap size */
	public static int MAX_HEAP_SIZE = 32;
	
	/**
	 * The HeapException class is the BinaryHeap's error handling mechanism,
	 * used to report problems occurred in the different BinaryHeap's operations
	 */
	public static class HeapException extends Exception {
		/** Serialization's serial version unified ID value */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Create a new HeapException instance from a given message.
		 * @param message A message describing the cause for the error
		 */
		public HeapException(String message) {
			super(message);
		}
		
		/**
		 * Create a new HeapException instance from a given cause and a 
		 * describing message
		 * @param message The message describing the error
		 * @param cause The root cause for the error being thrown
		 */
		public HeapException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * The HeapElement class represent a single element in the BinaryHeap.
	 * It holds a comparable instance and can be compared according to its
	 * key value.
	 */
	public static class HeapElement {
		
		/**
		 * Create a new HeapElement instance using the comparable value it
		 * holds (it's comparison key and value).
		 * @param key A key value used for comparison
		 * @param value The comparable value to be held by the HeapElement
		 */
		public HeapElement(int key, Object value) {
			m_key = key;
			m_value = value;
		}
		
		/**
		 * Get the comparable value held by the HeapElement instance
		 * @return The comparable value held by HeapElement
		 */
		public Object getValue() {
			return m_value;
		}
		
		/**
		 * Replace the value held by the HeapElement
		 * @param newValue The new value to be set
		 */
		public void setValue(Object newValue) {
			m_value = newValue;
		}
		
		/**
		 * Get the comparable key held by the HeapElement instance
		 * @return The comparable key held by HeapElement
		 */
		public int getKey() {
			return m_key;
		}
				
		/**
		 * Checks whether the value of the current value is greater then the
		 * value held by the given HeapElement
		 * @param other The HeapElement holding the value to perform the 
		 * comparison with.
		 * @return True in case the current value is greater than the value
		 * held by the other HeapElement. False otherwise.
		 */
		public boolean isGreaterThan(HeapElement other) {
			return m_key > other.m_key;
		}
				
		/** The additional data value held by the HeapElement instance */
		private Object m_value;
		
		/** The comparison key value held by the HeapElement instance */
		private int m_key;
	}
	
	/**
	 * Build a new BinaryHeap instance from the given array of elements.
	 * In case the given array of elements is empty or NULL, return an empty
	 * heap with the default maximum size (declared in MAX_HEAP_SIZE).
	 * Note: Never uses the same array as given but creating a copy of it.
	 * @param elems The elements to be set in the new built BinaryHeap
	 * @return A BinaryHeap instance with all the given values
	 */
	public static BinaryHeap buildHeap(HeapElement[] elements) 
	throws HeapException {
		if (elements == null || elements.length == 0) {
			return new BinaryHeap();
		}
		BinaryHeap heap = new BinaryHeap(elements.length);
		heap.m_elements = Arrays.<HeapElement>copyOf(elements, elements.length);
		heap.m_next = heap.m_elements.length;
		for (int i = (heap.m_next / 2) - 1; i >= 0; --i) {
			heap.percolateDown(i);
		}
		return heap;
	}
	
	//////////////////////////////////////////////////////////////////////
	// BinaryHeap's public methods
	
	/**
	 * Create a new BinaryHeap with the default heap's size, which will be
	 * the maximum heap size as defined in MAX_HEAP_SIZE const value.
	 */
	public BinaryHeap() throws HeapException {
		this(MAX_HEAP_SIZE);
	}
	
	/**
	 * Create a new BinaryHeap with a given maximum size of nodes
	 * @param capacity The maximum nodes' count of the created heap
	 */
	public BinaryHeap(int capacity) throws HeapException {
		if (capacity <= 0) {
			throw new HeapException("Heap's size must be a positive number!");
		}
		m_elements = new BinaryHeap.HeapElement[capacity];
		m_next = 0;
		for (int i = 0; i < capacity; ++i) {
			m_elements[i] = null;
		}
	}
	
	/**
	 * Inserts a new value into the heap.
	 * @param val The value to insert
	 * @throws HeapException In case a problem occurs while trying to insert
	 * the new value. 
	 */
	public void insert(HeapElement val) throws HeapException {
		if (m_next >= m_elements.length) {
			throw new HeapException("The Heap is full. Can't insert!");
		}
		m_elements[m_next] = val;
		percolateUp(m_next++);
	}
	
	/**
	 * Finds the minimum value held by the BinaryHeap instance.
	 * @throws HeapException In case the heap is empty
	 */
	public HeapElement findMin() throws HeapException {
		if (m_next == 0) {
			throw new HeapException("The heap is empty. Can't find minimum!");
		}
		return m_elements[0];
	}
	
	/**
	 * Deletes the minimum value held in the heap and returns it.
	 * @return The deleted value
	 * @throws HeapException In case of an empty heap is given.
	 */
	public HeapElement deleteMin() throws HeapException {
		// Make sure the heap is not empty
		if (m_next == 0) {
			throw new HeapException("Can't delete min element. Heap is empty!");
		}
		// If the heap has only one element, retrieve it and clear the heap
		if (m_next == 1) {
			m_next = 0;
			return m_elements[0];
		}
		// Set the last element in the heap as the head and re-sort the heap
		HeapElement min = m_elements[0];
		m_elements[0] = m_elements[--m_next];
		m_elements[m_next] = null;  // Just as a precaution
		percolateDown(0);
		return min;
	}
	
	/**
	 * Get the current size of the binary heap (the amount of nodes in it)
	 * @return The amount of current allocated nodes in the binary heap
	 */
	public int getNodesCount() {
		return m_next;
	}
	
	/**
	 * Get the maximum size of the binary heap
	 * @return The maximum amount of nodes in the heap
	 */
	public int getMaxNodesCount() {
		return m_elements.length;
	}
	
	/**
	 * Whether the heap is empty or not
	 * @return True if it's empty, false otherwise
	 */
	public boolean isEmpty() {
		return m_next == 0;
	}

	///////////////////////////////////////////////////////////////////////
	// BinaryHeap's private methods and members
	
	/**
	 * Get the index of the supposed left son of the given node
	 * @param current The index of the parent node
	 * @return The supposed index for the given parent's left son
	 */
	private int left(int current) {
		return (2 * current) + 1;
	}
	
	/**
	 * Get the index of the supposed right son of the given node
	 * @param current The index of the parent node
	 * @return The supposed index for the given parent's right son
	 */
	private int right(int current) {
		return (2 * current) + 2;
	}
	
	/**
	 * Get the index of the given's node parent, according to its index
	 * @param current The index of the son's node
	 * @returnThe index of the parent node
	 */
	private int parent(int current) {
		return (current - 1) / 2;
	}
	
	/**
	 * Swap the elements in the given index, regardless to their values
	 * @param index1 First element's index
	 * @param index2 Second's element's index
	 */
	private void swap(int index1, int index2) {
		HeapElement temp = m_elements[index1];
		m_elements[index1] = m_elements[index2];
		m_elements[index2] = temp;
	}
	
	/**
	 * Make sure the value in the given index is greater than its parent (or
	 * the heap's root). In case it isn't, swap the two values and invoke 
	 * recursively on the swapped index.
	 * @param index The index of the checked node.
	 */
	private void percolateUp(int index) {
		// we got to the root
		if (index == 0) {
			return;
		}
		int parentIndex = parent(index);
		HeapElement son = m_elements[index];
		HeapElement parent = m_elements[parentIndex];
		// If the parent is not greater than the son, we're done
		if (!parent.isGreaterThan(son)) {
			return;
		}
		// Otherwise, swap and invoke recursively
		swap(index, parentIndex);
		percolateUp(parentIndex);
	}
	
	/**
	 * Make sure the value in the given index is smaller or equal to every son
	 * of it. In case it isn't, replace it with the smaller valued son and
	 * invoke the method recursively with the swapped index value
	 * @param index The index of the node to check
	 */
	private void percolateDown(int index) {
		if (index >= m_elements.length) {
			return;
		}
		int leftIndex = left(index);
		int rightIndex = right(index);
		int smallestIndex = index;
		// Check left son
		if (leftIndex < m_next && 
			!m_elements[leftIndex].isGreaterThan(m_elements[smallestIndex])) {
			smallestIndex = leftIndex;
		}
		// Check right son
		if (rightIndex < m_next && 
			!m_elements[rightIndex].isGreaterThan(m_elements[smallestIndex])) {
			smallestIndex = rightIndex;
		}
		// Check new smallest value
		if (smallestIndex != index) {
			swap(index, smallestIndex);
			percolateDown(smallestIndex);
		}
	}
	
	/** The elements held in the heap */
	private HeapElement[] m_elements;
	
	/** The next free location in the heap */
	private int m_next;
}