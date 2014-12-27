/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5.models;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import ex5.math.Vec;

/**
 * Represents a model of a spaceship.
 * 
 * @author dor
 */
public class Spaceship extends AbstractModel implements Subdividable, Resetable {

	/** The diffusion factors of the engine */
	private static float[] ENGINE_DIFFUSION = new float[] {0.7f, 1f, 0.7f, 1};
	/** The specularity factors of the engine */
	private static float[] ENGINE_SPECULARITY = new float[] {1, 1, 1, 1};
	/** The default number of slices (when the model starts/resets) */
	private static int DEFAULT_NUM_SLICES = 4;
	/** The amount of slices to use for each GLU quadric shape */
	private int slices;
	/** The utility library instance for drawing spheres, disks, etc. */
	private GLU glu;
	/** The body of the spaceship. Starts as a pyramid and can be subdivided for smoothness */
	private Mesh body;
	
	/**
	 * Creates a new Spaceship model
	 */
	public Spaceship() {
		super("Spaceship");
		glu = new GLU();
		createSpaceshipBody();
		slices = DEFAULT_NUM_SLICES;
	}
	
	@Override
	public void subdivide(int levels) {
		slices *= Math.pow(2, levels);
	  body.subdivide(levels);
	}
	
	@Override
  public void reset() {
		slices = DEFAULT_NUM_SLICES;
		createSpaceshipBody();
  }

	@Override
	public void render(GL gl) {
		GLUquadric gluQuadric = glu.gluNewQuadric();
    glu.gluQuadricOrientation(gluQuadric, GLU.GLU_OUTSIDE);

		// Handle Lights
		addLights(gl, gluQuadric);
		
		drawSpaceship(gl, gluQuadric);
		
		// Remove the Lights
		removeLights(gl);

		glu.gluDeleteQuadric(gluQuadric);
	}
	
	/**
	 * Adds the scene's lights.
	 * @param gl The GL instance to use
	 * @param gluQuadric A GLUQuadric isntance for the light spheres
	 */
	private void addLights(GL gl, GLUquadric gluQuadric) {
		double lightSphereRadius = 0.05;
		float[] lightSphereColor = new float[] {0.7f, 0.7f, 0.1f, 1};
		// Save current matrix
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		// Add the first light
		gl.glLoadIdentity();
		float[] lightPos1 = new float[] {2.3f, 1, -1.2f, 1};
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPos1, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[] {25f, 25f, 25f, 1f}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, new float[] {1f, 1f, 1f, 1f}, 0);
		gl.glLightf(GL.GL_LIGHT1, GL.GL_QUADRATIC_ATTENUATION, 3);
		gl.glEnable(GL.GL_LIGHT1);
		if (showLightSpheres) {
			gl.glTranslatef(lightPos1[0], lightPos1[1], lightPos1[2]);
			setMaterialProperties(gl, null, null, 0, lightSphereColor);
			glu.gluSphere(gluQuadric, lightSphereRadius, slices, slices);
		}
		// Add the second light
		gl.glLoadIdentity();
		float[] lightPos2 = new float[] {-2.3f, -1, -1.2f, 1};
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, lightPos2, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, new float[] {1f, 1f, 1f, 1f}, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, new float[] {2f, 2f, 2f, 1f}, 0);
		gl.glLightf(GL.GL_LIGHT2, GL.GL_QUADRATIC_ATTENUATION, 1);
		gl.glEnable(GL.GL_LIGHT2);
		if (showLightSpheres) {
			gl.glTranslatef(lightPos2[0], lightPos2[1], lightPos2[2]);
			setMaterialProperties(gl, null, null, 0, lightSphereColor);
			glu.gluSphere(gluQuadric, lightSphereRadius, slices, slices);
		}
		// Pop the current matrix
		gl.glPopMatrix();
	}
	
	/**
	 * Remove the added lights
	 * @param gl The GL instance of the scene
	 */
	private void removeLights(GL gl) {
		gl.glDisable(GL.GL_LIGHT1);
		gl.glDisable(GL.GL_LIGHT2);
	}

	private void drawSpaceship(GL gl, GLUquadric gluQuadric) {
		// The body of the spaceship
		float[] bodyDiffusion = new float[] {0.7f, 0.7f, 1f, 1f};
		float[] bodySpecularity = new float[] {2f, 1.5f, 0.5f, 1f};
		setMaterialProperties(gl, bodyDiffusion, bodySpecularity, 100, null);
		body.draw(gl);
		// Cockpit
		gl.glPushMatrix();
		drawCockpit(gl, gluQuadric);
		gl.glPopMatrix();
		// Canons
		gl.glPushMatrix();
		drawCanons(gl, gluQuadric);
		gl.glPopMatrix();
		// Engines material
		gl.glColor3d(0.25, 0.25, 0.3);
		// Left engines cluster
		gl.glPushMatrix();
		gl.glTranslated(-1, 0.3, 0.2);
		gl.glRotatef(60, -1, 0, 0);
		drawEngineCluster(gl, gluQuadric);
		gl.glPopMatrix();
		// Right engines cluster
		gl.glPushMatrix();
		gl.glTranslated(-1, 0.3, -0.2);
		gl.glRotatef(120, -1, 0, 0);
		drawEngineCluster(gl, gluQuadric);
		gl.glPopMatrix();
	}

	/**
	 * Draws the spaceship's cockpit
	 * @param gl The drawable GL instance
	 * @param gluQuadric The GLU utilities instance
	 */
	private void drawCockpit(GL gl, GLUquadric gluQuadric) {
		float[] cockpitDiffusion = new float[] {0.8f, 0.8f, 0.2f, 1};
		float[] cockpitSpecularity = new float[] {0.8f, 0.8f, 0.2f, 1};
		setMaterialProperties(gl, cockpitDiffusion, cockpitSpecularity, 10, null);
		gl.glTranslated(0.7, 0.7, 0);
		gl.glScaled(2, 1, 1);
		glu.gluSphere(gluQuadric, 0.55, slices, slices);
	}

	
	/**
	 * Draws one of the spaceship's engines cluster (contains several engines)
	 * @param gl The drawable GL instance
	 * @param gluQuadric The GLU utilities instance
	 */
	private void drawEngineCluster(GL gl, GLUquadric gluQuadric) {
		double poleRadius = 0.2;
		double poleHeight = 2.2;
		setMaterialProperties(gl, ENGINE_DIFFUSION, ENGINE_SPECULARITY, 50, null);
		glu.gluCylinder(gluQuadric, poleRadius, poleRadius, poleHeight, slices, slices);
		gl.glTranslated(0, 0, poleHeight);
		// Engine 1
		gl.glPushMatrix();
		gl.glRotated(90, -1, 0, 0);
		drawEngine(gl, gluQuadric);
		gl.glPopMatrix();
		// Engine 2
		gl.glPushMatrix();
		gl.glRotated(-90, -1, 0, 0);
		drawEngine(gl, gluQuadric);
		gl.glPopMatrix();
	}
	
	/**
	 * Draws one of the spaceship's engines
	 * @param gl The drawable GL instance
	 * @param gluQuadric The GLU utilities instance
	 */
	private void drawEngine(GL gl, GLUquadric gluQuadric) {
		double poleRadius = 0.2;
		double poleHeight = 0.7;
		double backRadius = 0.5;
		double frontRadius = 0.3;
		double engineHeight = 1.5;
		float[] fireDiffusion = new float[] {0.4f, 0, 0, 1};
		float[] fireSpecularity = new float[] {0.5f, 0.1f, 0.1f, 1};
		glu.gluCylinder(gluQuadric, poleRadius, poleRadius, poleHeight, slices, slices);
		gl.glTranslated(-engineHeight / 2, 0, poleHeight);
		gl.glRotated(90, 0, 1, 0);
		glu.gluCylinder(gluQuadric, backRadius, frontRadius, engineHeight, slices, slices);
		setMaterialProperties(gl, fireDiffusion, fireSpecularity, 50, null);
		gl.glRotated(180, 1, 0, 0);
		glu.gluDisk(gluQuadric, 0, backRadius, slices, 1);
		gl.glRotated(-180, 1, 0, 0);
		setMaterialProperties(gl, ENGINE_DIFFUSION, ENGINE_SPECULARITY, 50, null);
		gl.glTranslated(0, 0, engineHeight);
		glu.gluDisk(gluQuadric, 0, frontRadius, slices, 1);
	}
	
	/**
	 * Draws the 2 front canons of the spaceship
	 * @param gl The drawable GL instance
	 * @param gluQuadric The GLU utilities instance
	 */
	private void drawCanons(GL gl, GLUquadric gluQuadric) {
		float[] canonsDiffusion = new float[] {1f, 0.3f, 0.2f, 1};
		float[] canonsSpecularity = new float[] {0.7f, 0.7f, 0.3f, 1};
		setMaterialProperties(gl, canonsDiffusion, canonsSpecularity, 20, null);
		gl.glTranslated(2.4, 0.2, 0.55);
		gl.glRotated(90, 0, 1, 0);
		gl.glPushMatrix();
		drawCanon(gl, gluQuadric);
		gl.glPopMatrix();
		gl.glTranslated(1.1, 0, 0);
		drawCanon(gl, gluQuadric);
	}

	/**
	 * Draws a single canon.
	 * @param gl The drawable GL instance
	 * @param gluQuadric The GLU utilities instance
	 */
	private void drawCanon(GL gl, GLUquadric gluQuadric) {
		double canonHeight = 1;
		double canonRadius = 0.1;
		glu.gluCylinder(gluQuadric, canonRadius, canonRadius, canonHeight, slices, slices);
		gl.glTranslated(0, 0, canonHeight);
		glu.gluDisk(gluQuadric, 0, canonRadius, slices, 1);
	}
	
	/**
	 * Creates the mesh representing the body of the spaceship
	 */
	private void createSpaceshipBody() {
		List<Vec> vertices = new ArrayList<Vec>(5);
		vertices.add(new Vec(12, -1,     0));          // Vertex #0 - The apex
		vertices.add(new Vec(-4, -3.5,   0));          // Vertex #1
		vertices.add(new Vec(-4,  2.25,  5.1650635));  // Vertex #2
		vertices.add(new Vec(-4,  2.25, -5.1650635));  // Vertex #3
		List<int[]> faces = new ArrayList<int[]>(5);
		faces.add(new int[] {0, 2, 1});
		faces.add(new int[] {0, 3, 2});
		faces.add(new int[] {0, 1, 3});
		faces.add(new int[] {1, 2, 3});
		body = new Mesh(faces, vertices);
	}
}
