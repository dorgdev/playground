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
 * This class represents a model of a cube in the GL space.
 * 
 * @author dor
 */
public class CubeModel extends AbstractModel implements Subdividable, Resetable {

	/** The mesh representing the cube's surface */
	private Mesh cubeMesh;
	/** The utility library instance for drawing spheres, disks, etc. */
	private GLU glu;
	
	/**
	 * Creates a new CubeModel instance
	 */
	public CubeModel() {
		super("Cube Model");
		glu = new GLU();
		createCube();
	}
	
	@Override
	public void render(GL gl) {
		GLUquadric gluQuad = glu.gluNewQuadric();
		glu.gluQuadricOrientation(gluQuad, GLU.GLU_OUTSIDE);

		gl.glMatrixMode(GL.GL_MODELVIEW);

		// Add the Lights
		addLights(gl, gluQuad);

		// Draw the cube
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[] {1f, 1f, 1f, 1}, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, new float[] {0f, 0f, 0f, 1}, 0);
		cubeMesh.draw(gl);
		
		// Remove the Lights
		removeLights(gl);
		
		glu.gluDeleteQuadric(gluQuad);
	}

	@Override
	public void subdivide(int levels) {
		cubeMesh.subdivide(levels);
	}
	
	@Override
	public void reset() {
		createCube();
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
		float[] lightPos1 = new float[] {-0.866f, -0.5f, -1.2f, 1};
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPos1, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[] {0f, 0f, 1f, 1f}, 0);
		gl.glLightf(GL.GL_LIGHT1, GL.GL_QUADRATIC_ATTENUATION, 0);
		gl.glEnable(GL.GL_LIGHT1);
		if (showLightSpheres) {
			gl.glTranslatef(lightPos1[0], lightPos1[1], lightPos1[2]);
			setMaterialProperties(gl, null, null, 0, lightSphereColor);
			glu.gluSphere(gluQuadric, lightSphereRadius, 32, 32);
		}
		// Add the second light
		gl.glLoadIdentity();
		float[] lightPos2 = new float[] {0.866f, -0.5f, -1.2f, 1};
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, lightPos2, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, new float[] {0f, 1f, 0f, 1f}, 0);
		gl.glLightf(GL.GL_LIGHT2, GL.GL_QUADRATIC_ATTENUATION, 0);
		gl.glEnable(GL.GL_LIGHT2);
		if (showLightSpheres) {
			gl.glTranslatef(lightPos2[0], lightPos2[1], lightPos2[2]);
			setMaterialProperties(gl, null, null, 0, lightSphereColor);
			glu.gluSphere(gluQuadric, lightSphereRadius, 32, 32);
		}
		// Add the third light
		gl.glLoadIdentity();
		float[] lightPos3 = new float[] {0f, 1f, -1.2f, 1};
		gl.glLightfv(GL.GL_LIGHT3, GL.GL_POSITION, lightPos3, 0);
		gl.glLightfv(GL.GL_LIGHT3, GL.GL_DIFFUSE, new float[] {1f, 0f, 0f, 1f}, 0);
		gl.glLightf(GL.GL_LIGHT3, GL.GL_QUADRATIC_ATTENUATION, 0);
		gl.glEnable(GL.GL_LIGHT3);
		if (showLightSpheres) {
			gl.glTranslatef(lightPos3[0], lightPos3[1], lightPos3[2]);
			setMaterialProperties(gl, null, null, 0, lightSphereColor);
			glu.gluSphere(gluQuadric, lightSphereRadius, 32, 32);
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
		gl.glDisable(GL.GL_LIGHT3);
	}
	
	/**
	 * Creates the cube from scratch.
	 */
	private void createCube() {
		// Create the cube's vertices
		List<Vec> vertices = new ArrayList<Vec>(8);
		vertices.add(new Vec(-1, +1, -1));  // Vertex #0
		vertices.add(new Vec(+1, +1, -1));  // Vertex #1
		vertices.add(new Vec(+1, -1, -1));  // Vertex #2
		vertices.add(new Vec(-1, -1, -1));  // Vertex #3
		vertices.add(new Vec(-1, +1, +1));  // Vertex #4
		vertices.add(new Vec(+1, +1, +1));  // Vertex #5
		vertices.add(new Vec(+1, -1, +1));  // Vertex #6
		vertices.add(new Vec(-1, -1, +1));  // Vertex #7
		// Create the cube's faces
		List<int[]> faces = new ArrayList<int[]>(6);
		faces.add(new int[] {0, 1, 2, 3});
		faces.add(new int[] {6, 2 ,1, 5});
		faces.add(new int[] {7, 6, 5, 4});
		faces.add(new int[] {3, 7, 4, 0});
		faces.add(new int[] {5, 1, 0, 4});
		faces.add(new int[] {3, 2, 6, 7});
		// Create the cube mesh itself
		cubeMesh = new Mesh(faces, vertices);
	}
}
