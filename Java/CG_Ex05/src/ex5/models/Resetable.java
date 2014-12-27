/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5.models;

/**
 * An interface which allows models to reset to their default view.
 * 
 * @author dor
 */
public interface Resetable {

	/**
	 * Tells the deriving classes to reset themselves.
	 */
	public void reset();
	
}
