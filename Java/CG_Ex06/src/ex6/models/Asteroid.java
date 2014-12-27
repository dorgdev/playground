/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.texture.Texture;

import ex6.Utils;
import ex6.Utils.TextureType;

/**
 * The Asteroid symbolizes an asteroid in the game. It knows how to render
 * itself and provide model information for the asteroid object.
 */
public class Asteroid extends AbstractSphericalObstacle implements IRenderable {

	/**
	 * The type of asteroid to use (and set the materials accordingly)
	 */
	public static enum AsteroidType {
		RealAsteroid,
		Bounds,
		BonusAsteroid
	}
	
	/** The base scale factor of the asteroid speed */
	public static final double ASTEROID_SPEED_BASE_FACTOR = 3;
	/** The division factor of the asteroid speed */
	public static final double ASTEROID_SPEED_DIV_FACTOR = 80;

	/** Whether the display lists created already */
  private static boolean isDisplayListsGenerated = false;
  /** The ID of the Asteroid display list (populated after creation) */
  private static int asteroidListID;
  /** The ID of the Bounds display list (populated after creation) */
  private static int boundsListID;
  /** The ID of the Bonus display list (populated after creation) */
	private static int bonusListID;
	/** The ambient color factors of the asteroid */
	private float[] ambientColor;
	/** The diffuse color factors of the asteroid */
	private float[] diffuseColor;
	/** The type of asteroid this instance represents */
	private AsteroidType type;
	
	/**
	 * Creates a new Asteroid with a regular texture (a RealAsteroid type).
	 * @see Asteroid#Asteroid(Vec, double, boolean)
	 */
  public Asteroid(Vec center, double radius) {
		this(center, radius, AsteroidType.RealAsteroid);
	}

  /**
   * Creates a new Asteroid from the given initial center and radius
   * @param center The asteroid's initial center
   * @param radius The asteroid's initial radius
   * @param isChecker Whether to use the checker texture for this asteroid
   */
  public Asteroid(Vec center, double radius, AsteroidType type) {
		super(center, radius);
		this.type = type;
		switch (type) {
    case RealAsteroid:
    	Color color = new Color(Color.HSBtoRGB(Utils.randFloat(), 0.6f, 1F));
    	float r = color.getRed() / 256f;
    	float g = color.getGreen() / 256f;
    	float b = color.getBlue() / 256f;
    	diffuseColor = new float[] {r, g, b, 0.5f};
    	ambientColor = new float[] {0.4f * r, 0.3f * g, 0.3f * b, 0.9f};
	    break;
    case Bounds:
    	diffuseColor = new float[] {0.7f, 0.7f, 0.7f, 0.2f};
    	ambientColor = new float[] {0.7f, 0.7f, 0.7f, 0.2f};
    	break;
    case BonusAsteroid:
    	diffuseColor = new float[] {0.6f, 0.6f, 0.3f, 1f};
    	ambientColor = new float[] {0.6f, 0.6f, 0.3f, 1f};
    	radius = 1;
    	break;
    default:
	    break;
    }
	}
	
	@Override
	public void render(GL gl) {
		gl.glPushMatrix();
		// Make sure we generated the display list already
		genDisplayLists(gl);
		// Set the underlying material (blended with the texture)
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuseColor, 0);
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColor, 0);
		// Change the asteroids location and radius (as the display list can't
		// handle different values for all the asteroids)
    gl.glTranslated(center.x, center.y, center.z);
    gl.glScaled(radius, radius, radius);
    // Render the asteroid
    switch (type) {
    case RealAsteroid:
    	gl.glCallList(asteroidListID);
    	break;
    case Bounds:
	    gl.glCallList(boundsListID);
	    break;
    case BonusAsteroid:
    	gl.glCallList(bonusListID);
    	break;
    default:
	    break;
    }
		gl.glPopMatrix();
	}
	
  /**
   * Updates the asteroid location according to the given direction.
   * The asteroid movement formula: A+D^2/B, where A and B are constant values
   * and D is the distance of the asteroid from the spaceship (origin).
   * @param direction The direction towards the asteroid moves
   * @see Asteroid#ASTEROID_SPEED_BASE_FACTOR
   * @see Asteroid#ASTEROID_SPEED_DIV_FACTOR
   */
	public void updateLocation(Vec direction) {
		double directionRatio = ASTEROID_SPEED_BASE_FACTOR;
		directionRatio += center.lengthSquared() / ASTEROID_SPEED_DIV_FACTOR;
		center.add(Vec.scale(directionRatio, direction));
	}

	/**
	 * @return The type of the asteroid
	 */
	public AsteroidType getType() {
		return type;
	}
	
	/**
	 * Generate the asteroid from the 
	 * @param gl The GK instance to use
	 */
	private void genDisplayLists(GL gl) {
		// Make sure we don't generate the lists more than once
		if (isDisplayListsGenerated) {
			return;
		}
		isDisplayListsGenerated = true;
		// Create the lists for the regular asteroid and for the checker one
		Texture astroidTexture = Utils.getTexture(TextureType.Asteroid);
		asteroidListID = createSphereListFromTexture(gl, astroidTexture);
		Texture checkerTexture = Utils.getTexture(TextureType.Checker);
		boundsListID = createSphereListFromTexture(gl, checkerTexture);
		Texture bonusTexture = Utils.getTexture(TextureType.Bonus);
		bonusListID = createSphereListFromTexture(gl, bonusTexture);
	}
	
	/**
	 * Generates a display list from the given texture on the GL instance.
	 * Note: Set the sphere radius to 1, scale later for actual size.
	 * @param gl The GL instance to use
	 * @param texture The texture to use for the display list
	 * @return The list ID (for the 'glCallList' method)
	 */
	public int createSphereListFromTexture(GL gl, Texture texture) {
		// Allocate some resource
		GLU glu = new GLU();
    GLUquadric quad = glu.gluNewQuadric();
		// Create a new list, and store it ID (for return value)
		int listID = gl.glGenLists(1);
		// Start the list, compile only (execute will be done elsewhere)
		gl.glNewList(listID, GL.GL_COMPILE);
		// Pass the command creating the textured sphere
    glu.gluQuadricTexture(quad, true);
    texture.bind();
    texture.enable();
    glu.gluQuadricTexture(quad, true);
    gl.glRotated(90, 0, 0, 1);
    gl.glRotated(-90, 0, 1, 0);
    gl.glRotated(90, 0, 0, 1);
    glu.gluSphere(quad, 1, 16, 16);
    texture.disable();
    // Done creating the list
    gl.glEndList();
    // Discard allocated resource and return the list's ID
    glu.gluDeleteQuadric(quad);
    return listID;
	}
}
