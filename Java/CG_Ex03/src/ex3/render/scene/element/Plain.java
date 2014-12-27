package ex3.render.scene.element;

import ex3.math.MatrixUtils;
import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.math.Vec;

/**
 * Represents a plain in the 3D scene.
 * @author dor
 */
public class Plain {

	/** The 3 points defining the plain */
	public Point3D p1;
	public Point3D p2;
	public Point3D p3;
	/** The plain's normal vector */
	public Vec normal;
	
	/**
	 * Construct a new plain.
	 */
	public Plain() {
		p1 = null;
		p2 = null;
		p3 = null;
		normal = null;
	}
	
	/**
	 * Creates a new Plain from 3 non-linear point (in the given string).
	 * @param p1 First point on the plain
	 * @param p2 Second point on the plain
	 * @param p3 Third point on the plain
	 */
	public Plain(Point3D p1, Point3D p2, Point3D p3) {
		InitPoints(p1, p2, p3);
	}
	
	/**
	 * Returns the coefficients (t, u1, u2) of the intersection point of the ray
	 * with the plain. If no intersection exists, return null.
	 * @param ray Ray to intersect with the plain
	 * @return Intersection point's coefficients
	 */
	public Point3D PlainIntersectionCoefficients(Ray ray) {
		// Eliminate rays hitting the rear face of the plain, or parallel to it.
		if (normal.dotProd(ray.v) >= 0) {
			// The ray can't hit the front side of the plain (whether it hits the
			// back or it's parallel to the plain)
			return null;
		}
		// Find an intersection point
		Point3D coefficients = MatrixUtils.solveMatrix(buildMatrix(ray));
		if (coefficients != null && coefficients.x <= 0) {
			// intersection is behind the ray's starting point
			return null;
		}
		return coefficients;
	}
	
	/**
	 * Initializes the plain's points and normal.
	 * @param p1 First point
	 * @param p2 Second point
	 * @param p3 Third point
	 */
	public void InitPoints(Point3D p1, Point3D p2, Point3D p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		// Assuming p1, p2 and p3 are in a counter-clockwise order!
		normal = Vec.crossProd(p2.vecFrom(p1), p3.vecFrom(p1));
		if (normal.length() == 0) {
			throw new IllegalArgumentException(
					"Plain's points should not be linear: " + p1 + " " + p2 + " " + p3);
		}
		normal.normalize();
	}
	
	/**
	 * Builds a matrix from the given ray and the 3 points representing the
	 * plain (V is the ray's vector, P0 is the ray's start point):
	 * 
	 *   -t-  -u1-     -u2-     -free-
	 *   
	 *   Vx  P1x-P2x  P1x-P3x | P1x-P0x
	 * ( Vy  P1y-P2y  P1y-P3y | P1y-P0y )
	 *   Vz  P1z-P2z  P1z-P3z | P1z-P0z
	 *   
	 * Note: For efficiency, we use the transpose of the matrix above (for
	 * 		   Cramer's rule usage).
	 * @return A representative matrix
	 */
	private double[][] buildMatrix(Ray ray) {
		double[][] matrix = {
				{ ray.v.x,        ray.v.y,        ray.v.z },
				{ p1.x - p2.x,    p1.y - p2.y,    p1.z - p2.z },
				{ p1.x - p3.x,    p1.y - p3.y,    p1.z - p3.z },
				{ p1.x - ray.p.x, p1.y - ray.p.y, p1.z - ray.p.z }
		};
		return matrix;
	}
}
