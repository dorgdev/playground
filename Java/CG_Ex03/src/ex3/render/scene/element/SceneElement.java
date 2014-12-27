package ex3.render.scene.element;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.utils.Initable;

/**
 * This interface represent an element in the scene.
 * It can be intersect with and return the intersection point (if exist).
 * 
 * @author dor
 */
public interface SceneElement extends Initable, Intersectable {

	/**
	 * Returns the light emitted from the element.
	 * @param p The point to which the factor is required
	 * @return The light emitted from the element
	 */
	public RGB mtlEmissionLight(Point3D p);

	/**
	 * Returns the ambient light factor of the element.
	 * @param p The point to which the factor is required
	 * @return The ambient light factor of the element
	 */
	public RGB mtlAmbientFactor(Point3D p);

	/**
	 * Returns the diffuse light factor of the element.
	 * @param p The point to which the factor is required
	 * @return The diffuse light factor of the element
	 */
	public RGB mtlDiffuseFactor(Point3D p);

	/**
	 * Returns the specular light factor of the element.
	 * @param p The point to which the factor is required
	 * @return The specular light factor of the element
	 */
	public RGB mtlSpecularFactor(Point3D p);

	/**
	 * Returns the shininess power of the element.
	 * @param p The point to which the factor is required
	 * @return The shininess power of the element
	 */
	public int mtlShininessPower(Point3D p);

	/**
	 * Returns the reflectance factor of the element.
	 * @param p The point to which the factor is required
	 * @return The reflectance factor of the element
	 */
	public double mtlReflectanceFactor(Point3D p);

	/**
	 * Returns the transparency factor of the element.
	 * @param p The point to which the factor is required
	 * @return The transparency factor of the element
	 */
	public double mtlTransparencyFactor(Point3D p);

	/**
	 * Tells the element to use/not use an accelerated intersection mode (if
	 * applicable to it).
	 * @param useAcceleration Whether to use acceleration or not.
	 */
	public void setAccelerationMode(boolean useAcceleration);
}
