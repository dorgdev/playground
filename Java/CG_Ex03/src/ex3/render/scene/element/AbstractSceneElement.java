package ex3.render.scene.element;

import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.utils.Utils;

/**
 * An abstract class representing a scene element.
 * @author dor
 */
public abstract class AbstractSceneElement implements SceneElement {

	/** The material from which the element is made of */
	public Material material;
	/** Use acceleration or not for intersecting with the element */
	protected boolean useAcceleration;
	
	/**
	 * Creates a new AbstractSceneElement.
	 */
	public AbstractSceneElement() {
		useAcceleration = Utils.DEFAULT_USE_ACCELERATION;
		material = new Material();
	}
	
	/**
	 * Initializes the element from a given map of materials' values
	 * @param attributes Initialization values
	 */
	public void init(Map<String, String> attributes) {
		material.init(attributes);
	}
	
	/* (non-Javadoc)
	 * @see ex3.render.raytrace.SceneElement#emittedLight()
	 */
	@Override
	public RGB mtlEmissionLight(Point3D p) {
		return material.emittedLight;
	}

	@Override
	public RGB mtlAmbientFactor(Point3D p) {
		return material.ambientFactor;
	}
	
	@Override
	public RGB mtlDiffuseFactor(Point3D p) {
	  return material.diffuseFactor;
	}
	
	@Override
	public RGB mtlSpecularFactor(Point3D p) {
	  return material.specularFactor;
	}
	
	@Override
	public int mtlShininessPower(Point3D p) {
	  return material.shininess;
	}
	
	@Override
	public double mtlReflectanceFactor(Point3D p) {
	  return material.reflectance;
	}
	
	@Override
	public double mtlTransparencyFactor(Point3D p) {
		return material.transparency;
	}
	
	@Override
	public void setAccelerationMode(boolean useAcceleration) {
		this.useAcceleration = useAcceleration;
	}
}
