package ex3.render.scene.element;

import ex3.math.Ray;
import ex3.render.scene.Hit;

/**
 * Describes any element which can be intersected with a Ray.
 * @author dor
 */
public interface Intersectable {

	/**
	 * Returns the Hit object representing the hit of the ray at the intersection
	 * point with the surface this element, if exists, or null otherwise.
	 * @param ray The ray to be intersected with
	 * @return The intersection hit, if exists, or null otherwise
	 */
	public Hit intersect(Ray ray);

}
