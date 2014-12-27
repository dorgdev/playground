/*
 * This file is part of "Data Structure - Exercise 7"
 */
package ex07;

/**
 * The TreeNode class represents a node in an {@link IntervalTree}, holding 
 * references to the node's parent, left son and right son.
 * Each TreeNode instance also holds its interval values (low and high) and the
 * maximum value of all the higher values held by the TreeNode itself and its
 * descendants.
 * Note#1: Low and high values given are immutable and can't be changed after
 * the node's creation.
 * Note#2: The TreeNode instance is a data holder structure only and it is not
 * responsible for manipulating its values according to setters methods 
 * invocations. E.g: It doesn't update the maximum value upon left/right son 
 * changes.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class TreeNode {
	
	/* ************************** */
	/* TreeNode's private members */
	/* ************************** */

	/** The TreeNode's right son */
	private TreeNode m_right;
	/** The TreeNode's left son */
	private TreeNode m_left;
	/** The TreeNode's parent node */
	private TreeNode m_parent;
	/** The lower value represented by the TreeNode */
	private int m_low;
	/** The higher value represented by the TreeNode */
	private int m_high;
	/** The maximum value represented by the TreeNode */
	private int m_max;
	
	/* ************************ */
	/* TreeNode's public method */
	/* ************************ */
	
	/**
	 * Creating a new TreeNode instance with no sons nor parent, and setting
	 * the held values according to the given values.
	 * @param low The lower value of the interval represented by the TreeNode
	 * @param high The higher value (as well as the maximum value) of the 
	 * interval represented by the TreeNode
	 * @throws IllegalArgumentException In case the low value given is higher
	 * then the high value given
	 */
	public TreeNode(int low, int high) throws IllegalArgumentException {
		if (low > high) {
			throw new IllegalArgumentException("Could not create a TreeNode " +
				"instance with a lower bound higher than the higher bound. " +
				"Given values were - low:" + low + " - high:" + high);
		}
		m_right = null;
		m_left = null;
		m_parent = null;
		m_low = low;
		m_high = high;
		m_max = high;
	}
	
	/**
	 * @return The lower value of the interval represented by the TreeNode
	 */
	public int getLow() {
		return m_low;
	}
	
	/**
	 * @return The higher value of the interval represented by the TreeNode
	 */
	public int getHigh() {
		return m_high;
	}
	
	/**
	 * @return The maximum value held by the TreeNode (and its descendants)
	 */
	public int getMax() {
		return m_max;
	}

	/**
	 * @return The TreeNode's parent node
	 */
	public TreeNode getParent() {
		return m_parent;
	}

	/**
	 * @return The TreeNode's left son node
	 */
	public TreeNode getLeft() {
		return m_left;
	}
	
	/**
	 * @return The TreeNode's right son node
	 */
	public TreeNode getRight() {
		return m_right;
	}
	
	/**
	 * Setting a new left son for the TreeNode
	 * @param left The new left son of the TreeNode
	 */
	public void setLeft(TreeNode left) {
		m_left = left;
	}
	
	/**
	 * Setting a new right son for the TreeNode
	 * @param right The new right son of the TreeNode
	 */
	public void setRight(TreeNode right) {
		m_right = right;
	}
	
	/**
	 * Setting a new parent node for the TreeNode
	 * @param parent The new parent node of the TreeNode
	 */
	public void setParent(TreeNode parent) {
		m_parent = parent;
	}
	
	/**
	 * Setting a new maximum value held by the TreeNode
	 * @param max The new maximum value to be set
	 * @throws IllegalArgumentException In case the given max value is lower
	 * than the higher value held by the current node
	 */
	public void setMax(int max) throws IllegalArgumentException {
		if (max < m_high) {
			throw new IllegalArgumentException("Could not set a new max " +
				"value which is lower than current high value. Given max " +
				"value was" + max + " while current higher value is " + 
				m_high);
		}
		m_max = max;
	}

	/**
	 * Checking whether the given values intersect with the current node's
	 * values.
	 * @param low The lower value of the intersect interval
	 * @param high The higher value of the intersect interval
	 * @return Whether the intervals intersect or not
	 */
	public boolean intersect(int low, int high) {
		return (low <= m_high && high >= m_low);
	}
	
	/**
	 * @return A readable representation of the node as a String
	 */
	@Override
	public String toString() {
		return "Low=" + m_low + "; High=" + m_high + "; Max=" + m_max;
	}
}
