package ex3.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ex3.render.scene.SceneFactory;
import ex3.render.scene.element.SceneElement;
import ex3.render.scene.light.Light;

/**
 * Contains a scene description. Ensures syntactic correctness but not semantic.
 */
public class SceneDescriptor {

	protected Map<String, String> sceneAttributes;
	protected Map<String, String> cameraAttributes;
	protected List<Light> lights;
	protected List<SceneElement> elements;
	
	/**
	 * Constructs scene description from given XML formatted text. Verifies
	 * syntactic requirements that at least one scene element and one camera
	 * element should exist.
	 * 
	 * @param text
	 *            XML string
	 * @throws ParseException
	 */
	public void fromXML(String text) throws ParseException {
		lights = new ArrayList<Light>();
		elements = new ArrayList<SceneElement>();

		SceneXMLParser parser = new SceneXMLParser();
		parser.parse(text, this);

		// Verify that scene structure is syntactically correct!!! see the
		// example in the PDF file given to you
		if (sceneAttributes == null) {
			throw new ParseException("No scene element found!", 0);
		}
		if (cameraAttributes == null) {
			throw new ParseException("No camera element found!", 0);
		}
	}

	public Map<String, String> getSceneAttributes() {
		return sceneAttributes;
	}

	public void setSceneAttributes(Map<String, String> sceneAttributes) {
		this.sceneAttributes = sceneAttributes;
	}

	public Map<String, String> getCameraAttributes() {
		return cameraAttributes;
	}

	public void setCameraAttributes(Map<String, String> cameraAttributes) {
		this.cameraAttributes = cameraAttributes;
	}

	public void addObject(Element elem) {
    if (SceneFactory.isLight(elem.getName())) {
    	lights.add(SceneFactory.buildLight(elem));
    } else {
    	elements.add(SceneFactory.buildSceneElement(elem));
    }
	}
	
	public List<SceneElement> getElements() {
		return elements;
	}
	
	public List<Light> getLights() {
		return lights;
	}
}
