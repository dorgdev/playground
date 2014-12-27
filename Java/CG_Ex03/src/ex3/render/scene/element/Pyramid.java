package ex3.render.scene.element;

import java.util.List;
import java.util.Map;

import ex3.math.Point3D;
import ex3.math.Vec;
import ex3.utils.Utils;

/**
 * Represents a Pyramid with a polygonal base shape in the 3D scene.
 * @author dor
 */
public class Pyramid extends Trimesh {

	/**
	 * Creates a new Pyramid.
	 */
	public Pyramid() {
		super();
	}
	
	@Override
	public void init(Map<String, String> attrs) {
	  // First, find the apex (tip) of the pyramid
	  Point3D apex = Point3D.ParsePoint3D(Utils.getValue(attrs, Utils.PYRAMID_APEX_ATTR));
	  // Read all the base point. Assume they are given counter-clockwise and that
	  // they are all on the same plane.
	  List<Point3D> basePoints = 
	  	Point3D.ParseSeveral(Utils.getValue(attrs, Utils.PYRAMID_BASE_ATTR));
	  if (basePoints.size() < 3) {
	  	throw new IllegalArgumentException("A pyramid should have at least 3 base-points!");
	  }
	  // Find the center of the pyramid on the base plain
	  Point3D p1 = basePoints.get(0);
	  Point3D p2 = basePoints.get(1);
	  Point3D p3 = basePoints.get(2);
	  Vec normal = Vec.crossProd(p1.vecFrom(p2), p3.vecFrom(p2));
	  normal.normalize();
	  normal.scale(normal.dotProd(p1.vecFrom(apex)));
	  Point3D baseCenter = apex.addVec(normal);
	  // Go over the all the sides and base to form a trimesh in the shape of a pyramid
	  int cnt = basePoints.size();
	  for (int i = 0; i < cnt; ++i) {
		  p1 = basePoints.get(i);
		  p2 = basePoints.get((i + 1) % cnt);
	  	// Side triangle
	  	Triangle side = new Triangle();
	  	side.init(p1, p2, apex);
	  	tris.add(side);
	  	// Base triangle (facing the other side) - Make sure it exists (handling
	  	// the case of an apex over one of the base edges)
	  	if (Vec.crossProd(p1.vecFrom(p2), p1.vecFrom(baseCenter)).lengthSquared() == 0) {
	  		continue;
	  	}
	  	Triangle base = new Triangle();
	  	base.init(p2, p1, baseCenter);
	  	tris.add(base);
	  }
	  // Finally, initialize the trimesh
	  super.init(attrs);
	}
}
