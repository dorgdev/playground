package ex3.render.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ex3.math.Point3D;
import ex3.math.Ray;
import ex3.math.Vec;
import ex3.utils.Utils;


/**
 * This class represents the camera (the eye of the scene).
 * It can produce the rays of every pixel given to it.
 * 
 * @author dor
 */
public class Camera {

	// The point where the camera is
	private Point3D eye;
	// The direction the camera faces
	private Vec direction;
	// The up-vector of the camera
	private Vec up;
	// The right-vector of the camera
	private Vec right;
	// The distance from the camera to the screen
	private double screenDist;
	// The central point of the screen
	private Point3D screenCenter;
	// The size of the each pixel equivalent to the camera's screen
	private double pixelSize;
	// The height of the canvas
	private int canvasHeight;
	// The width of the canvas
	private int canvasWidth;
	//
	
	/**
	 * Builds a camera from a set of attributes and the ratio between the width
	 * and the height of the screen (width/height).
	 * @param attrs The attributes defining the camera
	 * @param height The height of the canvas (for ray calculation)
	 * @param width The width of the canvas (for ray calculation)
	 */
	public Camera(Map<String, String> attrs, int height, int width) {
		this.canvasHeight = height;
		this.canvasWidth = width;
		
		// Eye
		eye = Point3D.ParsePoint3D(Utils.getValue(attrs, Utils.EYE_ATTR));
		
		// Direction (explicitly or implicitly)
		if (attrs.containsKey(Utils.DIRECTION_ATTR)) {
			direction = Vec.parseVec(attrs.get(Utils.DIRECTION_ATTR)); 
		} else {
			Point3D lookAt = Point3D.ParsePoint3D(attrs.get(Utils.LOOK_AT_ATTR));
			direction = lookAt.vecFrom(eye);
		}
		direction.normalize();  // Make sure the direction is normalized
		
		// Up vector
		up = Vec.parseVec(attrs.get(Utils.UP_DIRECTION_ATTR));
		up = Utils.fixOrthogonal(direction, up);
		if (up == null) {
			throw new IllegalArgumentException("Camera's direction and up vector " +
					"shouldn't be linearly dependent!");
		}
		up.normalize();  // Make sure up is normalized
		
		// Right vector
		right = Vec.crossProd(direction, up);
		right.normalize();
		
		// Screen's distance
		screenDist = Double.parseDouble(Utils.getValue(attrs, Utils.SCREEN_DIST_ATTR));
		
		// Screen's center
		screenCenter = eye.addVec(Vec.scale(screenDist, direction));
		
		// Screen dimensions
		pixelSize = Double.parseDouble(Utils.getValue(attrs, Utils.SCREEN_WIDTH_ATTR)) / width;
	}
	
	/**
	 * Returns a vector from the given point to the camera (the scene's eye)
	 * @param point The point from which the vector starts
	 * @return A vector from the point to the eye
	 */
	public Vec rayToEye(Point3D point) {
		return eye.vecFrom(point);
	}
	
	/**
	 * Produces the rays for a specific pixel on the screen.
	 * The returned list will have N*N rays, each for the center location of each
	 * sub-frame in the pixel, assuming the pixel is divided into N*N sub-frames
	 * (used for super-sampling).
	 * @param x The x axis of the pixel on the screen
	 * @param y The y axis of the pixel on the screen
	 * @param superSampling Super sampling width for multiple rays
	 * @return All the rays leaving the camera's eye and going through the pixel's
	 * equivalent location on the screen.
	 */
	public List<Ray> getRaysForPixel(int x, int y, int superSampling) {
		double pixelCenterX = (x - (canvasWidth / 2)) * pixelSize;
		double pixelCenterY = ((canvasHeight / 2) - y) * pixelSize;
		Point3D pixelCenter = screenCenter.addVec(Vec.scale(pixelCenterX, right))
													.addVec(Vec.scale(pixelCenterY, up));
		return getRaysForPixel(pixelCenter, superSampling);
	}
	
	/**
	 * Using the center of the pixel, create N*N rays from the eye to the pixel,
	 * each hitting a different cell's center in an N*N grid in the pixel.
	 * 
	 * The idea behind the math:
	 * Find the most left-bottom part's center, and using the size of the pixel,
	 * find the next center each time.
	 * 
	 * @param pixelCenter The center of the pixel.
	 * @param superSampling Super sampling width for multiple rays
	 * @return All the rays hitting the pixel
	 */
	private List<Ray> getRaysForPixel(Point3D pixelCenter, int superSampling) {
		int numRays = superSampling * superSampling;
		List<Ray> rays = new ArrayList<Ray>(numRays);
		// Calculate the size of each internal pixel size
		double scale = pixelSize / superSampling;
		Vec partUp = Vec.scale(scale, this.up);
		Vec partRight = Vec.scale(scale, this.right);
		// Calculate the starting point for the loop
		Point3D rowStart = pixelCenter.addVec(
				Vec.scale((superSampling - 1) * 0.5, Vec.negate(partRight))).addVec(
				Vec.scale((superSampling - 1) * 0.5, Vec.negate(partUp))); 
		// Loop over the N*N parts
		for (int i = 0; i < superSampling; ++i) {
			Point3D current = rowStart;
			for (int j = 0; j < superSampling; ++j) {
				rays.add(new Ray(eye, current.vecFrom(eye)));
				current = current.addVec(partRight);
			}
			rowStart = rowStart.addVec(partUp);
		}
		return rays;
	}
}
