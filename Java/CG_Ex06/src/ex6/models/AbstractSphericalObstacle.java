/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

/**
 * An abstract obstacle class, with a radius and a center.
 */
public class AbstractSphericalObstacle implements ISphericalObstacle {

	/** The obstacle center */
	protected Vec center;
	/** The obstacle radius */
	protected double radius;

	/**
	 * Creates a new AbstractSphericalObstacle instance.
	 * @param center The obstacle's center
	 * @param radius The obstacle's radius
	 */
	public AbstractSphericalObstacle(Vec center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	@Override
	public Vec center() {
		return center;
	}

	@Override
	public double radius() {
		return radius;
	}

}
