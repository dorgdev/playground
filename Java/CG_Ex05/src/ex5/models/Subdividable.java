/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5.models;

/**
 * Represents a object/model which can be subdivided by a given level.
 * 
 * @author dor
 */
public interface Subdividable {

	/**
	 * Performs subdivisions with accordance to the requested level
	 * @param levels The level of subdivisions to perform
	 */
	public void subdivide(int levels);

}
