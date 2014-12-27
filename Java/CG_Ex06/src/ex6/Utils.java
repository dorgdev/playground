/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.media.opengl.GLException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * An abstract class providing utilities attributes
 */
public final class Utils {

	/** The available control keys (as a viewable string) */
	public static final String CONTROL_KEYS =
		"Controls: s,m,g,p,j,r,c,d,t,a,q,->,<-,+,-";
	/** The audio file's name */
	public static final String AUDIO_FILE = "audio.wav";
	
	/** An RNG for utilities methods */
	private static final Random RNG = new Random();
	/** The resources directory */
	private static final String RESOURCES_DIR = "res";
	/** The available texture */
	private static Texture[] TEXTURES = new Texture[TextureType.values().length];
	/** The audio clip to play in the background */
	private static Clip audioClip = null;
	/** Whether the audio currently plays */
	private static boolean isAudioPlayed = false;

	/**
	 * An enum describing the type of textures available
	 */
	public enum TextureType {
		Asteroid("asteroid.jpg"),
		Checker("checker.gif"),
		Fire("fire.jpg"),
		Stars("stars.png"),
		Bonus("star.jpg");

		/** The texture's filename */
		private String filename;
		
		/**
		 * @param filename The filename of the texture
		 */
		TextureType(String filename) {
			this.filename = filename;
		}
	}
	
	/**
	 * @return whether the music is currently played
	 */
	public static boolean isMusicPlayed() {
		return isAudioPlayed;
	}

	/**
	 * Loads the music to the game
	 */
	public static void loadMusic() {
		if (audioClip != null) {
			audioClip.stop();
		}
		try {
  		File audioFile = new File(RESOURCES_DIR + File.separator + AUDIO_FILE);
  		audioClip = AudioSystem.getClip();
  		audioClip.open(AudioSystem.getAudioInputStream(audioFile));
		} catch (Exception e) {
	   	System.err.println("Urr... Couldn't load music...");
    	e.printStackTrace();
    	audioClip = null;
 		}
	}
	
	/**
	 * Starts the game's music
	 */
	public static void startMusic() {
		if (audioClip == null) {
			return;
		}
    audioClip.start();
  	isAudioPlayed = true;
	}

	/**
	 * Stops the game's music
	 */
	public static void stopMusic() {
  	isAudioPlayed = false;
		if (audioClip != null) {
			audioClip.stop();
		}
	}
	
	/**
	 * Loads all the textures.
	 */
	public static void loadTextures() {
		for (TextureType type : TextureType.values()) {
			buildTexture(type);
		}
	}
	
	/**
	 * Retrieves a texture from the available textures, or null if doesn't exist
	 * @param type The type of texture
	 * @return The Texture instance, or null if doens't exist
	 */
	public static Texture getTexture(TextureType type) {
		return TEXTURES[type.ordinal()];
	}
	
	/**
	 * @return A random generated float
	 */
	public static float randFloat() {
		return RNG.nextFloat();
	}

	/**
	 * @return A random generated double
	 */
	public static double randDouble() {
		return RNG.nextDouble();
	}

	/**
	 * Returns a random generated int value in the range [0,max]
	 * @param max The maximal value possible
	 * @return A random generated int value in the range [0,max]
	 */
	public static int randInt(int max) {
	  return RNG.nextInt(max + 1);
	}

	/**
	 * Build a texture according to its type.
	 * @param type The type of texture to build
	 */
	private static void buildTexture(TextureType type) {
		File textureFile = new File(RESOURCES_DIR + File.separator + type.filename);
		try {
      TEXTURES[type.ordinal()] = TextureIO.newTexture(textureFile, true);
    } catch (GLException e) {
    	System.err.println("Failed to load the texture: " + type.filename);
      e.printStackTrace();
    } catch (IOException e) {
    	System.err.println("Failed to load the texture: " + type.filename);
      e.printStackTrace();
    }
	}
	
	/** private CTOR to prevent instantiation */
	private Utils() {}
	
}
