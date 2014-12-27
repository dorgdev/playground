package ex3.render.scene.element;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.math.Vec;
import ex3.render.scene.Hit;


/**
 * Represents a bounding box, aligned with the grid's axis.
 * @author dor
 */
public class BoundingBox extends AbstractSceneElement {

	/** Normal vectors for each face of the box */
	public static final Vec MIN_X_NORMAL = new Vec(-1, 0, 0);
	public static final Vec MAX_X_NORMAL = new Vec(1, 0, 0);
	public static final Vec MIN_Y_NORMAL = new Vec(0, -1, 0);
	public static final Vec MAX_Y_NORMAL = new Vec(0, 1, 0);
	public static final Vec MIN_Z_NORMAL = new Vec(0, 0, -1);
	public static final Vec MAX_Z_NORMAL = new Vec(0, 0, 1);

	/** Points representing the minimum and maximum values of the box */
	public Point3D max;
	public Point3D min;

	/**
	 * Creates a new BoundingBox.
	 */
	public BoundingBox() {
		super();
		min = null;
		max = null;
	}

	/**
	 * Creates a new BoundingBox from the 2 points given.
	 * @param p1 First point
	 * @param p2 Second point
	 */
	public BoundingBox(Point3D p1, Point3D p2) {
		super();
		initBoundingPoints(p1, p2);
	}
	
	@Override
	public void init(Map<String, String> attributes) {
		// Don't allow this method to be called. Used the 2 points method instead.
		throw new IllegalAccessError("BoundingBox's init method should not be accessed!");
	}
	
	/**
	 * Initializes
	 * @param attributes
	 * @param p1
	 * @param p2
	 */
	public void init(Map<String, String> attributes, Point3D p1, Point3D p2) {
	  super.init(attributes);
	  initBoundingPoints(p1, p2);
	}
	
	/**
	 * Initializes the bounds of the bounding box.
	 * @param p1 First point.
	 * @param p2 Second point.
	 */
	protected void initBoundingPoints(Point3D p1, Point3D p2) {
		max = new Point3D(
				(p1.x > p2.x ? p1.x : p2.x),
				(p1.y > p2.y ? p1.y : p2.y),
				(p1.z > p2.z ? p1.z : p2.z));
		min = new Point3D(
				(p1.x < p2.x ? p1.x : p2.x),
				(p1.y < p2.y ? p1.y : p2.y),
				(p1.z < p2.z ? p1.z : p2.z));
	}
	
	/**
	 * Returns true if the given point is in the bounds of the box.
	 * @param p The point to check
	 * @return True if the point is in bounds
	 */
	public boolean inBounds(Point3D p) {
		return (p.x <= max.x) && (p.x >= min.x) && (p.y <= max.y) && 
					 (p.y >= min.y) && (p.z <= max.z) && (p.z >= min.z);
	}
	
	/**
	 * Find the intersection point of the given ray with the box. Returns null
	 * if no such point exists.
	 * @param ray The ray to intersect with
	 * @return The intersection point, if exists, or null otherwise.
	 */
	public Hit intersect(Ray ray) {
		// X
		if (ray.v.x != 0) {
			if (ray.p.x > max.x) {
				// Above the max X
				double t = (max.x - ray.p.x) / ray.v.x;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.y <= max.y && intersection.y >= min.y &&
							intersection.z <= max.z && intersection.z >= min.z) {
						return new Hit(MAX_X_NORMAL, intersection, this);
					}
				}
			} else if (ray.p.x < min.x) {
				// Above the max X
				double t = (min.x - ray.p.x) / ray.v.x;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.y <= max.y && intersection.y >= min.y &&
							intersection.z <= max.z && intersection.z >= min.z) {
						return new Hit(MIN_X_NORMAL, intersection, this);
					}
				}
			}
		}
		// Y
		if (ray.v.y != 0) {
			if (ray.p.y > max.y) {
				// Above the max Y
				double t = (max.y - ray.p.y) / ray.v.y;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.x <= max.x && intersection.x >= min.x &&
							intersection.z <= max.z && intersection.z >= min.z) {
						return new Hit(MAX_Y_NORMAL, intersection, this);
					}
				}
			} else if (ray.p.y < min.y) {
				// Above the max Y
				double t = (min.y - ray.p.y) / ray.v.y;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.x <= max.x && intersection.x >= min.x &&
							intersection.z <= max.z && intersection.z >= min.z) {
						return new Hit(MIN_Y_NORMAL, intersection, this);
					}
				}
			}
		}
		// Z
		if (ray.v.z != 0) {
			if (ray.p.z > max.z) {
				// Above the max Z
				double t = (max.z - ray.p.z) / ray.v.z;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.y <= max.y && intersection.y >= min.y &&
							intersection.x <= max.x && intersection.x >= min.x) {
						return new Hit(MAX_Z_NORMAL, intersection, this);
					}
				}
			} else if (ray.p.z < min.z) {
				// Above the max Z
				double t = (min.z - ray.p.z) / ray.v.z;
				if (t > 0) {
					Point3D intersection = ray.getPoint(t);
					if (intersection.y <= max.y && intersection.y >= min.y &&
							intersection.x <= max.x && intersection.x >= min.x) {
						return new Hit(MIN_Z_NORMAL, intersection, this);
					}
				}
			}
		}
		// No intersection
		return null;
	}
	
}
