package ex3.render.scene.element;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.math.Vec;
import ex3.render.scene.Hit;
import ex3.utils.Utils;

/**
 * Represents a circle surface (plain) in a 3D scene.
 * @author dor
 */
public class Circle extends AbstractSceneElement {

	/** The circle's default plain's vectors. Parallel to the Z axis */
	public static final Vec DEFAULT_VEC1 = new Vec(1, 0, 0);
	public static final Vec DEFAULT_VEC2 = new Vec(0, 1, 0);
	/** The default inner radius is 0, so the circle is not empty */
	public static final double DEFAULT_INNER_RADIUS = 0;
	
	/** the center of the circle */
	private Point3D center;
	/** The radius of the circle */
	private double radius;
	/** An optional inner radius to create an empty circle (like a ring) */
	private double innerRadius;
	/** The plain on which the circle lies */
	private Plain plain;
	
	/**
	 * Creating a new Circle.
	 */
	public Circle() {
		super();
	}
	
	@Override
	public void init(Map<String, String> attrs) {
		super.init(attrs);
	  // Center
	  center = Point3D.ParsePoint3D(Utils.getValue(attrs, Utils.CIRCLE_CENTER_ATTR));
	  // Radius
	  radius = Double.parseDouble(Utils.getValue(attrs, Utils.CIRCLE_RADIUS_ATTR));
	  // Inner radius
	  innerRadius = DEFAULT_INNER_RADIUS;
	  if (attrs.containsKey(Utils.CIRCLE_INNER_RADIUS_ATTR)) {
	  	innerRadius = Double.parseDouble(attrs.get(Utils.CIRCLE_INNER_RADIUS_ATTR));
	  }
	  // Check that radius is larger than the inner-radius
	  if (radius < innerRadius) {
	  	throw new IllegalArgumentException(
	  			"Inner radius should be less than the regular (larger) radius!");
	  }
	  // If available, the vectors
	  Vec v1 = DEFAULT_VEC1;
	  if (attrs.containsKey(Utils.CIRCLE_VEC1_ATTR)) {
	  	v1 = Vec.parseVec(attrs.get(Utils.CIRCLE_VEC1_ATTR));
	  }
	  Vec v2 = DEFAULT_VEC2;
	  if (attrs.containsKey(Utils.CIRCLE_VEC2_ATTR)) {
	  	v2 = Vec.parseVec(attrs.get(Utils.CIRCLE_VEC2_ATTR));
	  }
	  Point3D p1 = center.addVec(v1);
	  Point3D p2 = center.addVec(v2);
	  plain = new Plain(center, p1, p2);
	}

	@Override
	public Hit intersect(Ray ray) {
	  Point3D coefficients = plain.PlainIntersectionCoefficients(ray);
	  // No intersection
	  if (coefficients == null) {
	  	return null;
	  }
	  // Check the distance between the center and the intersection point
	  Point3D intersection = ray.getPoint(coefficients.x);
	  double dist = center.distance(intersection); 
	  if (dist > radius || dist < innerRadius) {
	  	return null;
	  }
		// Found a valid intersection :)
		return new Hit(plain.normal, intersection, this);
	}
}
