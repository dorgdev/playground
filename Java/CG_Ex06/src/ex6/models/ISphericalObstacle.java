/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

/**
 * This interface describes an object that can be intersected with other objects,
 * and can be approximated by a bounding sphere.
 */
public interface ISphericalObstacle {

	/**
	 * Returns the center point of the obstacle
	 */
	public Vec center();

	
	/**
	 * Returns the radius of the obstacle
	 */
	public double radius();
		
}
