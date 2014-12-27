/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

/**
 * Represents a object/model which can be subdivided by a given level.
 */
public interface Subdividable {

	/**
	 * Performs subdivisions with accordance to the requested level
	 * @param levels The level of subdivisions to perform
	 */
	public void subdivide(int levels);

}
