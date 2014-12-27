/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;

import ex6.Utils.TextureType;
import ex6.models.Asteroid;
import ex6.models.Asteroid.AsteroidType;
import ex6.models.ISphericalObstacle;
import ex6.models.Spaceship;
import ex6.models.Vec;

/**
 * Renders the GL picture. The viewer in the MVC pattern.
 */
public class Viewer implements GLEventListener {

	/** The default size of the view */
	public static final double DEFAULT_VIEW_SIZE = 15;
	
	/** The game's model */
	private GameLogic game;
	/** The spaceship model to render */
	private Spaceship spaceship;
	/** The animator for the viewing */
	private FPSAnimator ani;
	/** Whether the spaceship should be shown */
	private boolean showSpaceship;
	/** Whether the spcaeship's boundaries should be shown */
	private boolean showSpaceshipBounds;
	/** Whether the spcaeship's direction should be shown */
	private boolean showDirection;
	/** Whether to show the additional information text */
	private boolean showTextInfo;
	/** Whether an orthographic projection should be use */
	private boolean useOrthographic;
	/** Whether an cockpit view be use */
	private boolean useCockpit;
	/** Whether the projection matrix should change (reshaped)*/
	private boolean reshape;
	/** Fake asteroids for the spaceship's collision boundaries */
	private List<Asteroid> spaceshipAsteroids;

	/**
	 * Create a new Viewer instance from the game's model and the spaceship.
	 * @param game The model of the MVC pattern
	 * @param spaceship The spaceship model to use
	 */
	public Viewer(GameLogic game, Spaceship spaceship) {	
		this.game = game;
		this.spaceship = spaceship;
		// Initialize default values
		showSpaceship = true;
		showSpaceshipBounds = false;
		showDirection = false;
		showTextInfo = true;
		useOrthographic = false;
		useCockpit = false;
		reshape = false;
		// Crate fake asteroids for the spaceship boundaries
		ISphericalObstacle[] spaceshipBoundaries = spaceship.getSphericalBoundaries();
		spaceshipAsteroids = new ArrayList<Asteroid>(spaceshipBoundaries.length);
		for (ISphericalObstacle boundary : spaceshipBoundaries) {
			Asteroid asteroid = new Asteroid(boundary.center(), boundary.radius(), AsteroidType.Bounds);
			spaceshipAsteroids.add(asteroid);
		}
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// First thing - update the game
		game.update();
		// If the view should change - change it
		if (reshape) {
			updateProjection(drawable, drawable.getWidth(), drawable.getHeight());
			reshape = false;
		}
		// Let the fun begin - start rendering stuff
		GL gl = drawable.getGL();
		GLU glu = new GLU();
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluQuadricTexture(quad, true);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		// Adjust the lights
		adjustLights(gl);
		
		// Render the stars
		renderFarAwayStars(gl, glu, quad);

		// Move the camera (origin) a bit forward for a better view
		setupCamera(gl, glu, quad);

		// Render the spaceship  (if necessary)
		renderSpaceship(gl);

		// Render the spaceship's boundaries  (if necessary)
		renderSpaceshipBoundaries(gl);

		// Render the asteroids
		renderAsteroids(gl);

		// Render the collision point (if necessary)
		renderCollisionPoint(gl);

		// Render text
		renderText(gl);
		
		// Delete allocated resource
		glu.gluDeleteQuadric(quad);
	}

	@Override
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Define some default behavior
		GL gl = drawable.getGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, 1);
		gl.glEnable(GL.GL_NORMALIZE);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_LIGHTING);
		// Make sure the textures are loaded
		Utils.loadTextures();
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST_MIPMAP_NEAREST);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		// Start animating the game
		ani = new FPSAnimator(30, true);
		ani.add(drawable);
		startAnimation();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		updateProjection(drawable, width, height);
	}

	/**
	 * Updates the projection matrix used for the game.
	 * @param drawable The GL drawable instance
	 * @param width The canvas' width
	 * @param height The canvas' height
	 */
	private void updateProjection(GLAutoDrawable drawable, int width, int height) {
		GL gl = drawable.getGL();
		// Calculate the new width and height (while preserving the ratio)
		double ratio = ((double)height) / width;
		double newWidth = DEFAULT_VIEW_SIZE;
		double newHeight = DEFAULT_VIEW_SIZE;
		if (width > height) {
			newHeight *= ratio;
		} else {
			newWidth /= ratio;
		}
		// Set the projection accordingly
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		if (useOrthographic) {
			gl.glOrtho(-newWidth, newWidth, -newHeight, newHeight, -500, 500);
			gl.glRotated(90, 1, 0, 0);
			gl.glTranslated(0, 10, Math.sqrt(ratio) * DEFAULT_VIEW_SIZE);
		} else {
			GLU glu = new GLU();
			glu.gluPerspective(60, newWidth / newHeight, 1, 500);
		}
	}

	public void startAnimation() {
		ani.start();
	}

	public void stopAnimation() {
		ani.stop();
	}

	public void toggleShip() {
		showSpaceship = ! showSpaceship;
		spaceship.setShowBody(showSpaceship);
	}

	public void toggleShipMark() {
		showSpaceshipBounds = ! showSpaceshipBounds;
	}

	public void toggleProjection() {
		if (!useOrthographic) {
			if (useCockpit) {
				useOrthographic = true;
				reshape = true;
			}
			useCockpit = !useCockpit;
		} else {
			useOrthographic = false;
			reshape = true;
		}
	}
	
	public void toggleShowDirection() {
		showDirection = !showDirection;
		spaceship.setShowDirection(showDirection);
	}

	public void toggleShowTextInfo() {
		showTextInfo = !showTextInfo;
	}
	
	/**
	 * Adjusts the lights in the scene
	 * @param gl The GL instance to use
	 */
	private void adjustLights(GL gl) {
		// First light - only diffuse (at the camera's position)
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[] {0, 0, 0, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, new float[] {0.4f, 0.4f, 0.4f, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, new float[] {0, 0, 0, 1}, 0);
		gl.glEnable(GL.GL_LIGHT0);

		// Second light - from above (for the orthographic display)
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, new float[] {0, 10, -10, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, new float[] {1, 1, 1, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, new float[] {0, 0, 0, 1}, 0);
		gl.glEnable(GL.GL_LIGHT1);
	}

	/**
	 * Moves the camera according to the projection in use.
	 * Also adds an effect of a cockpit when in the cockpit mode
	 * @param gl The GL instance to use
	 * @param glu A GLU utilities instance
	 * @param quad A GLU quadric helper instance
	 */
	private void setupCamera(GL gl, GLU glu, GLUquadric quad) {
		// Ortographic and non-cockpit view should move the spaceship a little
		if (useOrthographic || !useCockpit) {
			gl.glTranslated(0, -2, -7);
			return;
		}
		// Cockpit-mode should move the camera towards the spaceship's cockpit.
		// First, create a cockpit effect with a new semi-transparent sphere
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[] {1, 1, 0, 0.15f}, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] {1, 1, 0, 0.15f}, 0);
		glu.gluQuadricOrientation(quad, GLU.GLU_INSIDE);
		glu.gluSphere(quad, 20, 16, 16);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[] {0, 0, 0, 0}, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] {0, 0, 0, 0}, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		// And move the whole world accordingly
		gl.glTranslated(0, -0.7, -0.2);
	}
	
	/**
	 * Renders the stars far away
	 * @param gl The GL instance to use
	 * @param glu The GLU utilities instance
	 * @param quad The quad used for shapes
	 */
	private void renderFarAwayStars(GL gl, GLU glu, GLUquadric quad) {
		// Don't render the far away stars in orthographic projection
		if (useOrthographic) {
			return;
		}
		gl.glDepthMask(false);
		gl.glPushMatrix();

		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] {0, 0, 0, 1} ,0);
		Texture texture = Utils.getTexture(TextureType.Stars);
		texture.bind();
		texture.enable();
		gl.glRotated(2 * game.getAngle(), 0, 0, 1);
		gl.glRotated(game.getAngle(), 0, 1, 0);
		gl.glRotated(90, 1, 0, 0);
		glu.gluSphere(quad, 100D, 16, 16);
		texture.disable();

		gl.glPopMatrix();
		gl.glDepthMask(true);
	}
	
	/**
	 * Rotates the world according to the spaceship's angle
	 * @param gl The GL instance to use
	 */
	private void rotateSpaceshipAngle(GL gl) {
		// Don't rotate the ship when in cockpit mode
		if (useCockpit) {
			return;
		}
		gl.glRotated(-game.getAngle(), 0, 0, 1);
	}

	/**
	 * Rotates the world according to the astroids's angle
	 * @param gl The GL instance to use
	 */
	private void rotateAstroidsAngle(GL gl) {
		// The turning illusion
		gl.glRotated(0.4 * game.getAngle(), 0, 0, 1);
		// The actual angle
		gl.glRotated(game.getAngle(), 0, 1, 0);
	}

	/**
	 * Renders the spaceship
	 * @param gl The GL instance to use
	 */
	private void renderSpaceship(GL gl) {
		if (!(showSpaceship || showDirection)) {
			return;
		}
		gl.glPushMatrix();
		rotateSpaceshipAngle(gl);
		gl.glScaled(0.3, 0.3, 0.3);
		spaceship.render(gl);
		gl.glPopMatrix();
	}
	
	/**
	 * Renders the spaceship's boundaries (fake asteroids)
	 * @param gl The GL instance to use
	 */
	private void renderSpaceshipBoundaries(GL gl) {
		if (!showSpaceshipBounds) {
			return;
		}
		gl.glPushMatrix();
		rotateSpaceshipAngle(gl);
		for (Asteroid spaceshipAsteroid : spaceshipAsteroids) {
			spaceshipAsteroid.render(gl);
		}
		gl.glPopMatrix();
	}

	/**
	 * Renders the asteroids in the game.
	 * @param gl The GL instance to use
	 */
	public void renderAsteroids(GL gl) {
		gl.glDisable(GL.GL_LIGHT0);
		
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glPushMatrix();
		rotateAstroidsAngle(gl);
		// Draw the asteroid (from last to first for the transparency effect)
		for (int i = game.getAsteroids().size() - 1; i >= 0; --i) {
			game.getAsteroids().get(i).render(gl);
		}
		gl.glDisable(GL.GL_CULL_FACE);
		// Init the ambient material (asteroids tend to change it)
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] {0, 0, 0, 0}, 0);
		// Restore the matrix
		gl.glPopMatrix();
		
		gl.glEnable(GL.GL_LIGHT0);
	}
	
	/**
	 * Renders the collision point (if applicable)
	 * @param gl The GL instance to use
	 */
	private void renderCollisionPoint(GL gl) {
		Vec[] collision = game.getCollision();
		// Check if there was a collision
		if (collision == null) {
			return;
		}
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glPushMatrix();
		gl.glPointSize(3);
		// Draw the yellow line (collision path)
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(1, 1, 0);
		gl.glVertex3d(collision[0].x, collision[0].y, collision[0].z);
		gl.glVertex3d(collision[2].x, collision[2].y, collision[2].z);
		gl.glEnd();
		// Draw the red point (collision point)
		gl.glBegin(GL.GL_POINTS);
	  gl.glColor3d(1, 0, 0);
		gl.glVertex3d(collision[1].x, collision[1].y, collision[1].z);
		gl.glEnd();
		gl.glPopMatrix();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_LIGHTING);
	}

	/**
	 * Renders the text displayed for the user (score, asteroids count, etc.).
	 * @param gl The GL instance to use
	 */
	private void renderText(GL gl) {
		// Text time. Display all the necessary information
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
		GLUT glut = new GLUT();
		
		int letterHeight = glut.glutBitmapWidth(7, 'M') + 6;
		int letterMult = 0;
		int textDist = 5;
		// Controls
		gl.glColor3d(0.6, 0.6, 0.6);
		gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
		glut.glutBitmapString(7, Utils.CONTROL_KEYS);
		// The following statistical text parts could be toggles
		if (showTextInfo) {
			// Distance covered
			gl.glColor3d(0.7, 0, 0.6);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Travel Distance: " + (int)(game.getTravelDistance()) + " K-Miles");
			// Average speed
			int averageSpeed = (int)(1000 * game.getAverageSpeed());
			gl.glColor3d(0.7, 0, 0.6);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Avg Speed: " + averageSpeed + " Mps");
			// Current speed
			int speed = (int)(1000 * game.getSpeed());
			gl.glColor3d(0.7, 0, 0.6);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Speed: " + speed + " Mps");
			// Current angle
			gl.glColor3d(0.7, 0, 0.6);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Angle: " + (int)(game.getAngle()) + " degrees");
			// Asteroids count
			gl.glColor3d(0.6, 0.6, 0);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Asteroids: " + game.getAsteroids().size());
			// Score
			gl.glColor3d(0.6, 0.6, 0);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(7, "Score: " + game.getScore());
		}
		// End of optional text.  States are mandatory.
		// Optional strings (e.g., pause, ghost mode, etc..)
		if (game.isGameOver()) {
			gl.glColor3d(1, 0, 0);
			gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
			glut.glutBitmapString(8, "GAME OVER");
		} else {
			if (game.isGhostMode()) {
				gl.glColor3d(0, 1, 0);
				gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
				glut.glutBitmapString(8, "GHOST MODE");
			}
			if (game.isCruiseControl()) {
				gl.glColor3d(0.2, 0.8, 1);
				gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
				glut.glutBitmapString(8, "CRUISE-CONTROL");
			}
			if (game.isMuted()) {
				gl.glColor3d(0.5, 0.5, 0.5);
				gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
				glut.glutBitmapString(8, "NO-MUSIC");
			}
			if (game.isPaused()) {
				gl.glColor3d(1, 1, 0);
				gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
				glut.glutBitmapString(8, "PAUSED");
			} else {
				if (game.isBonus()) {
					gl.glColor3d(1, 1, 0);
					gl.glWindowPos2d(textDist, textDist + letterHeight * (letterMult++));
					glut.glutBitmapString(8, "Bonus!");
				}
			}
		}
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_LIGHTING);
	}
}
