/*
 * This file is part of "Data Structure - Exercise 7"
 */
package ex07;

/**
 * The Classroom class represents a classroom holding Lecture nodes which
 * are interval representing the lecture's start time, end time and the 
 * lecture's name.
 * The Classroom class manages the different lectures in it and can tell when
 * an insertion conflict is made due to overlapping lectures.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class Classroom extends IntervalTree {

	/* **************************** */
	/* Classroom private members    */
	/* **************************** */
	
	/** The classroom's name */
	private String m_name;
	
	/* *************************** */
	/* Classroom public methods    */
	/* *************************** */

	/**
	 * Creating a new classroom using its name
	 * @param name The new classroom's name
	 */
	public Classroom(String name) {
		super();
		m_name = name;
	}
	
	/**
	 * @return A readable presentation of the Classroom instance with all of 
	 * its Lecture nodes.
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Classroom ");
		buffer.append(m_name).append(":\n").append(super.toString());
		return buffer.toString();
	}
	
	/**
	 * Making sure the Classroom's user inserts only Lectures to the classroom.
	 */
	@Override
	public void insert(TreeNode node) {
		if (node instanceof Lecture) {
			super.insert(node);
		} else {
			throw new IllegalArgumentException("Can't insert TreeNode instance"
					+ " which is not a Lecture: class=" + node.getClass());
		}
	}
	
	/**
	 * Adding a new Lecture to the classroom.
	 * In case the given lecture overlaps an existing lecture in the Classroom,
	 * retrieving the overlapping lecture, otherwise, inserting the new Lecture
	 * and returning null.
	 * @param lect The new Lecture to be inserted into the Classroom
	 * @return The overlapping Lecture if failed or null if succeeded
	 */
	public Lecture addLecture(Lecture lect) {
		TreeNode intersect = intersect(lect.getLow(), lect.getHigh());
		if (intersect != null) {
			// Found an overlapping lecture
			return (Lecture)intersect;
		}
		// No overlapping, inserting the node
		super.insert(lect);
		return null;
	}
}
