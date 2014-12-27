/*
 * This file is part of "Data Structure - Exercise 7"
 */
package ex07;

/**
 * The IntervalTree class represents an ordered list of known values intervals,
 * using a Binary Search Tree of {@link TreeNode} instances.
 * It uses for its comparable value the lower value held by each TreeNode.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class IntervalTree {

	/* **************************** */
	/* IntervalTree private members */
	/* **************************** */
	
	/** The root of the tree instance */
	private TreeNode m_root;
	
	/* *************************** */
	/* IntervalTree public methods */
	/* *************************** */
	
	/**
	 * Creating a new IntervalTree instance with no root.
	 */
	public IntervalTree() {
		m_root = null;
	}
	
	/**
	 * Inserts a new node into the tree according to its lower value, and 
	 * setting the maximum values of its future ancestors' node to the new
	 * highest value if necessary.
	 * Note: Null value given will be ignored
	 * @param node The new node to be added
	 */
	public void insert(TreeNode node) {
		if (m_root == null) {
			m_root = node;
			return;
		}
		if (node == null) {
			return;
		}
		// Finding where to insert the new node
		TreeNode context = m_root;
		boolean toRight = node.getLow() > context.getLow();
		TreeNode son = toRight ? context.getRight() : context.getLeft();
		while (son != null) {
			context = son;
			toRight = node.getLow() > context.getLow();
			son = toRight ? context.getRight() : context.getLeft();
		}
		// Inserting the new node
		setNewParent(node, context, toRight);
		// Might have new maximum
		if (context.getMax() < node.getHigh()) {
			updateMax(context);
		}
	}
	
	/**
	 * First looking for a matching node, and if found, deleting it from the
	 * tree.
	 * @param low Lower bound of the node's interval
	 * @param high Higher bound of the node's interval
	 * @see IntervalTree#delete(TreeNode)
	 */
	public void delete(int low, int high) {
		// Finding the node
		TreeNode found = search(low, high);
		delete(found);
	}
	
	/**
	 * Deleting a node from the tree and updating its' ancestors of a possible
	 * changes in the maximum value.
	 * Note: Null value given will be ignored
	 * @param node The node to be deleted.
	 */
	public void delete(TreeNode node) {
		if (node == null) {
			return;
		}
		// Handling all cases of deletion:
		// (1) The node is a leaf
		// (2) The node has only one son
		// (3) The node has two sons
		TreeNode parent = node.getParent();
		TreeNode replaced = null;
		boolean hasRight = node.getRight() != null;
		boolean hasLeft = node.getLeft() != null;
		TreeNode nodeToSetMax = null;

		if (!hasRight || !hasLeft) {
			// (1) The node is a leaf, or (2) the node has only one child
			replaced = hasRight ? node.getRight() : node.getLeft();
			nodeToSetMax = parent;
		} else {
			// (3) The node has both left and right sons
			replaced = findSuccessor(node);
			// Replacing the node with its successor
			setNewParent(node.getLeft(), replaced, false);
			if (replaced == node.getRight()) {
				nodeToSetMax = replaced;
			} else {
				nodeToSetMax = replaced.getParent();
				setNewParent(replaced.getRight(), replaced.getParent(), false);
				setNewParent(node.getRight(), replaced, true);
			}
		}
		// Setting the new parent for the replaced node
		setNewParent(replaced,
					 parent, 
					 parent != null && node == parent.getRight());
		// Finally - Updating max
		updateMax(nodeToSetMax);
	}
	
	/**
	 * Searching for a given node in the tree with the given interval values.
	 * If found, retrieving it. Otherwise, returning null.
	 * Note: Illegal values will result a null return value.
	 * @param low The lower interval's bound of the searched node
	 * @param high The higher interval's bound of the searched node
	 * @return A node matching the required interval or null if not found
	 */
	public TreeNode search(int low, int high) {
		if (low > high || m_root == null) {
			return null;
		}
		TreeNode context = m_root;
		while (context != null) {
			// If context node matches the values
			if (context.getHigh() == high && context.getLow() == low) {
				return context;
			}
			// No chances of finding if values are higher than max
			if (context.getMax() < high) {
				return null;
			}
			// Searching descendants
			boolean toRight = low > context.getLow();
			context = toRight ? context.getRight() : context.getLeft();
		}
		// The matching node, or null if not found
		return context;
	}
	
	/**
	 * Looking for a node which represents an interval overlapping with the
	 * given ineterval's values (and by that, intersecting the given values).
	 * Note: Illegal values will result a null return value.
	 * @param low The lower interval's bound of the searched node
	 * @param high The higher interval's bound of the searched node
	 * @return A node matching the required interval or null if not found
	 */
	public TreeNode intersect(int low, int high) {
		if (low > high) {
			return null;
		}
		TreeNode node = m_root;
		while (node != null && !node.intersect(low, high)) {
			if (node.getLeft() != null && node.getLeft().getMax() >= low) {
				node = node.getLeft();
			} else {
				node = node.getRight();
			}
		}
		return node;
	}
	
	/**
	 * Getting the root node of the IntervalTree instance.
	 * @return The root node of the tree.
	 */
	public TreeNode getRoot() {
		return m_root;
	}
	
	/**
	 * @return A readable representation of the tree node's in the tree as a
	 * String. An empty tree will result an empty String
	 */
	@Override
	public String toString() {
		if (m_root == null) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		buildToStringBuffer(buffer, m_root);
		return buffer.toString();
	}
	
	/* **************************** */
	/* IntervalTree private methods */
	/* **************************** */
		
	/**
	 * Updating the maximum value for the given context node and all matching
	 * ancestors of it until a new max value is found.
	 * @param node The context node to be set with the new max value
	 */
	private void updateMax(TreeNode node) {
		int max;
		while (node != null) {
			max = getMax(node);
			node.setMax(node.getHigh());
			if (max > node.getMax()) {
				node.setMax(max);
			}
			node = node.getParent();
		}
	}
	
	/**
	 * Getting the max value of the given node by checking the node itself and
	 * it sons (in case they are not null).
	 * The method assumes the node is not null
	 * @param node The node to check
	 * @return The node's max value
	 */
	private int getMax(TreeNode node) {
		int max = node.getHigh();
		if (node.getRight() != null) {
			max = Math.max(max, node.getRight().getMax());
		}
		if (node.getLeft() != null) {
			max = Math.max(max, node.getLeft().getMax());
		}
		return max;
	}	
	
	/**
	 * Finding the successor node of the given node.
	 * Note: The method assumes given node has both left and right sons
	 * @param node The node being inspected for successor
	 * @return The given node's successor in the binary tree
	 */
	private TreeNode findSuccessor(TreeNode node) {
		TreeNode successor = node.getRight();
		while (successor.getLeft() != null) {
			successor = successor.getLeft();
		}
		return successor;
	}
	
	/**
	 * Setting the new parent for the given node. If the parent node given is
	 * null, assuming it should be set as the tree root.
	 * @param node The node to set its parent
	 * @param parent The new node's parent or null if it should be set as the
	 * tree's root
	 * @param right Whether to set the node as the parent right son. Ignored if
	 * given parent node is null
	 */
	private void setNewParent(TreeNode node, TreeNode parent, boolean right) {
		if (node != null) {
			node.setParent(parent);
		}
		if (parent != null) {
			if (right) {
				parent.setRight(node);
			} else {
				parent.setLeft(node);
			}
		} else {
			m_root = node;
		}
	}
	
	/**
	 * Building the tree's toString representation in a recursive way.
	 * @param buffer The string buffer built so far
	 * @param node The current inspected node
	 */
	private void buildToStringBuffer(StringBuffer buffer, TreeNode node) {
		buffer.append(node.toString());
		if (node.getLeft() != null) {
			buffer.append('\n');
			buildToStringBuffer(buffer, node.getLeft());
		}
		if (node.getRight() != null) {
			buffer.append('\n');
			buildToStringBuffer(buffer, node.getRight());
		}
	}
}
