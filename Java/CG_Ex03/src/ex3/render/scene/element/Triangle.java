package ex3.render.scene.element;

import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.render.scene.Hit;
import ex3.utils.Utils;

/**
 * Represents a triangle in the 3D scene.
 * Derives from Plain and checks coefficients of intersection point to determine
 * whether the intersection point is inside the triangle or not.
 * @author dor
 */
public class Triangle extends AbstractSceneElement {

	/** The plain on which the triangle lies */
	public Plain plain;
	
	/**
	 * Creating a new Triangle.
	 */
	public Triangle() {
		super();
	}

	/**
	 * Creates a new Triangle from the 3 points of its plain.
	 * @param p1 First point of the triangle
	 * @param p2 Second point of the triangle
	 * @param p3 Third point of the triangle
	 */
	public Triangle(Point3D p1, Point3D p2, Point3D p3) {
		super();
		plain = new Plain(p1, p2, p3);
	}

	/**
	 * Initializes the Triangle from 3 points.
	 * @param p1 First point of the triangle
	 * @param p2 Second point of the triangle
	 * @param p3 Third point of the triangle
	 * @param material The material from which the triangle is made
	 */
	public void init(Point3D p1, Point3D p2, Point3D p3) {
		plain = new Plain(p1, p2, p3);
	}
	
	@Override
	public Hit intersect(Ray ray) {
		Point3D coefficients = plain.PlainIntersectionCoefficients(ray);
		if (coefficients == null) {
			// No intersection
			return null;
		}
		double t = coefficients.x;
		double u1 = coefficients.y;
		double u2 = coefficients.z;
		if (Utils.gt(u1 + u2, 1) || Utils.gt(u1, 1) || Utils.gt(u2, 1) || 
				u1 < 0 || u2 < 0) {
			// No intersection *inside* the triangle
			return null;
		}
		// Found a valid intersection :)
		return new Hit(plain.normal, ray.getPoint(t), this);
	}
}
