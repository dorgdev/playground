package tests.ex07;

import java.util.Random;

import junit.framework.TestCase;
import ex07.IntervalTree;
import ex07.TreeNode;

public class IntervalTreeTest extends TestCase {

	public IntervalTree buildTree(TreeNode[] nodes) {
		IntervalTree tree = new IntervalTree();
		for (TreeNode node : nodes) {
			tree.insert(node);
		}
		return tree;
	}
	
	public boolean compareTrees(IntervalTree tree1, IntervalTree tree2) {
		TreeNode root1 = tree1.getRoot();
		TreeNode root2 = tree2.getRoot();
		return compareNodes(root1, root2);
	}
	
	public boolean compareNodes(TreeNode node1, TreeNode node2) {
		if (node1 == null && node2 == null) {
			return true;
		}
		if (node1 != null && node2 != null) {
			boolean rc = node1.getLow() == node2.getLow();
			rc = rc && node1.getHigh() == node2.getHigh();
			rc = rc && node1.getMax() == node2.getMax();
			rc = rc && compareNodes(node1.getLeft(), node2.getLeft());
			return rc && compareNodes(node1.getRight(), node2.getRight());
		}
		return false;
	}
	
	/* The testing methods */
	
	public void testTreeNode() {
		try {
			new TreeNode(5, 2);
			fail("Should not get here!");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		TreeNode n = new TreeNode(2, 8);
		assertNull(n.getParent());
		assertNull(n.getLeft());
		assertNull(n.getRight());
		assertEquals(2, n.getLow());
		assertEquals(8, n.getHigh());
		assertEquals(8, n.getMax());
	}
	
	public void testIntervalTree() {
		IntervalTree tree = new IntervalTree();
		assertNull(tree.search(1, 10));
		tree.insert(new TreeNode(1, 10));
		assertNotNull(tree.search(1, 10));
		assertNull(tree.search(2, 10));
		assertNull(tree.search(1, 8));
		assertNull(tree.search(2, 8));
		
		tree.insert(new TreeNode(2, 20));
		tree.insert(new TreeNode(1, 5));
		tree.insert(new TreeNode(2, 8));
		assertNotNull(tree.search(1, 10));
		assertNotNull(tree.search(2, 20));
		assertNotNull(tree.search(1, 5));
		assertNotNull(tree.search(2, 8));
		assertNull(tree.search(3, 6));
		
		tree.delete(2, 20);
		assertNull(tree.search(2, 20));
		
		assertNotNull(tree.intersect(2, 4));
		tree.delete(1, 5);
		assertNotNull(tree.intersect(2, 4));
		tree.delete(1, 10);
		assertNotNull(tree.intersect(2, 4));
		tree.delete(2, 4);
		tree.delete(2, 8);
		
		assertNull(tree.intersect(6, 8));
		tree.insert(new TreeNode(7, 12));
		assertNotNull(tree.intersect(6, 8));
		tree.insert(new TreeNode(4, 7));
		assertNotNull(tree.intersect(6, 8));
		tree.insert(new TreeNode(4, 10));
		assertNotNull(tree.intersect(6, 8));
		tree.insert(new TreeNode(10, 15));
		assertNull(tree.intersect(1, 2));
		assertNotNull(tree.intersect(6, 8));
		assertNull(tree.intersect(16, 20));

		tree.insert(new TreeNode(6, 9));
		tree.delete(4, 7);
		assertTrue(tree.getRoot().getLeft().getLeft() == null ||
				   tree.getRoot().getLeft().getRight() == null);
	}
	
	public void testIntervalTreeInputValidation() {
		IntervalTree tree = new IntervalTree();
		// Insert
		try {
			tree.insert(null);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail("Should not get here (1)!");
		}
		// Delete
		try {
			tree.delete(null);
			tree.delete(1,2);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail("Should not get here (2)!");
		}
		// Search
		assertNull(tree.search(2, 1));
		// Intersect
		assertNull(tree.intersect(2, 1));
	}
	
	public void testInsert() {
		IntervalTree tree = new IntervalTree();
		assertNull(tree.getRoot());
		
		tree.insert(new TreeNode(2, 6));
		assertNotNull(tree.getRoot());
		assertEquals(2, tree.getRoot().getLow());
		assertEquals(6, tree.getRoot().getHigh());
		assertEquals(6, tree.getRoot().getMax());
		assertNull(tree.getRoot().getParent());
		assertNull(tree.getRoot().getLeft());
		assertNull(tree.getRoot().getRight());

		tree.insert(new TreeNode(5, 8));
		assertEquals(8, tree.getRoot().getMax());
		assertNull(tree.getRoot().getLeft());
		TreeNode right = tree.getRoot().getRight();
		assertNotNull(right);
		assertEquals(5, right.getLow());
		assertEquals(8, right.getMax());
		assertEquals(8, right.getHigh());
		assertNotNull(right.getParent());
		assertNull(right.getLeft());
		assertNull(right.getRight());
		
		tree.insert(new TreeNode(2, 10));
		assertEquals(10, tree.getRoot().getMax());
		TreeNode left1 = tree.getRoot().getLeft();
		assertNotNull(left1);
		TreeNode right1 = tree.getRoot().getRight();
		assertEquals(5, right1.getLow());
		assertEquals(8, right1.getMax());
		assertEquals(8, right1.getHigh());
		assertNotNull(right1.getParent());
		assertNotNull(left1);
		assertEquals(2, left1.getLow());
		assertEquals(10, left1.getMax());
		assertEquals(10, left1.getHigh());
		assertNotNull(left1.getParent());
		assertNull(left1.getLeft());
		assertNull(left1.getRight());
	}
	
	public void testDeleteCase1() {
		// Only root
		IntervalTree tree = buildTree(new TreeNode[] {new TreeNode(2, 5)});
		IntervalTree expected = buildTree(new TreeNode[] {});
		tree.delete(2, 5);
		assertTrue(compareTrees(tree, expected));
	}
	
	public void testDeleteCase2() {
		// Leaf of the root
		IntervalTree actual = buildTree(new TreeNode[] {new TreeNode(2, 5),
														new TreeNode(1,7)});
		IntervalTree expected = buildTree(new TreeNode[] {new TreeNode(2, 5)});
		actual.delete(1, 7);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
		
		// Leaf of the root
		actual = buildTree(new TreeNode[] {new TreeNode(2, 5),
										   new TreeNode(1,4)});
		actual.delete(1, 4);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

	}
	
	public void testDeleteCase3() {
		// Root with two sons
		IntervalTree actual = buildTree(new TreeNode[] {
				new TreeNode(3, 7),
				new TreeNode(2, 5),
				new TreeNode(5, 8)});
		IntervalTree expected = buildTree(new TreeNode[] {
				new TreeNode(5, 8),
				new TreeNode(2, 5)});
		actual.delete(3, 7);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// Root with two sons
		actual = buildTree(new TreeNode[] {
				new TreeNode(3, 7),
				new TreeNode(2, 8),
				new TreeNode(5, 6)});
		expected = buildTree(new TreeNode[] {
				new TreeNode(5, 6),
				new TreeNode(2, 8)});
		actual.delete(3, 7);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
}

	public void testDeleteCase4() {
		// 3 nodes, 3 levels
		// (1)
		IntervalTree actual = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(2, 6),
				new TreeNode(1, 5)
		});
		IntervalTree expected = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(1, 5)
		});
		actual.delete(2, 6);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (2)
		actual = buildTree(new TreeNode[] {
				new TreeNode(4, 6),
				new TreeNode(2, 8),
				new TreeNode(1, 5)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(4, 6),
				new TreeNode(1, 5)
		});
		actual.delete(2, 8);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (3)
		actual = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(2, 6),
				new TreeNode(1, 12)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(1, 12)
		});
		actual.delete(2, 6);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (4)
		actual = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(2, 12),
				new TreeNode(1, 10)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(4, 8),
				new TreeNode(1, 10)
		});
		actual.delete(2, 12);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
}

	public void testDeleteCase5() {
		// Deleting root, have both son, right is successor
		// (1)
		IntervalTree actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(3, 8),
				new TreeNode(7, 12),
				new TreeNode(9, 13)
		});
		IntervalTree expected = buildTree(new TreeNode[] {
				new TreeNode(7, 12),
				new TreeNode(3, 8),
				new TreeNode(9, 13)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (2)
		actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(3, 15),
				new TreeNode(7, 12),
				new TreeNode(9, 13)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(7, 12),
				new TreeNode(3, 15),
				new TreeNode(9, 13)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (3)
		actual = buildTree(new TreeNode[] {
				new TreeNode(5, 15),
				new TreeNode(3, 8),
				new TreeNode(7, 12),
				new TreeNode(9, 13)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(7, 12),
				new TreeNode(3, 8),
				new TreeNode(9, 13)
		});
		actual.delete(5, 15);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (4)
		actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(3, 8),
				new TreeNode(7, 15),
				new TreeNode(9, 13)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(7, 15),
				new TreeNode(3, 8),
				new TreeNode(9, 13)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
}

	public void testDeleteCase6() {
		// Deleting node in tree, right is not successor
		// (1)
		IntervalTree actual = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(6, 15),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(7, 13),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		IntervalTree expected = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(7, 13),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		actual.delete(6, 15);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (2)
		actual = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(6, 25),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(7, 13),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(7, 13),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		actual.delete(6, 25);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (3)
		actual = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(6, 15),
				new TreeNode(4, 25),
				new TreeNode(9, 15),
				new TreeNode(7, 13),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(7, 13),
				new TreeNode(4, 25),
				new TreeNode(9, 15),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		actual.delete(6, 15);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (4)
		actual = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(6, 15),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(7, 25),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(10, 20),
				new TreeNode(7, 25),
				new TreeNode(4, 9),
				new TreeNode(9, 15),
				new TreeNode(8, 16),
				new TreeNode(12, 21)
		});
		actual.delete(6, 15);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
}

	public void testDeleteCase7() {
		// Deleting root, successor is not right son
		// (1)
		IntervalTree actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(6, 12),
				new TreeNode(9, 11)
		});
		IntervalTree expected = buildTree(new TreeNode[] {
				new TreeNode(6, 12),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(9, 11)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (2)
		actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(6, 15),
				new TreeNode(9, 11)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(6, 15),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(9, 11)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));

		// (3)
		actual = buildTree(new TreeNode[] {
				new TreeNode(5, 10),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(6, 12),
				new TreeNode(9, 18)
		});
		expected = buildTree(new TreeNode[] {
				new TreeNode(6, 12),
				new TreeNode(7, 13),
				new TreeNode(4, 8),
				new TreeNode(9, 18)
		});
		actual.delete(5, 10);
		// Comparing actual and expected values
		assertTrue(compareTrees(actual, expected));
	}

	public void testBuildingRandomTrees() {
		Random rand = new Random();
		for (int j = 0; j < 50; ++j) {
			TreeNode[] nodes1 = new TreeNode[50];
			TreeNode[] nodes2 = new TreeNode[50];
			for (int i = 0; i < nodes1.length; ++i) {
				int low = rand.nextInt(25) - 12;
				int high = rand.nextInt(25) + 13;
				nodes1[i] = new TreeNode(low, high);
				nodes2[i] = new TreeNode(low, high);
			}
			IntervalTree tree1 = buildTree(nodes1);
			IntervalTree tree2 = buildTree(nodes2);
			assertTrue(compareTrees(tree1, tree2));
			int deleteIndex = rand.nextInt(nodes1.length);
			tree1.delete(nodes1[deleteIndex]);
			assertFalse(compareTrees(tree1, tree2));
			tree2.delete(nodes2[deleteIndex]);
			assertTrue(compareTrees(tree1, tree2));
		}
	}
}
