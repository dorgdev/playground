/*
 * This file is part of "Data Structure - Exercise 9"
 */
package ex09;


/**
 * This class represents a UnionFind using Up-Tree forest algorithm.
 * It enables finding the root of each element, uniting trees by their roots
 * and retrieving the number of inner trees in it.
 * @author Lee Khen (lee.khn@gmail.com)
 * @author Dor Gross (dorgross@gmail.com)
 */
public class UnionFind {
	
	/** The current number of sets in the UnionFind */
	private int m_numSets;
	/** The elements of the UnionFind */
	private int[] m_elements;
	
	/* ************************ */
	/* UnionFind public methods */
	/* ************************ */

	/**
	 * Creates a new UnionFind instance with the given count of starting sets.
	 * @param numElement The amount of starting sets
	 */
	public UnionFind(int numElement) {
		if (numElement < 0) {
			throw new IllegalArgumentException("Sets count must be >= 0; given"
					+ " value was " + numElement);
		}
		m_numSets = numElement;
		m_elements = new int[numElement];
		for (int i = 0; i < numElement; ++i) {
			m_elements[i] = -1;
		}
	}
	
	/**
	 * Unites the sets whose leaders are the indexes I and J.
	 * The method performs a leadership validation prior to the union.
	 * @param i First leader's index
	 * @param j Second leader's index
	 */
	public void union(int i, int j) {
		// Check valid indexes:
		validInBoundIndex(i);
		validInBoundIndex(j);
		if (i == j) {
			throw new IllegalArgumentException("Can't unite a set with itself;"
					+ " Provided indexes are equal: " + i);
		}
		// Check that i and j are both their sets' leaders:
		if (!isLeader(i) || !isLeader(j)) {
			throw new IllegalArgumentException("Can't perform union on an " +
					"element which is not the set's leader; " + 
					(isLeader(i) ? j : i) + "  is not the leader of its set");
		}
		// Set the united set's parent
		int parent = i;
		int son = j;
		if (weight(i) == weight(j)) {
			if (j < i) {
				parent = j;
				son = i;
			}
		}
		if (weight(j) > weight(i)) {
			parent = j;
			son = i;
		}
		setParent(son, parent);
	}
	
	/**
	 * Finds the leader's index of the I given index
	 * @param i The index to search for its leader
	 * @return The leader's index
	 */
	public int find(int i) {
		validInBoundIndex(i);
		int leader = m_elements[i - 1];
		if (leader < 0) {
			return i;
		}
		while (m_elements[leader - 1] > 0) {
			 leader = m_elements[leader - 1];
		}
		m_elements[i - 1] = leader;
		return leader;
	}

	/**
	 * Returns the amount of currently held sets in the UnionFind
	 * @return Current amount of sets
	 */
	public int getNumSets() {
		return m_numSets;
	}
	
	/* ************************* */
	/* UnionFind private methods */
	/* ************************* */
	
	/**
	 * Validates the given index by checking it's in length of sets' bound, 
	 * which is [1, num elements].
	 * @param i The index to check
	 * @throws IllegalArgumentException In case the index is out of bounds
	 */
	private void validInBoundIndex(int i) {
		if (i > m_elements.length || i <= 0) {
			throw new IllegalArgumentException("Index should be in the range" +
					" [1," + m_elements.length + "]; Given value was " + i);
		}
	}
	
	/**
	 * Checks whether the given index is the leader of its sets.
	 * @param i The index to check
	 * @return True if it's the leader of the set, false otherwise
	 */
	private boolean isLeader(int i) {
		return m_elements[i-1] < 0;
	}
	
	/**
	 * Returns the weight of an index. If it's a leader of set, the value is
	 * the actual weight of it, otherwise, it's the negative value of the 
	 * index's parent in the set.
	 * @param i The index to check
	 * @return Weight of the given index
	 */
	private int weight(int i) {
		return (- m_elements[i-1]);
	}

	/**
	 * Sets the parent of the index I given as the Index J given.
	 * The method assumes both I and J are the leaders of their sets (for
	 * weight calculation).
	 * @param i The index of the future son
	 * @param j The index of the future parent
	 */
	private void setParent(int i, int j) {
		m_elements[j - 1] += m_elements[i - 1];
		m_elements[i - 1] = j;
		--m_numSets;
	}
}
