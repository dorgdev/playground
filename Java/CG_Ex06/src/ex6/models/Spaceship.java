/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.texture.Texture;

import ex6.Utils;
import ex6.Utils.TextureType;

/**
 * Represents a model of a spaceship.
 */
public class Spaceship extends AbstractModel implements Subdividable {

	/** The diffusion factors of the engine */
	private static float[] ENGINE_DIFFUSION = new float[] {0.7f, 1f, 0.7f, 1};
	/** The specularity factors of the engine */
	private static float[] ENGINE_SPECULARITY = new float[] {0.6f, 0.6f, 0.6f, 1};
	/** The default number of slices (when the model starts/resets) */
	private static int DEFAULT_NUM_SLICES = 4;
	/** The amount of slices to use for each GLU quadric shape */
	private int slices;
	/** The utility library instance for drawing spheres, disks, etc. */
	private GLU glu;
	/** The body of the spaceship. Starts as a pyramid and can be subdivided for smoothness */
	private Mesh body;
	/** The current CLU quadric to use */
  private GLUquadric quad;
  /** The boundaries defining the spaceship's collision parts */
  private ISphericalObstacle[] boundaries;
  /** Whether the spaceship is active (i.e., show fire, etc.) */
  private boolean active;
  /** Whether the spaceship's current direction should be displayed */
  private boolean showDirection;
  /** Whether the spaceship's body should be displayed */
  private boolean showBody;

  /**
	 * Creates a new Spaceship model
	 */
	public Spaceship() {
		super("Spaceship");
		glu = new GLU();
		createSpaceshipBody();
		slices = DEFAULT_NUM_SLICES;
		subdivide(4);
		active = true;
		showDirection = false;
		showBody = true;
		
		// Create the spaceship's boundaries
		boundaries = new AbstractSphericalObstacle[] {
				new AbstractSphericalObstacle(new Vec(0.4, 0.5, 0.4), 0.4),
				new AbstractSphericalObstacle(new Vec(-0.4, 0.5, 0.4), 0.4),
				new AbstractSphericalObstacle(new Vec(0, 0.1, -0.8), 0.3),
				new AbstractSphericalObstacle(new Vec(0, 0.3, -0.2), 0.6)
		};
	}
	
	@Override
	public void subdivide(int levels) {
		slices *= Math.pow(2, levels);
	  body.subdivide(levels);
	}
	
	@Override
	public void render(GL gl) {
		gl.glMatrixMode(GL.GL_MODELVIEW);
		glu = new GLU();
		quad = glu.gluNewQuadric();
		if (showBody) {
				drawSpaceship(gl);
		}
		if (showDirection) {
			drawDirection(gl);
		}
		glu.gluDeleteQuadric(quad);
	}
	
	/**
	 * @return The collision boundaries of the spaceship
	 */
	public ISphericalObstacle[] getSphericalBoundaries() {
		return boundaries;
	}

	/**
	 * Changes the show-body flag (whether to draw the spaceship)
	 * @param showDirection Whether to show the spaceship
	 */
	public void setShowBody(boolean showBody) {
		this.showBody = showBody;
	}

	/**
	 * Changes the show-direction flag (whether to show the spaceship's direction)
	 * @param showDirection Whether to show the direction
	 */
	public void setShowDirection(boolean showDirection) {
		this.showDirection = showDirection;
	}

	/**
	 * Starts the spaceship (activates it).
	 */
	public void start() {
		active = true;
	}
	
	/**
	 * Stops the spaceship (de-activates it).
	 */
	public void stop() {
		active = false;
	}
	
	/**
	 * Draws the current direction the spaceship is headed to
	 * @param gl The GL instance to use
	 */
	private void drawDirection(GL gl) {
		gl.glDisable(GL.GL_LIGHTING);

		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.8, 0, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glColor3d(0, 0, 0);
		gl.glVertex3d(0, 0, -100);
		gl.glEnd();
		
		gl.glEnable(GL.GL_LIGHTING);
	}
	
	/**
	 * Draws the spaceship of the model
	 * @param gl The GL instance to draw on
	 */
	private void drawSpaceship(GL gl) {
		// The body of the spaceship
		float[] bodyDiffusion = new float[] {0.7f, 0.7f, 1f, 1f};
		float[] bodySpecularity = new float[] {2f, 1.5f, 0.5f, 1f};
		setMaterialProperties(gl, bodyDiffusion, bodySpecularity, 100, null);
		body.draw(gl);
		// Cockpit
		gl.glPushMatrix();
		drawCockpit(gl);
		gl.glPopMatrix();
		// Canons
		gl.glPushMatrix();
		drawCanons(gl);
		gl.glPopMatrix();
		// Engines material
		gl.glColor3d(0.25, 0.25, 0.3);
		// Left engines cluster
		gl.glPushMatrix();
		gl.glTranslated(0.2, 0.75, 1);
		gl.glRotatef(50, 0, 0, 1);
		gl.glRotatef(90, 0, 1, 0);
		drawEngineCluster(gl);
		gl.glPopMatrix();
		// Right engines cluster
		gl.glPushMatrix();
		gl.glTranslated(-0.2, 0.75, 1);
		gl.glRotatef(50, 0, 0, -1);
		gl.glRotatef(90, 0, 1, 0);
		drawEngineCluster(gl);
		gl.glPopMatrix();
	}

	/**
	 * Draws the spaceship's cockpit
	 * @param gl The drawable GL instance
	 */
	private void drawCockpit(GL gl) {
		float[] cockpitDiffusion = new float[] {0.8f, 0.8f, 0.2f, 1};
		float[] cockpitSpecularity = new float[] {0.8f, 0.8f, 0.2f, 1};
		setMaterialProperties(gl, cockpitDiffusion, cockpitSpecularity, 10, null);
		gl.glTranslated(0, 0.7, -0.7);
		gl.glScaled(1, 1, 2);
		glu.gluSphere(quad, 0.55, slices, slices);
	}

	
	/**
	 * Draws one of the spaceship's engines cluster (contains several engines)
	 * @param gl The drawable GL instance
	 */
	private void drawEngineCluster(GL gl) {
		double poleRadius = 0.2;
		double poleHeight = 1.8;
		gl.glRotatef(-90, 1, 0, 0);
		setMaterialProperties(gl, ENGINE_DIFFUSION, ENGINE_SPECULARITY, 50, null);
		glu.gluCylinder(quad, poleRadius, poleRadius, poleHeight, slices, slices);
		gl.glTranslated(0, 0, poleHeight);
		// Engine 1
		gl.glPushMatrix();
  	gl.glRotated(90, -1, 0, 0);
		drawEngine(gl);
		gl.glPopMatrix();
		// Engine 2
		gl.glPushMatrix();
  	gl.glRotated(90, 1, 0, 0);
		drawEngine(gl);
		gl.glPopMatrix();
	}
	
	/**
	 * Draws one of the spaceship's engines
	 * @param gl The drawable GL instance
	 */
	private void drawEngine(GL gl) {
		double poleRadius = 0.2;
		double poleHeight = 0.7;
		double frontRadius = 0.3;
		double engineHeight = 1.5;
		double backRadius = 0.5;
		glu.gluCylinder(quad, poleRadius, poleRadius, poleHeight, slices, slices);
		gl.glTranslated(-engineHeight / 2, 0, poleHeight);
		gl.glRotated(90, 0, 1, 0);
		glu.gluCylinder(quad, backRadius, frontRadius, engineHeight, slices, slices);
		gl.glRotated(180, 1, 0, 0);
		gl.glPushMatrix();
		drawFireDisk(gl, backRadius, true);
		gl.glPopMatrix();
		gl.glRotated(-180, 1, 0, 0);
		gl.glTranslated(0, 0, engineHeight);
		gl.glPushMatrix();
		drawFireDisk(gl, frontRadius, false);
		gl.glPopMatrix();
	}
	
	/**
	 * Draws the back disk of the engine with the fire effect
	 * @param gl The GL instance to draw on
	 * @param radius The radius of the disk
	 * @param withFire Whether to draw the fire effect
	 */
	public void drawFireDisk(GL gl, double radius, boolean withFire) {
		Texture texture = null;
		if (withFire) {
			texture = Utils.getTexture(TextureType.Fire);
			glu.gluQuadricTexture(quad, true);
			texture.bind();
			texture.enable();
			double fireAngle = active ? Utils.randInt(360) : 0;
			gl.glRotated(fireAngle, 0, 0, 1);
		} else {
			setMaterialProperties(gl, ENGINE_DIFFUSION, ENGINE_SPECULARITY, 50, null);	
		}
		glu.gluDisk(quad, 0, radius, slices, 1);
		if (withFire) {
	    texture.disable();
		}
	}

	/**
	 * Draws the 2 front canons of the spaceship
	 * @param gl The drawable GL instance
	 */
	private void drawCanons(GL gl) {
		float[] canonsDiffusion = new float[] {1f, 0.3f, 0.2f, 1};
		float[] canonsSpecularity = new float[] {0.7f, 0.7f, 0.3f, 1};
		setMaterialProperties(gl, canonsDiffusion, canonsSpecularity, 20, null);
		gl.glTranslated(0.55, 0.2, -2.4);
		gl.glRotated(180, 0, 1, 0);
		gl.glPushMatrix();
		drawCanon(gl);
		gl.glPopMatrix();
		gl.glTranslated(1.1, 0, 0);
		drawCanon(gl);
	}

	/**
	 * Draws a single canon.
	 * @param gl The drawable GL instance
	 */
	private void drawCanon(GL gl) {
		double canonHeight = 1;
		double canonRadius = 0.1;
		glu.gluCylinder(quad, canonRadius, canonRadius, canonHeight, slices, slices);
		gl.glTranslated(0, 0, canonHeight);
		glu.gluDisk(quad, 0, canonRadius, slices, 1);
	}
	
	/**
	 * Creates the mesh representing the body of the spaceship
	 */
	private void createSpaceshipBody() {
		List<Vec> vertices = new ArrayList<Vec>(5);
		vertices.add(new Vec( 0,      -1,   -10));  // Vertex #0 - The apex
		vertices.add(new Vec( 0,      -1.5,   3));  // Vertex #1
		vertices.add(new Vec(-5.1650,  2.25,  3));  // Vertex #2
		vertices.add(new Vec( 5.1650,  2.25,  3));  // Vertex #3
		List<int[]> faces = new ArrayList<int[]>(5);
		faces.add(new int[] {0, 2, 1});
		faces.add(new int[] {0, 3, 2});
		faces.add(new int[] {0, 1, 3});
		faces.add(new int[] {1, 2, 3});
		body = new Mesh(faces, vertices);
	}
}
