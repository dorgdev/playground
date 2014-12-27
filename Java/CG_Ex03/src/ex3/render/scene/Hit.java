package ex3.render.scene;

import ex3.math.Point3D;
import ex3.math.Vec;
import ex3.render.scene.element.SceneElement;

/**
 * Represents a hit of a ray on a surface.
 * Every hit is represented by the point of intersection, the normal at that
 * point and the surface the ray hit (the element in the scene).
 * 
 * @author dor
 */
public class Hit {

	public Vec normal;
	public Point3D p;
	public SceneElement surface;
	
	public Hit(Vec normal, Point3D intersection, SceneElement surface) {
		this.normal = normal;
		this.p = intersection;
		this.surface = surface;
	}
	
}
