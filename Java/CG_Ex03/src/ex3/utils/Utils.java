package ex3.utils;

import java.io.File;
import java.util.Map;

import ex3.math.Point3D;
import ex3.math.RGB;
import ex3.math.Vec;


/**
 * This class hold utilities methods and definitions, widely used in the code.
 * 
 * @author dor
 */
public final class Utils {
	
	/*
	 * XML parsing values
	 */

	// The general attributes of the scene
	public static final String BG_COLOR_ATTR = "background-col";
	public static final String BG_TEXTURE_ATTR = "background-tex";
	public static final String AMBIENT_LIGHT_ATTR = "ambient-light";
	public static final String SUPER_SAMPLING_ATTR = "super-samp-width";
	public static final String MAX_RECURSION_ATTR = "max-recursion-level";
	public static final String USE_ACCELERATION_ATTR = "use-acceleration";
	public static final boolean DEFAULT_USE_ACCELERATION = false;
	
	// The possible attributes of a camera
	public static final String EYE_ATTR = "eye";
	public static final String DIRECTION_ATTR = "direction";
	public static final String LOOK_AT_ATTR = "look-at";
	public static final String UP_DIRECTION_ATTR = "up-direction";
	public static final String SCREEN_DIST_ATTR = "screen-dist";
	public static final String SCREEN_WIDTH_ATTR = "screen-width";

	// General object related tags and attributes
	public static final String MTL_EMISSION_ATTR = "mtl-emission";
	public static final String MTL_AMBIENT_ATTR = "mtl-ambient";
	public static final String MTL_SPECULAR_ATTR = "mtl-specular";
	public static final String MTL_DIFFUSE_ATTR = "mtl-diffuse";
	public static final String MTL_SHININESS_ATTR = "mtl-shininess";
	public static final String MTL_REFLECTANCE_ATTR = "reflectance";
	public static final String MTL_TRANSPARENCY_ATTR = "transparency";
	public static final String MTL_TEXTURE_ATTR = "mtl-texture";
	
	// Sphere related tags and attributes
	public static final String SPHERE_TAG = "sphere";
	public static final String SPHERE_CENTER_ATTR = "center";
	public static final String SPHERE_RADIUS_ATTR = "radius";
	public static final String SPHERE_TEXTURE_UP_ATTR = "texture-up";
	public static final String SPHERE_TEXTURE_CENTER_ATTR = "texture-center";
	
	// Trimesh related attributes
	public static final String TRIMESH_TAG = "trimesh";
	public static final String TRIANGLE_ATTR_PREFIX = "tri";
	
	// Pyramid related attributes
	public static final String PYRAMID_TAG = "pyramid";
	public static final String PYRAMID_BASE_ATTR = "base";
	public static final String PYRAMID_APEX_ATTR = "apex";
	
	// Circle related attributes
	public static final String CIRCLE_TAG = "circle";
	public static final String CIRCLE_CENTER_ATTR = "center";
	public static final String CIRCLE_RADIUS_ATTR = "radius";
	public static final String CIRCLE_INNER_RADIUS_ATTR = "inner-radius";
	public static final String CIRCLE_VEC1_ATTR = "vec1";
	public static final String CIRCLE_VEC2_ATTR = "vec2";
	
	// AlignedBox related attributes
	public static final String ALINGED_BOX_TAG = "box";
	public static final String ALINGED_BOX_VERTICES_ATTR = "vertices";
	
	// Light related attributes
	public static final String OMNI_LIGHT_TAG = "omni-light";
	public static final String SPOT_LIGHT_TAG = "spot-light";
	public static final String LIGHT_COLOR_ATTR = "color";
	public static final String LIGHT_DIRECTION_ATTR = "dir";
	public static final String LIGHT_POS_ATTR = "pos";
	public static final String LIGHT_KC_ATTR = "kc";
	public static final String LIGHT_KL_ATTR = "kl";
	public static final String LIGHT_KQ_ATTR = "kq";
	
	/** Default sampling when the super-sampling attribute is missing */
	public static final int DEFAULT_SAMPLING = 1;
	
	/** Default maximum recursion depth for ray tracing */
	public static final int DEFAULT_MAX_RECURSION_DEPTH = 10;
	
	/** For double calculation, use this tolerance to eliminate miscalculations */
	public static final double CLOSENESS_TOLERANCE = 0.0001;

	/** Holds the current scene file, for resources access */
	private static File sceneFile = null;
	
	/**
	 * Saves the current scene file for future access.
	 * @param SceneFile The current scene file
	 */
	public static void setSceneFile(File newSceneFile) {
		sceneFile = newSceneFile;
	}
	
	/**
	 * Retrieves the full path to a resource in the scene's directory according
	 * to it relative path
	 * @param relativePath The resource's relative path
	 * @return The full path to the resource
	 */
	public static File getResource(String relativePath) {
		return new File(sceneFile.getParent() + File.separator + relativePath);
	}
	
	/**
	 * @return A File instance for the scene's current file
	 */
	public static File getSceneFile() {
		return sceneFile;
	}
	
	/**
	 * Returns true if value1 is greater then to value2, up to a certain closeness
	 * tolerance (defined above).
	 * @param value1 First value
	 * @param value2 Second value
	 * @return True if value1 > value2
	 * @see Utils#CLOSENESS_TOLERANCE
	 */
	public static boolean gt(double value1, double value2) {
		return value1 - CLOSENESS_TOLERANCE > value2;
	}
	
	/**
	 * Returns true if value1 is equal to value2, up until a certain closeness
	 * tolerance (defined above).
	 * @param value1 First value
	 * @param value2 Second value
	 * @return True if value2 == value2
	 * @see Utils#CLOSENESS_TOLERANCE
	 */
	public static boolean eq(double value1, double value2) {
		return Math.abs(value1 - value2) <= CLOSENESS_TOLERANCE;
	}
	
	/**
	 * Returns an RGB instance from the 0-1 based point values. If a value
	 * @param p The point with coordinates representing the RGB values 
	 * @return An RGB instance
	 */
	public static RGB point2rgb(Point3D p) {
		return new RGB(p.x, p.y, p.z);
	}
	
	/**
	 * Gets 2 vectors and return a vector which orthogonal to the first vector
	 * and is on the plain created by both given vectors. Used for "fixing"
	 * directions of objects (such as the camera, etc.).
	 * If the vectors are linearly dependent, returns null.
	 * Note: Returned vector is not promised to be normalized.
	 * @param v1 The fixed vector.
	 * @param v2 Another vector, 
	 * @return Null if the vectors are linearly dependent, or a vector orthogonal
	 * to the first vector.
	 */
	public static Vec fixOrthogonal(Vec v1, Vec v2) {
		if (v1.dotProd(v2) == 0) {
			// Already orthogonal
			return v2;
		}
		Vec crossProdVec = Vec.crossProd(v2, v1);
		if (crossProdVec.lengthSquared() == 0) {
			// Linearly dependent vectors
			return null;
		}
		return Vec.crossProd(v1, crossProdVec);
	}
	
	/**
	 * A utility method to read from a map of value and make sure returned value
	 * is never null. If missing, an exception will be thrown.
	 * @param attrs The map of values to read the value from
	 * @param key The key of the value to read
	 * @return The value from the map
	 */
	public static String getValue(Map<String, String> attrs, String key) {
		if (attrs.containsKey(key)) {
			return attrs.get(key);
		}
		throw new IllegalArgumentException("Missing an attribute named: " + key);
	}
	
	/**
	 * Private CTOR. Can't create an instance of it.
	 */
	private Utils() {}
	
}
