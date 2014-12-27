/*
 * This file is part of "Data Structure - Exercise 7"
 */
package ex07;

/**
 * The Lecture class describes a lecture using its starting and ending time and
 * the lecture's name.
 * It derives from the {@link TreeNode} instance and therefore can be managed
 * using an {@link IntervalTree}.
 * @author Dor Gross (dorgross@gmail.com)
 * @author Lee Khen (lee.khn@gmail.com)
 */
public class Lecture extends TreeNode {

	/* **************************** */
	/* Lecture private members      */
	/* **************************** */
	
	/** The lecture's name */
	private String m_name;
	
	/* *************************** */
	/* Lecture public methods      */
	/* *************************** */

	/**
	 * Creating a new Lecture class using its starting time (low interval's
	 * bound), its ending time (high interval's bound) and the lecture's name.
	 * Note: Beside the numerical order (low < high), there are no restrictions
	 * on the given input as the user may describe the lecture's timeline in
	 * any desired values.
	 * @param low Starting time
	 * @param high Ending time
	 * @param name The lecture's name
     * @throws IllegalArgumentException In case low value given is higher than
     * the high value given
	 */
	public Lecture(int low, int high, String name) 
	throws IllegalArgumentException {
		super(low, high);
		m_name = name;
	}
	
	/**
	 * @return A readable representation of the Lecture instance.
	 */
	@Override
	public String toString() {
		return m_name + " (" + getLow() + ", " + getHigh() + ")";
	}

}
