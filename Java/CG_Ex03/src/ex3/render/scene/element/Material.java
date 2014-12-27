package ex3.render.scene.element;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.utils.Initable;
import ex3.utils.Utils;

/**
 * Represent a material with color factors (ambient, emission, shininess, etc.),
 * and reflecting and transparency behaviors.
 * @author dor
 */
public class Material implements Initable {

	// Default material's values
	public static final RGB DEFAULT_EMISSION = new RGB(0, 0, 0);
	public static final RGB DEFAULT_AMBIENT = new RGB(0.1, 0.1, 0.1);
	public static final RGB DEFAULT_SPECULAR = new RGB(1, 1, 1);
	public static final RGB DEFAULT_DIFFUSE = new RGB(0.7, 0.7, 0.7);
	public static final int DEFAULT_SHININESS = 100;
	public static final double DEFAULT_REFLECTANCE = 0;
	public static final double DEFAULT_TRANSPARENCY = 0;
	
	/** Light emitted by the material */
	public RGB emittedLight;
	/** Ambient factor of the material */
	public RGB ambientFactor;
	/** Specularity factor of the material */
	public RGB specularFactor;
	/** Diffuse factor of the material */
	public RGB diffuseFactor;
	/** Shininess power of the material */
	public int shininess;
	/** Reflectance factor of the material */
	public double reflectance;
	/** Transparency factor of the material */
	public double transparency;

	/**
	 * Creates a new Material from the default values.
	 */
	public Material() {
		emittedLight = DEFAULT_EMISSION;
		ambientFactor = DEFAULT_AMBIENT;
		specularFactor = DEFAULT_SPECULAR;
		diffuseFactor = DEFAULT_DIFFUSE;
		shininess = DEFAULT_SHININESS;
		reflectance = DEFAULT_REFLECTANCE;
		transparency = DEFAULT_TRANSPARENCY;
	}
	
	@Override
	public void init(Map<String, String> attributes) {
		// Emitted light
		if (attributes.containsKey(Utils.MTL_EMISSION_ATTR)) {
			emittedLight = Utils.point2rgb(Point3D.ParsePoint3D(
					attributes.get(Utils.MTL_EMISSION_ATTR)));
		}
		// Ambient light factor
		if (attributes.containsKey(Utils.MTL_AMBIENT_ATTR)) {
			ambientFactor = Utils.point2rgb(Point3D.ParsePoint3D(
					attributes.get(Utils.MTL_AMBIENT_ATTR)));
		}
		// Specularity factor
		if (attributes.containsKey(Utils.MTL_SPECULAR_ATTR)) {
			specularFactor = Utils.point2rgb(Point3D.ParsePoint3D(
					attributes.get(Utils.MTL_SPECULAR_ATTR)));
		}
		// Diffuse factor
		if (attributes.containsKey(Utils.MTL_DIFFUSE_ATTR)) {
			diffuseFactor = Utils.point2rgb(Point3D.ParsePoint3D(
					attributes.get(Utils.MTL_DIFFUSE_ATTR)));
		}
		// Shininess power
		if (attributes.containsKey(Utils.MTL_SHININESS_ATTR)) {
			shininess = Integer.parseInt(attributes.get(Utils.MTL_SHININESS_ATTR));
		}
		// Reflectance
		if (attributes.containsKey(Utils.MTL_REFLECTANCE_ATTR)) {
			reflectance = Double.parseDouble(attributes.get(Utils.MTL_REFLECTANCE_ATTR));
		}
		// Transparency
		if (attributes.containsKey(Utils.MTL_TRANSPARENCY_ATTR)) {
			transparency = Double.parseDouble(attributes.get(Utils.MTL_TRANSPARENCY_ATTR));
		}
	}

}
