/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ex6.models.AbstractSphericalObstacle;
import ex6.models.Asteroid;
import ex6.models.Asteroid.AsteroidType;
import ex6.models.ISphericalObstacle;
import ex6.models.Spaceship;
import ex6.models.Vec;

/**
 * Simple game where spaceship can be turned right or left (Model in the MVC
 * paradigm)
 */
public class GameLogic {

	/** The game's angle change delta in each cycle (in degrees) */
	public static final double ANGLE_CHANGE_DEGREES = 3;
	/** The maximal angle the ship can turn (in degrees) */
	public static final double MAX_ANGLE_DEGREES = 60;
	/** The position where asteroid are removed from the game */
	public static final double ASTEROIDS_FADE_INDEX = 30;
	/** The default asteroids' speed (cycle's advance step) */
	public static final double DEFAULT_ASTEROIDS_SPEED = 0.1;
	/** The asteroids' maximal speed */
	public static final double MAX_ASTEROID_SPEED = 0.2;
	/** The asteroids' minimal speed */
	public static final double MIN_ASTEROID_SPEED = 0.06;
	/** The change in the asteroids' speed (for increasing and decreasing it) */
	public static final double ASTEROID_SPEED_CHANGE = 0.01;
	/** Once in how many cycles should asteroid be created */
	public static final int ASTEROIDS_CREATION_CYCLES = 1;
	/** The number of asteroid to create (when needed) */
	public static final int ASTEROIDS_CREATION_COUNT = 4;
	/** The maximal amount of asteroids in the game */
	public static final int ASTEROIDS_MAX_COUNT = 150;
	/** The minimal amount of asteroids in the game */
	public static final int ASTEROIDS_MIN_COUNT = 50;
	/** The distance of the farthest asteroid to display */
	public static final double FARTHEST_ASTEROID_DISTANCE = 300;
	
	/** The angle of the spaceship */
	private double angle;
	/** The current game's score */
	private long score;
	/** Whether the game was over */
	private boolean isGameOver;
	/** Whether the game was paused */
	private boolean isPaused;
	/** Whether the game is in ghost mode (i.e., can't lose) */
	private boolean isGhostMode;
	/** Whether the spaceship is on cruise control (keep the same direction) */
	private boolean isCruiseControl;
	/** Should the spaceship turn left in the next game cycle */
	private boolean isTurnLeft;
	/** Should the spaceship turn right in the next game cycle */
	private boolean isTurnRight;
	/** Is the spaceship currently collisioning with a Bonus asteroid*/
	private boolean isBonus;
	/** Should the music be muted */
	private boolean isMuted;
	/** The list of Asteroid currently in the game */
	private List<Asteroid> asteroids; 
	/** In how many cycles ahead should asteroids be created */
	private int createAsteroidsCyclesCounter;
  /** The spaceship used in the game */ 
	private Spaceship spaceship;
	/** When not null, indicates the collision line (center, collision, center) */
	private Vec[] collision;
	/** The current asteroid speed (may change during runtime) */
	private double asteroidSpeed;
	/** The total distance traveled by the spaceship */
	private double travelDistance;
	/** The number of cycles in the current game */
	private int travelTime;

	/**
	 * Creates a new GameLogic instance, from the spaceship model to use 
	 * @param spaceship The spaceship to use
	 */
	public GameLogic(Spaceship spaceship) {
		this.spaceship = spaceship;
		isMuted = false;
		init();
	}

	/**
	 * Update game model for one cycle.
	 * Moves the asteroids (as if the spaceship has moved).
	 */
	public synchronized void update() {
		// Only update game if it's not paused or over
		if (!(isGameOver || isPaused)) {
			// Make sure we update according to the spaceship's angle
			updateAngle();
			// Update the location of the asteroids (instead of the spaceship's)
			updateAsteroids();
			// If it's not ghost mode, check for collision
			if (!isGhostMode) {
				checkCollision();
				if (!isGameOver) {
					// Get points with respect to the number of asteroids and their speed
					score += asteroids.size() * asteroidSpeed;
				}
			}
			// Update distance and time of current played game
			travelDistance += asteroidSpeed;
			travelTime++;
		}
	}

	/**
	 * Restarting the game (initializes it from the beginning).
	 */
	public void restart() {
		init();
		if (!Utils.isMusicPlayed() && !isMuted) {
			Utils.startMusic();
		}
	}

	/**
	 * @param isTurnLeft Whether the spaceship is turning left
	 */
	public void setTurnLeft(boolean isTurnLeft) {
		this.isTurnLeft = isTurnLeft;
	}

	/**
	 * @param isTurnLeft Whether the spaceship is turning right
	 */
	public void setTurnRight(boolean isTurnRight) {
		this.isTurnRight = isTurnRight;
	}

	/**
	 * @return The current spaceship angle
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * @return The current list of asteroids in the game
	 */
	public List<Asteroid> getAsteroids() {
		return asteroids;
	}
	
	/**
	 * Stop/resume the game (toggles paused state)
	 */
	public void togglePause() {
		isPaused = !isPaused;
		if (!isMuted) {
			if (isPaused) {
				Utils.stopMusic();
			} else {
				Utils.startMusic();
			}
		}
	}
	
	/**
	 * @return Whether the game is paused
	 */
	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * @return The current game's score
	 */
	public long getScore() {
		return score;
	}

	/**
	 * @return Whether the game is over
	 */
	public boolean isGameOver() {
		return isGameOver;
	}

	/**
	 * Changes ghost mode (when the game is on)
	 */
	public void toggleGhostMode() {
		isGhostMode = !isGhostMode;
	}

	/**
	 * @return Whether the game is in ghost mode (invulnerable)
	 */
	public boolean isGhostMode() {
		return isGhostMode;
	}

	/**
	 * @return Whether the spaceship currently collisions with a bonus asteroid
	 */
	public boolean isBonus() {
		return isBonus;
	}
	/**
	 * Toggles the spaceship auto pilot which preserves the spaceship direction
	 */
	public void toggleCruiseControl() {
		isCruiseControl = !isCruiseControl;
	}

	/**
	 * Whether we're in cruise-control mode
	 */
	public boolean isCruiseControl() {
		return isCruiseControl;
	}

	/**
	 * Toggles the music in the game
	 */
	public void toggleMusic() {
		isMuted = !isMuted;
		if (isPaused || isGameOver) {
			return;
		}
		if (Utils.isMusicPlayed()) {
			Utils.stopMusic();
		} else if (!isMuted) {
			Utils.startMusic();
		}
	}
	
	/**
	 * @return Whether the music in the game is currently muted
	 */
	public boolean isMuted() {
		return isMuted;
	}
	
	/**
	 * Increases the asteroids speed (only if the current speed is under the max).
	 */
	public void increaseSpeed() {
		if (asteroidSpeed < MAX_ASTEROID_SPEED) {
			asteroidSpeed += ASTEROID_SPEED_CHANGE;
		}
	}

	/**
	 * Decreases the asteroids speed (only if the current speed is over the min).
	 */
	public void decreaseSpeed() {
		if (asteroidSpeed > MIN_ASTEROID_SPEED) {
			asteroidSpeed -= ASTEROID_SPEED_CHANGE;
		}
	}

	/**
	 * @return The current asteroids' speed
	 */
	public double getSpeed() {
		return asteroidSpeed;
	}

	/**
	 * @return The average speed
	 */
	public double getAverageSpeed() {
		return travelDistance / travelTime;
	}
	
	/**
	 * @return The distance traveled so far by the spaceship
	 */
	public double getTravelDistance() {
		return travelDistance;
	}
	
	/**
	 * The collision point, defined by the 3 points of collision:
	 * 0 - The collision asteroid center
	 * 1 - The collision point between the bodies
	 * 2 - The collision spaceship's boundary center
	 * @return The collision as described above
	 */
	public Vec[] getCollision() {
		return collision;
	}
	
	/**
	 * Initializes all the default values of the game.
	 */
	private void init() {
    asteroids = new ArrayList<Asteroid>(ASTEROIDS_MAX_COUNT);
    angle = 0;
    score = 0;        
    isPaused = false;
    isGameOver = false;
    isCruiseControl = false;
    isBonus = false;
    collision = null;
    createAsteroidsCyclesCounter = 1;
    asteroidSpeed = DEFAULT_ASTEROIDS_SPEED;
    travelDistance = 0;
    travelTime = 0;
		spaceship.start();
		Utils.loadMusic();
		if (!isMuted) {
			Utils.startMusic();
		}
	}
	
	/**
	 * Rotates the given before vector in the game's agnle accoring to the given
	 * angle's factors. First according to X, then Y and finally Z.
	 * @param before The vector to rotate
	 * @param xFactor The angle's X factor
	 * @param yFactor The angle's Y factor
	 * @param zFactor The angle's Z factor
	 * @return The rotated vector
	 */
	private Vec rotateByAngle(Vec before, double xFactor, double yFactor, double zFactor) {
		Vec after = before;
		// Rotate around the X axis
		if (xFactor != 0) {
			Vec tmp = new Vec(after);
			double cos = Math.cos(xFactor * angle * Math.PI / 180);
			double sin = Math.sin(xFactor * angle * Math.PI / 180);
			tmp.x = after.x;
			tmp.y = cos * after.y - sin * after.z;
			tmp.z = cos * after.z + sin * after.y;
			after = tmp;
		}
		// Rotate around the Y axis
		if (yFactor != 0) {
			Vec tmp = new Vec(after);
			double cos = Math.cos(yFactor * angle * Math.PI / 180);
			double sin = Math.sin(yFactor * angle * Math.PI / 180);
			tmp.x = cos * after.x + sin * after.z;
			tmp.y = after.y;
			tmp.z = cos * after.z - sin * after.x;
			after = tmp;
		}
		// Rotate around the Z axis
		if (zFactor != 0) {
			Vec tmp = new Vec(after);
			double cos = Math.cos(zFactor * angle * Math.PI / 180);
			double sin = Math.sin(zFactor * angle * Math.PI / 180);
			tmp.x = cos * after.x - sin * after.y;
			tmp.y = cos * after.y + sin * after.x;
			tmp.z = after.z;
			after = tmp;
		}
		return after;
	}
	
	/**
	 * Checks whether there's a collision between the spaceship and one of the
	 * asteroids in the game
	 */
	private void checkCollision() {
		isBonus = false;
		// First, rotate the spaceship's boundaries according to the game's angle:
		ISphericalObstacle[] actualBounds = null;
		if (angle == 0) {
			actualBounds = spaceship.getSphericalBoundaries();
		} else {
			ISphericalObstacle[] spaceshipBounds = spaceship.getSphericalBoundaries();
			actualBounds = new ISphericalObstacle[spaceshipBounds.length];
			for (int i = 0; i < actualBounds.length; ++i) {
				Vec newCenter = rotateByAngle(spaceshipBounds[i].center(), 0, 0, -1);
				actualBounds[i] =
					new AbstractSphericalObstacle(newCenter, spaceshipBounds[i].radius());
			}
		}
		// Iterate over all the asteroids
		for (Asteroid asteroid : asteroids) {
			// Rotate the asteroid boundaries according to the game's angle
			Vec asteroidCenter = asteroid.center();
			if (angle != 0) {
				// Rotate around the Y axis first, and then around the Z axis
				asteroidCenter = rotateByAngle(asteroidCenter, 0, 1, 0.4);				
			}
			// Iterate over all the spaceship's boundaries
			for (ISphericalObstacle spaceshipBoundary : actualBounds) {
				// Calculate the distance to see if there's a collision
				Vec boundaryCenter = spaceshipBoundary.center();
				double distance = Vec.sub(asteroidCenter, boundaryCenter).length();
				// Check the collision
				if (distance > asteroid.radius() + spaceshipBoundary.radius()) {
					// No collision
					continue;
				}
				if (asteroid.getType() == AsteroidType.BonusAsteroid) {
					// It's a bonus asteroid. Reward!
					isBonus = true;
					score += 100;
				} else {
					// There's a collision. Save the collision's values and return
					collision = new Vec[3];
					collision[0] = asteroidCenter;
					collision[2] = spaceshipBoundary.center();
					double scaleRatio = spaceshipBoundary.radius();
					scaleRatio /= spaceshipBoundary.radius() + asteroid.radius();
					collision[1] = Vec.add(collision[2],
							Vec.scale(scaleRatio, Vec.sub(collision[0], collision[2])));
					// Game is over after a collision was found
					isGameOver = true;
					spaceship.stop();
					if (Utils.isMusicPlayed()) {
						Utils.stopMusic();
					}
					return;
				}
			}
		}
	}
	
	/**
	 * Updates the game's angle according to the left/right turns flags.
	 */
	private void updateAngle() {
		if (isTurnRight && !isTurnLeft) {
			// Turning right
			angle = Math.min(MAX_ANGLE_DEGREES, angle + ANGLE_CHANGE_DEGREES);
			return;
		}
		if (isTurnLeft && !isTurnRight) {
			// Turn left
			angle = Math.max(-MAX_ANGLE_DEGREES, angle - ANGLE_CHANGE_DEGREES);
			return;
		}
		// In case of cruise control, keep the same direction
		if (isCruiseControl) {
			return;
		}
		if (!(isTurnLeft || isTurnRight)) {
			// Return to default position
			if (angle > 0) {
				angle--;
			} else if (angle < 0) {
				angle++;
			}
		}
	}
	
	/**
	 * Update the asteroids state in the game:
	 * 1. Create new asteroids if there are less than needed.
	 * 2. Move the asteroids towards the ship (simulate the ship's movement).
	 */
	private void updateAsteroids() {
		// Update the location of the asteroids (and remove those off limit)
		double dirAngle = angle * Math.PI / 180;
		Vec direction = new Vec(-asteroidSpeed * Math.sin(dirAngle), 0, asteroidSpeed * Math.cos(dirAngle));
		Iterator<Asteroid> iterator = asteroids.iterator();
		while (iterator.hasNext()) {
			Asteroid asteroid = iterator.next();
			asteroid.updateLocation(direction);
			// Check if the asteroid should be removed (fade away)
			if (asteroid.center().z > ASTEROIDS_FADE_INDEX) {
				iterator.remove();
			}
		}
		// Check whether new asteroids should be created
		if (--createAsteroidsCyclesCounter == 0) {
			createAsteroidsCyclesCounter = ASTEROIDS_CREATION_CYCLES;
			if (asteroids.size() < ASTEROIDS_MAX_COUNT) {
				// New asteroids are required. Create them
				createNewAsteroids();
			}
		}
	}
	
	/**
	 * Creates new asteroids
	 */
	private void createNewAsteroids() {
		int numToCreate = Math.max(ASTEROIDS_CREATION_COUNT, ASTEROIDS_MIN_COUNT - asteroids.size());
		for (int i = 0; i < numToCreate; i++) {
			double radius = 0.5 + Utils.randDouble();
			double angle = MAX_ANGLE_DEGREES - (Utils.randInt(2 * (int)MAX_ANGLE_DEGREES));
			angle *= Math.PI / 180;
			Vec center = new Vec();
			center.x = FARTHEST_ASTEROID_DISTANCE * Math.sin(angle);
			center.y = Utils.randDouble() * 2 - 1;
			center.z = -FARTHEST_ASTEROID_DISTANCE * Math.cos(angle);
			if (Utils.randInt(20) == 0) {
				asteroids.add(new Asteroid(center, radius, AsteroidType.BonusAsteroid));
			} else {
				asteroids.add(new Asteroid(center, radius));
			}
    }
	}
}
