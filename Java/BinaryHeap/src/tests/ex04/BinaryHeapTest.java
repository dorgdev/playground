package tests.ex04;

import java.util.Arrays;
import java.util.Random;

import ex04.BinaryHeap;
import ex04.MergeArrays;
import ex04.BinaryHeap.HeapElement;
import ex04.BinaryHeap.HeapException;

import junit.framework.TestCase;


public class BinaryHeapTest extends TestCase {

	private BinaryHeap m_heap;
	private Random m_random;
	private int[] m_values;
	
	@Override
	protected void setUp() throws Exception {
		m_random = new Random();
		m_values = new int[m_random.nextInt(BinaryHeap.MAX_HEAP_SIZE-1)+1];
		m_heap = new BinaryHeap(m_values.length);
		for (int i = 0; i < m_values.length; ++i) {
			m_values[i] = m_random.nextInt(200) - 100;
			m_heap.insert(new HeapElement(m_values[i], String.valueOf(m_values[i])));
		}
		Arrays.sort(m_values);
	}
	
	////////////////////////////////////////////////////////////////////////
	// BinaryHeap test methods
	
	public void testBinaryHeap() throws HeapException {
		BinaryHeap heap1 = new BinaryHeap();
		assertEquals(0, heap1.getNodesCount());
		assertEquals(BinaryHeap.MAX_HEAP_SIZE, heap1.getMaxNodesCount());
		int size = 16;
		BinaryHeap heap2 = new BinaryHeap(size);
		assertEquals(0, heap2.getNodesCount());
		assertEquals(size, heap2.getMaxNodesCount());
		BinaryHeap heap3 = null;
		try {
			 heap3 = new BinaryHeap(0);
			fail();
		} catch (HeapException e) {
			assertTrue(heap3 == null);
		}
		try {
			 heap3 = new BinaryHeap(-4);
			fail();
		} catch (HeapException e) {
			assertTrue(heap3 == null);
		}
	}

	public void testInsert() throws Exception {
		BinaryHeap heap1 = new BinaryHeap(3);
		heap1.insert(new HeapElement(5, "5"));
		assertEquals(1, heap1.getNodesCount());
		heap1.insert(new HeapElement(8, "8"));
		assertEquals(2, heap1.getNodesCount());
		heap1.insert(new HeapElement(5, "10"));
		assertEquals(3, heap1.getNodesCount());
		try {
			heap1.insert(new HeapElement(7, "7"));
			fail();
		} catch (HeapException e) {
			assertTrue(heap1.getMaxNodesCount() == heap1.getNodesCount());
		}
	}
	
	public void testFindMin() throws Exception {
		BinaryHeap heap1 = new BinaryHeap();
		try {
			heap1.findMin();
			fail();
		} catch (HeapException e) {
			assertEquals(0, heap1.getNodesCount());
		}
		assertEquals(m_values[0], m_heap.findMin().getKey());
	}
	
	public void testDeleteMin() throws Exception {
		BinaryHeap heap1 = new BinaryHeap();
		try {
			heap1.deleteMin();
			fail();
		} catch (HeapException e) {
			assertEquals(0, heap1.getNodesCount());
			assertEquals(BinaryHeap.MAX_HEAP_SIZE, heap1.getMaxNodesCount());
		}
		
		for (int i = 0; i < m_values.length; ++i) {
			HeapElement elem = m_heap.deleteMin();
			assertEquals(m_values[i], elem.getKey());
			assertEquals(m_values.length - i - 1, m_heap.getNodesCount());
		}
	}
	
	public void testConcurrent() throws Exception {
		int[] vals = {2, 4, 7, 10, 12, 10, -3, 7, 42, 100, 87, 55, 1221, 0, 
					  -597, 301, 211, -750, 500, -200};
		BinaryHeap heap1 = new BinaryHeap(vals.length + 5);
		for (int i = 0; i < vals.length; ++i) {
			heap1.insert(new HeapElement(vals[i], "The Integer: " + vals[i] + 
						 " (" + i + "/" + vals.length + ")."));
		}
		assertEquals(20, heap1.getNodesCount());
		assertEquals(25, heap1.getMaxNodesCount());
		assertEquals(-750, heap1.findMin().getKey());
		assertEquals(-750, heap1.deleteMin().getKey());
		assertEquals(19, heap1.getNodesCount());
		assertEquals(25, heap1.getMaxNodesCount());
		assertEquals(-597, heap1.findMin().getKey());
		heap1.insert(new HeapElement(-750, "The Iteger: -750"));
		assertEquals(20, heap1.getNodesCount());
		assertEquals(25, heap1.getMaxNodesCount());
		assertEquals(-750, heap1.deleteMin().getKey());
		int min = -750;
		while (!heap1.isEmpty()) {
			HeapElement minElem = heap1.deleteMin(); 
			assertTrue(min <= minElem.getKey());
			min = minElem.getKey();
		}
	}
	
	public void testBuildHeap() throws Exception {
		BinaryHeap heap1 = BinaryHeap.buildHeap(null);
		assertEquals(BinaryHeap.MAX_HEAP_SIZE, heap1.getMaxNodesCount());
		assertEquals(0, heap1.getNodesCount());
		BinaryHeap heap2 = BinaryHeap.buildHeap(new HeapElement[] {} );
		assertEquals(BinaryHeap.MAX_HEAP_SIZE, heap2.getMaxNodesCount());
		assertEquals(0, heap2.getNodesCount());
		
		int[] vals = {2, 4, 7, 10, 12, 10, -3, 7, 42, 100, 87, 55, 1221, 0, 
				      -597, 301, 211, -750, 500, -200};
		HeapElement[] elems = new HeapElement[vals.length];
		BinaryHeap heap3 = new BinaryHeap(vals.length + 5);
		for (int i = 0; i < vals.length; ++i) {
			HeapElement elem = new HeapElement(vals[i], "The Integer: " + 
					vals[i] + " (" + (i+1) + "/" + vals.length + ").");
			heap3.insert(elem);
			elems[i] = elem;
		}
		BinaryHeap heap4 = BinaryHeap.buildHeap(elems);
		assertEquals(heap3.getNodesCount(), heap4.getNodesCount());
		while (!heap3.isEmpty() && !heap4.isEmpty()) {
			int min3 = heap3.deleteMin().getKey();
			int min4 = heap4.deleteMin().getKey();
			assertEquals(min3, min4);
		}
		assertTrue(heap3.isEmpty() && heap4.isEmpty());
	}
	
	public void testMerge() throws Exception {
		int arrays[][] = new int[5][];
		arrays[0] = new int[] {4, 6, 9, 12, 32, 40, 56}; 
		arrays[1] = null; 
		arrays[2] = new int[] {7, 18, 20}; 
		arrays[3] = new int[] {}; 
		arrays[4] = new int[] {5, 11, 18, 27, 90};
		int[] result = MergeArrays.merge(arrays);
		int[] expected_result = new int[] {4, 5, 6, 7, 9, 11, 12, 18, 18, 20, 
										   27, 32, 40, 56, 90};
		assertTrue(Arrays.equals(expected_result, result));
	}
	
	public void testMerge2() {
		int[][] arrays = new int[100000][];
		for (int i = 0; i < arrays.length; ++i) {
			int size = 100;
			if (size == -1) {
				arrays[i] = null;
			} else if (size == 0) {
				arrays[i] = new int[] {};
			} else {
				arrays[i] = new int[size];
				for (int j = 0; j < size; ++j) {
					arrays[i][j] = m_random.nextInt(2000);
				}
				Arrays.sort(arrays[i]);
			}
		}
//		for (int[] arr : arrays) {
//			if (arr != null) {
//				for (int i : arr) {
//					System.out.print(i + ", ");
//				}
//			}
//		}
		long ts = System.currentTimeMillis();
		int[] sorted = MergeArrays.merge(arrays);
		System.out.println("\nRun time: " + (System.currentTimeMillis() - ts));
		for (int i = 1; i < sorted.length; ++i) {
			assertTrue(sorted[i-1] <= sorted[i]);
		}
//		System.out.println();
//		for (int i : sorted) {
//			System.out.print(i + ", ");
//		}
	}
}