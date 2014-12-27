/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5;

import java.awt.Point;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.sun.opengl.util.FPSAnimator;

import ex5.math.Vec;
import ex5.models.AbstractModel;
import ex5.models.IRenderable;

/**
 * An OpenGL model viewer 
 */
public class Viewer implements GLEventListener {

	/** The default zoom to start with */
	public static double DEFAULT_ZOOM = -5;
	/** The zoom factor for each requested zoom unit */
	public static double ZOOM_FACTOR = 0.02;
	/** The size of the long edge of the projected screen */
	public static double PROJECTION_LONG_EDGE = 5;

	/** The width of the screen (not the view!) */
	private double screenWidth;
	/** The height of the screen (not the view!) */
	private double screenHeight;
	/** The current zoom (distance) of the camera from the scene */
	private double zoom;
	/** Whether we should display polygons as their lines (true), or filled (false) */
	private boolean isWireframe;
	/** Whether the axes origin should be displayed or not */
	private boolean isAxes;
	/** The currently displayed model */
	private IRenderable model;
	/** The animator playing current changes (for rotations) */
	private FPSAnimator ani;
	/** The next rotation angle to be applied */
	private double nextRotationAngle;
	/** The next rotation axis to rotate around (or null if no rotation is needed) */
	private Vec nextRotationAxis;
	/** The GL rotation matrix calculated so far */
	private double[] rotationMatrix;
	
	/**
	 * Creates a new Viewer instance.
	 */
	public Viewer() {
		zoom = DEFAULT_ZOOM;
		isWireframe = false;
		isAxes = true;
		nextRotationAngle = 0;
		nextRotationAxis = null;
		rotationMatrix = null;
		screenWidth = 0;
		screenHeight = 0;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		// Wire frame or not
		gl.glPolygonMode(GL.GL_FRONT, isWireframe ? GL.GL_LINE : GL.GL_FILL);
		// Z-Buffer method
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LESS);
		// Normalize (for scaling)
		gl.glEnable(GL.GL_NORMALIZE);
		// Light model
		gl.glEnable(GL.GL_LIGHTING);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		// Set the camera and push the current view matrix (before rendering scene)
		setupCamera(gl);
		gl.glPushMatrix();

		// Render the axis
		if (isAxes) {
			renderAxes(gl);
		}
		// Render the model
		model.render(gl);
		
		// Pop the original matrix (the model might have change the current matrix):
		gl.glPopMatrix();
	}

	@Override
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glEnable(GL.GL_CULL_FACE); // Enable back face culling
		gl.glCullFace(GL.GL_BACK);    // Set Culling Face To Back Face

		// Initialize display callback timer
		ani = new FPSAnimator(60, true);
		ani.add(drawable);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// Keep the width and height for the trackball feature
		screenWidth = width;
		screenHeight = height;

		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);

		// Clear current view
		gl.glLoadIdentity();

		// Set a basic perspective view (using a frustum):
		double frustumWidth = PROJECTION_LONG_EDGE / 2;
		double frustumHeight = PROJECTION_LONG_EDGE / 2;
		if (width < height) {
			frustumWidth *= screenWidth / screenHeight;
		} else {
			frustumHeight *= screenHeight / screenWidth;
		}
		gl.glFrustum(-frustumWidth, frustumWidth, -frustumHeight, frustumHeight, 1, 100);
	}

	/**
	 * Rotate model in a way that corresponds with a virtual trackball.
	 * 
	 * @param from
	 *            2D canvas point of drag beginning
	 * @param to
	 *            2D canvas point of drag ending
	 */
	public void trackball(Point from, Point to) {
		// Find the points coordinates on a unit sphere
		double fromX = 2 * from.getX() / screenWidth - 1;
		double fromY = 1 - 2 * from.getY() / screenHeight;
		double tmp = 1 - fromX * fromX - fromY * fromY;
		if (tmp < 0) {
			return;
		}
		double fromZ = Math.sqrt(tmp);
		double toX = 2 * to.getX() / screenWidth - 1;
		double toY = 1 - 2 * to.getY() / screenHeight;
		tmp = 1 - toX * toX - toY * toY;
		if (tmp < 0) {
			return;
		}
		double toZ = Math.sqrt(tmp);
		
		// Find a common perpendicular to be used as the rotation axis
		Vec fromVec = new Vec(fromX, fromY, fromZ);
		Vec toVec = new Vec(toX, toY, toZ);
		nextRotationAxis  = Vec.crossProd(fromVec, toVec);
		
		// Find the rotation angle (in degrees) between the vectors
		nextRotationAngle = Math.acos(Vec.dotProd(fromVec, toVec)) * 180 / Math.PI;
	}
	
	/**
	 * Zoom in or out of object in a certain amount of zoom units.
	 * The method translates the given size into real world distance according to
	 * the ZOOM_FACTOR constant.
	 * 
	 * @param s
	 *            Scalar
	 */
	public void zoom(int s) {
		zoom += s * ZOOM_FACTOR;
	}

	/**
	 * Toggle rendering method. Either wireframes (lines) or fully shaded
	 */
	public void toggleRenderMode() {
		isWireframe = !isWireframe;
	}
	
	/**
	 * Toggle whether little spheres are shown at the location of the light sources.
	 */
	public void toggleLightSpheres() {
		model.control(AbstractModel.TOGGLE_LIGHT_SPHERES, null);
	}

	/**
	 * Toggle whether axes are shown.
	 */
	public void toggleAxes() {
		isAxes = !isAxes;
	}
	
	/**
	 * Start redrawing the scene with 60 FPS
	 */
	public void startAnimation() {
		ani.start();
	}
	
	/**
	 * Stop redrawing the scene with 60 FPS
	 */
	public void stopAnimation() {
		ani.stop();
	}
	
	/**
	 * Sets the camera (zoom and rotation of the view).
	 * @param gl The GL instance to draw on
	 */
	private void setupCamera(GL gl) {
		gl.glMatrixMode(GL.GL_MODELVIEW);
		// If there's a rotation to make, do it before everything else
		gl.glLoadIdentity();
    if (rotationMatrix == null) {
			rotationMatrix = new double[16];
			gl.glGetDoublev(GL.GL_MODELVIEW, rotationMatrix, 0);
		}
		if (nextRotationAxis != null) {
			gl.glRotated(nextRotationAngle, nextRotationAxis.x, nextRotationAxis.y, nextRotationAxis.z);
			nextRotationAngle = 0;
			nextRotationAxis = null;
		}
		gl.glMultMatrixd(rotationMatrix, 0);
		gl.glGetDoublev(GL.GL_MODELVIEW, rotationMatrix, 0);
		// Move the objects according to the camera (instead of moving the camera)
		if (zoom != 0) {
			double[] oldMatrix = new double[16];
			gl.glGetDoublev(GL.GL_MODELVIEW, oldMatrix, 0);
			gl.glLoadIdentity();
			gl.glTranslated(0, 0, zoom);
			gl.glMultMatrixd(oldMatrix, 0);
		}
	}
	
	/**
	 * Draws the axes in the origin
	 * @param gl The GL instance to draw on
	 */
	private void renderAxes(GL gl) {
		gl.glLineWidth(2);
		boolean flag = gl.glIsEnabled(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(1, 0, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(100, 0, 0);
		
		gl.glColor3d(0, 1, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 100, 0);
		
		gl.glColor3d(0, 0, 1);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 0, 100);
		
		gl.glEnd();
		
		if (flag) {
			gl.glEnable(GL.GL_LIGHTING);
		}
	}

	public void setModel(IRenderable model) {
		this.model = model;
	}
	
	public void subdivide() {
		model.control(AbstractModel.SUBDIVIDE, null);
	}

	public void resetModel() {
		model.control(AbstractModel.RESET_MODEL, null);
	}
	
	public void resetView() {
		rotationMatrix = null;
		zoom = DEFAULT_ZOOM;
	}
}
