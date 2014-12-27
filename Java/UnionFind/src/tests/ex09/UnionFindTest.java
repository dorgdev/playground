package tests.ex09;


import junit.framework.TestCase;
import ex09.UnionFind;


public class UnionFindTest extends TestCase {
	
	private UnionFind m_instance;
	private int m_sets;
	
	protected void setUp() {
		//    2     4   10  11
		//  1,7,3  5,6      12
		//      8    9
		//
		m_instance = new UnionFind(12);
		m_instance.union(2, 7);
		m_instance.union(1, 2);
		m_instance.union(3, 8);
		m_instance.union(3, 2);
		m_instance.union(4, 5);
		m_instance.union(6, 9);
		m_instance.union(6, 4);
		m_instance.union(11, 12);
		m_sets = m_instance.getNumSets();
	}

	public void testUnionFindCTOR() {
		UnionFind x = new UnionFind(5);
		assertEquals(5, x.getNumSets());
		try {
			x = new UnionFind(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	public void testUnionFindUnion() {
		assertEquals(4, m_sets);
		m_instance.union(2, 4);
		assertEquals(m_sets - 1, m_instance.getNumSets());
		m_instance.union(10, 11);
		assertEquals(m_sets - 2, m_instance.getNumSets());
		m_instance.union(2, 11);
		assertEquals(1, m_instance.getNumSets());
		
		try {
			m_instance.union(1, 2);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			m_instance.union(2, 3);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			m_instance.union(6, 7);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			m_instance.union(8, 8);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	public void testUnionFindFind() {
		int[] leaders = {2, 2, 2, 4, 4, 4, 2, 2, 4, 10, 11, 11};
		for (int i = 0; i < 12; ++i) {
			assertEquals(leaders[i], m_instance.find(i + 1));
		}
		try {
			m_instance.find(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			m_instance.find(13);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	public void testUnionFindEmptySets() {
		UnionFind x = new UnionFind(0);
		assertEquals(0, x.getNumSets());
		try {
			x.find(1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			x.union(1, 2);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
	}
}
