package ex3.render.scene.light;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.math.Vec;
import ex3.utils.Utils;


/**
 * Represents a spot light instance in the scene.
 * @author dor
 */
public class SpotLight extends Light {

	/** The direction of the spot light */
	private Vec direction;

	/**
	 * Constructs a new SpotLight.
	 */
	public SpotLight() {
		super();
	}
	
	@Override
	public void init(Map<String, String> attrs) {
		super.init(attrs);
		// Direction
		direction = Vec.parseVec(Utils.getValue(attrs, Utils.LIGHT_DIRECTION_ATTR));
		direction.normalize();
	}
	
	@Override
	public RGB getColor(Point3D point) {
		RGB rc = new RGB(color);
		// L
		Vec toPoint = point.vecFrom(position);
		double dist = toPoint.length();
		// (D.L)
	  toPoint.normalize();
	  double factor = toPoint.dotProd(direction);
	  rc.factor(factor);
	  // 1/(Kc + Kl * d + kq * d^2)
		factor = kc + kl * dist + kq * dist * dist;
		// When the distance is 0, the intensity is the color at source
		if (factor == 0) {
			return color;
		}
		rc.factor(1 / factor);
	  return rc;
	}
	
}
