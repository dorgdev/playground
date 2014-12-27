package ex3.render.scene.element;

import java.util.List;
import java.util.Map;

import ex3.math.Point3D;
import ex3.utils.Utils;

/**
 * Represents a box aligned with the axis.
 * @author dor
 */
public class AlignedBox extends BoundingBox {

	/**
	 * Creates a new AlignedBox.
	 */
	public AlignedBox() {
		super();
	}
	
	@Override
	public void init(Map<String, String> attrs) {
	  // Get points
		List<Point3D> points =
			Point3D.ParseSeveral(Utils.getValue(attrs, Utils.ALINGED_BOX_VERTICES_ATTR));
		if (points.size() != 2) {
			throw new IllegalArgumentException(
					"2 points exactly should be given as the box's vertices!");
		}
		super.init(attrs, points.get(0), points.get(1));
	}

	protected void initMinMaxPoints(Point3D p1, Point3D p2) {
		max = new Point3D(
				(p1.x > p2.x ? p1.x : p2.x),
				(p1.y > p2.y ? p1.y : p2.y),
				(p1.z > p2.z ? p1.z : p2.z));
		min = new Point3D(
				(p1.x < p2.x ? p1.x : p2.x),
				(p1.y < p2.y ? p1.y : p2.y),
				(p1.z < p2.z ? p1.z : p2.z));
	}
}
