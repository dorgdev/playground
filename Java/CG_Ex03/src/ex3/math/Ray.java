package ex3.math;

/**
 * This will work only if you will add the Vec Class from Ex2 - Make sure its working properly.
 * You will also need to implement your own Point3D class.
 */
public class Ray {

	// point of origin
	public Point3D p;
	// ray direction
	public Vec v;
	
	/**
	 * constructs a new ray
	 * @param p - point of origin
	 * @param v - ray direction
	 */
	public Ray(Point3D p, Vec v) {
		this.p = p;
		this.v = v;
		v.normalize();
	}
	
	/**
	 * Computes the point at the distance of t*v from the ray's origin.
	 * @param t The scalar t to scale the ray's vector with
	 * @return The point at the requested distance
	 */
	public Point3D getPoint(double t) {
		return p.addVec(Vec.scale(t, v));
	}
	
	@Override
	public String toString() {
		return p + "+t" + v;
	}
}
