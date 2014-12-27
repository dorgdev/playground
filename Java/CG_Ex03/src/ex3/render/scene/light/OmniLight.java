package ex3.render.scene.light;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;



/**
 * Represents a pointed light (imni-light).
 * @author dor
 */
public class OmniLight extends Light {

	/**
	 * Constructs a new OmniLight.
	 */
	public OmniLight() {
		super();
	}
	
	@Override
	public void init(Map<String, String> attrs) {
		super.init(attrs);
	}

	@Override
	public RGB getColor(Point3D point) {
		double dist = position.distance(point);
		double factor = kc + kl * dist + kq * dist * dist;
		// When the distance is 0, the intensity is the color at source
		if (factor == 0) {
			return color;
		}
		RGB rc = new RGB(color);
		rc.factor(1 / factor);
		return rc;
	}
}
