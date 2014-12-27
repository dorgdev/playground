package ex3.render.raytrace;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.math.Ray;
import ex3.math.Vec;
import ex3.parser.SceneDescriptor;
import ex3.render.IRenderer;
import ex3.render.scene.Camera;
import ex3.render.scene.Hit;
import ex3.render.scene.element.SceneElement;
import ex3.render.scene.light.Light;
import ex3.utils.Utils;

public class RayTracer implements IRenderer {
	
	// General scene attribtues
	private SceneDescriptor scene;
	private int width;
	private int height;
	private int superSampling;
	// The scene's camera
	private Camera camera;
	// Background color (for non-intersecting rays)
	private RGB bgColor;
	// Ambient light
	private RGB ambientLight;
	// Background texture
	private BufferedImage bgImage;
	// Max recursion depth
	private int maxRecDepth;
	// Whether we should use acceleration in the scene
	private boolean useAcceleration;
	
	/**
	 * Inits the renderer with scene description and sets the target canvas to
	 * size (width X height). After init renderLine may be called
	 * 
	 * @param sceneDesc
	 *            Description data structure of the scene
	 * @param width
	 *            Width of the canvas
	 * @param height
	 *            Height of the canvas
	 */
	@Override
	public void init(SceneDescriptor sceneDesc, int width, int height) {
		this.scene = sceneDesc;
		this.width = width;
		this.height = height;
		this.bgColor = new RGB();
		this.ambientLight = new RGB();
		this.bgImage = null;
		this.useAcceleration = Utils.DEFAULT_USE_ACCELERATION;
		
		initGeneralScene();
		initCamera();
	}

	/**
	 * Initializes general attributes of the scene
	 */
	public void initGeneralScene() {
		Map<String, String> attrs = scene.getSceneAttributes();
		// Background color
		if (attrs.containsKey(Utils.BG_COLOR_ATTR)) {
			bgColor = Utils.point2rgb(Point3D.ParsePoint3D(
					attrs.get(Utils.BG_COLOR_ATTR)));
		}
		
		// Background texture
		if (attrs.containsKey(Utils.BG_TEXTURE_ATTR)) {
			try {
				bgImage = ImageIO.read(Utils.getResource(attrs.get(Utils.BG_TEXTURE_ATTR)));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		// Ambient light
		if (attrs.containsKey(Utils.AMBIENT_LIGHT_ATTR)) {
			ambientLight = Utils.point2rgb(Point3D.ParsePoint3D(
					scene.getSceneAttributes().get(Utils.AMBIENT_LIGHT_ATTR)));
		}
		
		// Super sampling width
		if (attrs.containsKey(Utils.SUPER_SAMPLING_ATTR)) {
			superSampling = Integer.parseInt(attrs.get(Utils.SUPER_SAMPLING_ATTR));
		} else {
			superSampling = Utils.DEFAULT_SAMPLING;
		}
		
		// Max recursion depth
		if (attrs.containsKey(Utils.MAX_RECURSION_ATTR)) {
			maxRecDepth = Integer.parseInt(attrs.get(Utils.MAX_RECURSION_ATTR));
		} else {
			maxRecDepth = Utils.DEFAULT_MAX_RECURSION_DEPTH;
		}
		// Use acceleration
		if (attrs.containsKey(Utils.USE_ACCELERATION_ATTR)) {
			useAcceleration =
					Integer.parseInt(attrs.get(Utils.USE_ACCELERATION_ATTR)) == 1;
			for (SceneElement elem : scene.getElements()) {
				elem.setAccelerationMode(useAcceleration);
			}
		}
	}
	
	/**
	 * Initializes the camera for the scene
	 */
	public void initCamera() {
		camera = new Camera(scene.getCameraAttributes(), height, width);
	}
	
	/**
	 * Renders the given line to the given canvas. Canvas is of the exact size
	 * given to init. This method must be called only after init.
	 * 
	 * @param canvas
	 *            BufferedImage containing the partial image
	 * @param line
	 *            The line of the image that should be rendered.
	 */
	@Override
	public void renderLine(BufferedImage canvas, int line) {
		for (int col = 0; col < width; ++col) {
			List<Ray> rays = camera.getRaysForPixel(col, line, superSampling);
			RGB background = getDefaultBackground(col, line);
			int r = 0, g = 0, b = 0;
			for (Ray ray : rays) {
	      Color c = castRay(ray, background, 0).toColor();
	      r += c.getRed();
	      g += c.getGreen();
	      b += c.getBlue();
      }
			r /= rays.size();
			g /= rays.size();
			b /= rays.size();
			canvas.setRGB(col, line, new Color(r, g, b).getRGB());
		}
	}
	
	/**
	 * Returns the corresponding color on the background texture (if exists).
	 * Otherwise, returns null.
	 * @param col The pixel's col (width) index on the canvas screen
	 * @param line The pixel's line (height) index on the canvas screen
	 * @return The default background color
	 */
	private RGB getDefaultBackground(int col, int line) {
		// Check if there's a background image
		if (bgImage == null) {
			return null;
		}
		int x = bgImage.getWidth() * col / width;
		int y = bgImage.getHeight() * line / height;
		return new RGB(new Color(bgImage.getRGB(x, y)));
	}
	
	/**
	 * Shoots a ray into the scene and diagnoses its color.
	 * @param ray The ray shot into the scene
	 * @param bg The default background for the image
	 * @param level Current level of recursion
	 * @return The color of the intersection point (if exists)
	 */
	private RGB castRay(Ray ray, RGB bg, int level) {
		// Make sure we need to check ray casting
		if (level == maxRecDepth) {
			return RGB.NO_COLOR;
		}
		// Find the closest intersection point
		Hit closest = findClosestIntersection(ray);
		// Get the color at that intersection point
		if (closest != null) {
			return getColor(closest, ray, level, bg);
		} else {
			if (bg != null) {
				return bg;
			}
			return new RGB(bgColor);
		}
	}

	/**
	 * Find the closest intersection point of a ray with another element in the
	 * scene. Returns null in case no such intersection exists.
	 * @param ray The intersecting ray
	 * @return A hit instance representing an intersection point
	 */
	private Hit findClosestIntersection(Ray ray) {
		Hit closest = null;
		for (SceneElement elem : scene.getElements()) {
	    Hit intersection = elem.intersect(ray);
	    if (closest == null) {
	    	closest = intersection;
	    } else if (intersection != null) {
	    	if (ray.p.distance(closest.p) > ray.p.distance(intersection.p)) {
	    		closest = intersection;
	    	}
	    }
    }
		return closest;
	}
	
	/**
	 * Returns the color of an intersection point, according to its parameters,
	 * and the lights and objects in the scene.
	 * @param hit The intersection instance
	 * @param ray The ray hitting at the intersection point
	 * @param level The level of ray casting we're in
	 * @param bg Original background color for transparencyc color
	 * @return The color viewable from the incoming ray of the hit
	 */
	private RGB getColor(Hit hit, Ray ray, int level, RGB bg) {
		RGB rgb = new RGB(calcEmissionColor(hit));
		rgb.add(calculateAmbientColor(hit));
		// Add material specific color
		for (Light light : scene.getLights()) {
	    Ray lightRay = ConstructRayFromLight(hit, light);
	    if (!occluded(lightRay, hit.p)) {
	    	rgb.add(calculateDiffuseColor(hit, light));
	    	rgb.add(calculateSpecularColor(ray, hit, light));
	    }
    }
		// Check if we need to recurse the check for the reflection
		if (hit.surface.mtlReflectanceFactor(hit.p) > 0) {
			Ray reflected = new Ray(hit.p, ray.v.reflect(hit.normal));
			RGB reflectedColor = castRay(reflected, null, level + 1);
			reflectedColor.factor(hit.surface.mtlReflectanceFactor(hit.p));
			rgb.add(reflectedColor);
		}
		// Check if we need to recurse the check for transparency
		if (hit.surface.mtlTransparencyFactor(hit.p) > 0) {
			Ray refracted = new Ray(hit.p, ray.v);
			RGB refractedColor = castRay(refracted, bg, level + 1);
			refractedColor.factor(hit.surface.mtlTransparencyFactor(hit.p));
			rgb.add(refractedColor);
		}
		return rgb;
	}
	
	/**
	 * Construct the ray from the light source to the hit point.
	 * @param hit A hit point
	 * @param light A light source
	 * @return Ray starting at the light source with the direction towards hit
	 */
	private Ray ConstructRayFromLight(Hit hit, Light light) {
		Vec v = hit.p.vecFrom(light.getPosition());
		return new Ray(light.getPosition(), v);
	}
	
	/**
	 * Checks whether there's an object between the ray starting location and the
	 * intersection point given (i.e, if the element is occluded from the light
	 * at the start of the ray).
	 * @param ray A ray from a light source
	 * @param hit An intersection point with an element
	 * @return True if the element is occluded, false otherwise
	 */
	private boolean occluded(Ray ray, Point3D hit) {
		// First, find any intersection point
		Hit closest = findClosestIntersection(ray);
		// This shouldn't happen, as the ray should start at the light source
		// towards the intersection point, so there should be at least one point.
		// However, miscalculation (of double's precision) might cause it, so
		// return false (as nothing else is in the way)
		if (closest == null) {
			return false;
		}
		// Check if the intersection point is beyond the light source.
		if (Utils.gt(ray.p.distance(hit), ray.p.distance(closest.p))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Calculates the emitted color in the intersection point
	 * @param hit The intersection point descriptor
	 * @return The color at the intersection point
	 */
	private RGB calcEmissionColor(Hit hit) {
		return hit.surface.mtlEmissionLight(hit.p);
	}

	/**
	 * Calculates the ambient color in the intersection point
	 * @param hit The intersection point descriptor
	 * @return The color at the intersection point
	 */
	private RGB calculateAmbientColor(Hit hit) {
		RGB rc = new RGB(hit.surface.mtlAmbientFactor(hit.p));
		rc.factor(ambientLight);
		return rc;
	}
	
	/**
	 * Calculates the diffuse color in the intersection point. Assumes that point
	 * is not occluded from the light source. 
	 * @param hit An intersection point descriptor
	 * @param light A light source
	 * @return The color at the intersection point
	 */
	private RGB calculateDiffuseColor(Hit hit, Light light) {
		// Kd
		RGB rc = new RGB(hit.surface.mtlDiffuseFactor(hit.p));
		// Kd * Il
		rc.factor(light.getColor(hit.p));
		// (N.L)
		Vec v = light.getPosition().vecFrom(hit.p);
		v.normalize();
		double nDotL = Vec.dotProd(hit.normal, v);
		// Kd * (N.L) * Il
		rc.factor(nDotL);
		
		return rc;
	}
	
	/**
	 * Calculates the specular color in the intersection point. Assumes that
	 * point is not occluded from the light source.
	 * @param ray The ray hitting at the intersection point. 
	 * @param hit An intersection point descriptor
	 * @param light A light source
	 * @return The color at the intersection point
	 */
	private RGB calculateSpecularColor(Ray ray, Hit hit, Light light) {
		// L (opposite direction for reflection calculation)
		Vec l = hit.p.vecFrom(light.getPosition());
		l.normalize();
		// R
		Vec r = l.reflect(hit.normal);
		// V
		Vec v = Vec.negate(ray.v);
		v.normalize();
		// (V.R)^n
		double vDotR = v.dotProd(r);
		// Make sure the angle is positive (so some reflection could be seen)
		if (vDotR < 0) {
			return new RGB(0, 0, 0);
		}
		double vDotRPowerN = Math.pow(vDotR, hit.surface.mtlShininessPower(hit.p));
		// Ks
		RGB rc = new RGB(hit.surface.mtlSpecularFactor(hit.p));
		// Ks * Il
		rc.factor(light.getColor(hit.p));
		// Ks * (V.R)^n * Il
		rc.factor(vDotRPowerN);
		
		return rc;
	}
}
