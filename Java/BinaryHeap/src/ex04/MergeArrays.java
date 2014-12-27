/*
 * This file is part of "Data Structure - Exercise 4"
 */
package ex04;

import ex04.BinaryHeap.HeapElement;
import ex04.BinaryHeap.HeapException;


/**
 * The MergeArrays class is a class containing only one visible method: 
 * <code>int[] merge(int[][])<code> which takes several sorted arrays and 
 * retrieves a single array, sorted with all the valued from the given arrays.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class MergeArrays {
	
	/**
	 * Receive an array of sorted integer arrays, and returning a single array
	 * of sorted integers from all the received arrays.
	 * The algorithm works as follows:
	 * 1. Eliminating empty or null arrays from the list.
	 * 2. Creating a heap from the first elements of all arrays, and a queue
	 *    of integers from every array (since assumed sorted).
	 * 3. Each phase, taking the minimal value from the heap, and inserting the
	 *    next element from the element's original array.
	 * 4. When the main heap is empty, were done.
	 * In case of the following problems, an empty array would be returned:
	 * - All given arrays are empty
	 * - An internal error occurred
	 * Note: This method works only for sorted arrays. If none sorted arrays
	 * are given, there's no guarantee for a sorted outcome!
	 * @param arrays The list of sorted arrays to sort their values
	 * @return A sorted array of values from all the given arrays, or an empty
	 * array in case of problem during the merge algorithm
	 */
	public static int[] merge(int[][] arrays) {
		// Getting the amount of values in the arrays, and the amount of queues
		// needed (we won't create an queue for an empty array)
		int heapsCount = 0;
		int elemsCount = 0;
		for (int i = 0; i < arrays.length; ++i) {
			if (arrays[i] == null || arrays[i].length == 0) {
				continue;
			}
			elemsCount += arrays[i].length;
			heapsCount++;
		}
		if (heapsCount == 0) {
			return new int[] {};
		}
		// Creating ArrayFetchers (queues) from the given arrays
		ArrayFetcher[] fetchers = new ArrayFetcher[heapsCount];
		for (int i = 0; i < arrays.length; ++i) {
			if (arrays[i] == null || arrays[i].length == 0) {
				continue;
			}
			fetchers[--heapsCount] = new ArrayFetcher(arrays[i]);
		}
		try {
			// Creating the main heap with the first element of each arrays
			HeapElement[] mainHeapsElems = new HeapElement[fetchers.length];
			for (int i = 0; i < fetchers.length; ++i) {
				mainHeapsElems[i] =  new HeapElement(fetchers[i].fetch(), 
													 fetchers[i]);
			}
			BinaryHeap mainHeap = BinaryHeap.buildHeap(mainHeapsElems);
			// Creating the result array from all the arrays:
			int[] result = new int[elemsCount];
			int index = 0;
			while (!mainHeap.isEmpty()) {
				HeapElement newMin = mainHeap.deleteMin();
				ArrayFetcher fetcher = (ArrayFetcher)(newMin.getValue());
				// Getting the next value from the queue
				Integer newMinVal = fetcher.fetch();
				if (newMinVal != null) {
					mainHeap.insert(new HeapElement(newMinVal, fetcher));
				}
				result[index++] = newMin.getKey();
			}
			return result;
		} catch (HeapException e) {
			// This is not likely to happen since all heaps operations are
			// internally derived, but to be safe, we return an empty array
			return new int[] {};
		}
	}

	/**
	 * An empty private CTOR so this class won't be instantiated
	 */
	private MergeArrays() {
	}

	
	/**
	 * An inner class which holds an array and an index which helps iterating 
	 * over the array (fetch the array content).
	 * This is actually a simple implementation of a predefined static queue of 
	 * integers (can't be dynamically changed after creation).
	 */
	private static class ArrayFetcher {
		/**
		 * Create the Arrayfetcher with an int[] to be held
		 * @param array The array held by the fetcher
		 */
		public ArrayFetcher(int[] array) {
			m_array = array;
			m_index = 0;
		}
		/**
		 * Get the next integer in the array. If the array is NULL or if we're
		 * done iterating over the array, return NULL, otherwise, the next int
		 * in the array
		 * @return The next int in the array or NULL of done
		 */
		public Integer fetch() {
			if (m_array == null || m_index >= m_array.length) {
				return null;
			}
			return m_array[m_index++];
		}
		/** The array held by the fetcher */
		private int[] m_array;
		/** An index for following the array */
		private int m_index;
	}

}
