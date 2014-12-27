package ex3.render.scene.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.render.scene.Hit;
import ex3.utils.Utils;

/**
 * Represents a set of several triangles combined together.
 * 
 * @author dor
 */
public class Trimesh extends AbstractSceneElement {

	public static final int MIN_ITEMS_FOR_BOUNDING = 3;
	
	// The list of triangles held by this trimesh
	protected List<Triangle> tris;
	
	// A bounding box for accelerating the intersection with the triangles of
	// the trimesh (if needed).
	protected BoundingBox bounds;
	
	/**
	 * Creating a new Trimesh.
	 */
	public Trimesh() {
		bounds = null;
		tris = new ArrayList<Triangle>();
	}
	
	@Override
	public void init(Map<String, String> attributes) {
	  super.init(attributes);
		for (String key : attributes.keySet()) {
	    if (!key.startsWith(Utils.TRIANGLE_ATTR_PREFIX)) {
	    	continue;
	    }
			Triangle tri = new Triangle();
			List<Point3D> points = Point3D.ParseSeveral(attributes.get(key));
			if (points.size() != 3) {
				throw new IllegalArgumentException("Triangle expects 3 points exactly!");
			}
			tri.init(points.get(0), points.get(1), points.get(2));
			tris.add(tri);
	  }
		for (Triangle tri : tris) {
			tri.material = material;
		}
	}
	
	@Override
	public Hit intersect(Ray ray) {
		if (bounds != null && !(bounds.inBounds(ray.p))) {
			// If the ray comes from outside of the trimesh's bounds, check if it
			// intersect with the bounding box, if it doesn't return no intersection.
			if (bounds.intersect(ray) == null) {
				return null;
			}
		}
		Hit closest = null;
		for (Triangle tri : tris) {
	    Hit intersection = tri.intersect(ray);
	    if (intersection == null) {
	    	continue;
	    }
	    if (closest == null) {
	    	closest = intersection;
	    } else {
	    	if (ray.p.distance(closest.p) > ray.p.distance(intersection.p)) {
	    		closest = intersection;
	    	}
	    }
    }
		return closest;
	}
	
	@Override
	public void setAccelerationMode(boolean useAcceleration) {
		super.setAccelerationMode(useAcceleration);
		if (useAcceleration) {
			calcBoundingBox();
		}
	}
	
	/**
	 * Calculates, if applicable, the bounding box to improve intersection
	 * calculation.
	 */
	private void calcBoundingBox() {
		if (tris.size() < MIN_ITEMS_FOR_BOUNDING) {
			// Don't create the bounding box for less than a minimum number of triangles
			return;
		}
		List<Point3D> points = new ArrayList<Point3D>(tris.size() * 3);
		for (Triangle tri : tris) {
	    points.add(tri.plain.p1);
	    points.add(tri.plain.p2);
	    points.add(tri.plain.p3);
    }
		Point3D min = new Point3D(points.get(0)); 
		Point3D max = new Point3D(points.get(0)); 
		for (Point3D point : points) {
	    if (min.x > point.x) {
	    	min.x = point.x;
	    } else if (max.x < point.x) {
	    	max.x = point.x;
	    }
	    if (min.y > point.y) {
	    	min.y = point.y;
	    } else if (max.y < point.y) {
	    	max.y = point.y;
	    }
	    if (min.z > point.z) {
	    	min.z = point.z;
	    } else if (max.z < point.z) {
	    	max.z = point.z;
	    }
    }
		bounds = new BoundingBox(min, max);
	}
	
}
