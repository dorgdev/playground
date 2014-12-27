package ex3.render.scene;

import ex3.parser.Element;
import ex3.render.scene.element.AlignedBox;
import ex3.render.scene.element.Circle;
import ex3.render.scene.element.Pyramid;
import ex3.render.scene.element.SceneElement;
import ex3.render.scene.element.Sphere;
import ex3.render.scene.element.Trimesh;
import ex3.render.scene.light.Light;
import ex3.render.scene.light.OmniLight;
import ex3.render.scene.light.SpotLight;
import ex3.utils.Utils;

/**
 * Builds scene elements from the SceneXMLParser parsed elements.
 *  
 * @author dor
 */
public class SceneFactory {

	public static SceneElement buildSceneElement(Element e) {
		SceneElement se = null;
		String name = e.getName();
		if (name.equals(Utils.SPHERE_TAG)) {
			se = new Sphere();
		} else if (name.equals(Utils.TRIMESH_TAG)) {
			se = new Trimesh();
		} else if (name.equals(Utils.CIRCLE_TAG)) {
			se = new Circle();
		} else if (name.equals(Utils.PYRAMID_TAG)) {
			se = new Pyramid();
		} else if (name.equals(Utils.ALINGED_BOX_TAG)) {
			se = new AlignedBox();
		}
		if (se != null) {
			se.init(e.getAttributes());
			return se;
		}
		// Couldn't create a scene element
		throw new IllegalArgumentException(
				"Couldn't parse the given element: " + e.getName());
	}
	
	public static Light buildLight(Element e) {
		String name = e.getName();
		Light light = null;
		if (name.equals(Utils.OMNI_LIGHT_TAG)) {
			light = new OmniLight();
		}
		if (name.equals(Utils.SPOT_LIGHT_TAG)) {
			light = new SpotLight();
		}
		if (light != null) {
			light.init(e.getAttributes());
			return light;
		}
		// Couldn't create a light instance
		throw new IllegalArgumentException(
				"Couldn't parse the given element: " + e.getName());
	}
	
	public static boolean isLight(String name) {
		if (name.equals(Utils.OMNI_LIGHT_TAG) ||
				name.equals(Utils.SPOT_LIGHT_TAG)) {
			return true;
		}
		return false;
	}
	
}
