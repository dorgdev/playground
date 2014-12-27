package ex3.render.scene.light;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.utils.Initable;
import ex3.utils.Utils;


/**
 * Represents a light in the scene.
 * @author dor
 */
public abstract class Light implements Initable {

	// Default values for lights
	public static final RGB DEFAULT_LIGHT_COLOR = new RGB(1, 1, 1);
	public static final Point3D DEFAULT_LIGHT_POSITION = new Point3D(10, 10, 10);
	public static final double DEFAULT_KC_FACTOR = 1;
	public static final double DEFAULT_KL_FACTOR = 0;
	public static final double DEFAULT_KQ_FACTOR = 0;

	/** The color spread by this light source */
	protected RGB color;
	/** The position of the light source */
	protected Point3D position;
	/** The attenuation factors of the light */
	protected double kc;
	protected double kl;
	protected double kq;
	
	/**
	 * Constructs a new light.
	 */
	public Light() {
	}
	
	@Override
	public void init(Map<String, String> attrs) {
		// Light's color
		if (attrs.containsKey(Utils.LIGHT_COLOR_ATTR)) {
			color = Utils.point2rgb(Point3D.ParsePoint3D(
					attrs.get(Utils.LIGHT_COLOR_ATTR)));
		} else {
			color = DEFAULT_LIGHT_COLOR;
		}
		// Light's position
		if (attrs.containsKey(Utils.LIGHT_POS_ATTR)) {
			position = Point3D.ParsePoint3D(attrs.get(Utils.LIGHT_POS_ATTR));
		} else {
			position = DEFAULT_LIGHT_POSITION;
		}
		// Light attenuation - kc
		if (attrs.containsKey(Utils.LIGHT_KC_ATTR)) {
			kc = Double.parseDouble(attrs.get(Utils.LIGHT_KC_ATTR));
		} else {
			kc = DEFAULT_KC_FACTOR;
		}
		// Light attenuation - kl
		if (attrs.containsKey(Utils.LIGHT_KL_ATTR)) {
			kl = Double.parseDouble(attrs.get(Utils.LIGHT_KL_ATTR));
		} else {
			kl = DEFAULT_KL_FACTOR;
		}
		// Light attenuation - kq
		if (attrs.containsKey(Utils.LIGHT_KQ_ATTR)) {
			kq = Double.parseDouble(attrs.get(Utils.LIGHT_KQ_ATTR));
		} else {
			kq = DEFAULT_KQ_FACTOR;
		}
		// Check at least one of the attenuation is not 0:
		if (kc == 0 && kl == 0 && kq == 0) {
			throw new IllegalArgumentException("At least one of the light's " +
					"attenuation factor should be non-zero!");
		}
	}

	/**
	 * Returns the intensity of the light (color) spread at a given point.
	 * @param point The point to calculate the intensity of light there.
	 * @return The light's intensity in the given point
	 */
	public abstract RGB getColor(Point3D point);
	
	/**
	 * @return The position of the light source
	 */
	public Point3D getPosition() {
		return position;
	}
}
