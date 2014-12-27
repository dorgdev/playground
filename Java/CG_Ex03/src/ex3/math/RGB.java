package ex3.math;

import java.awt.Color;

/**
 * This class represents an RGB instance, holding values for all 3 light
 * channels (red, green and blue).
 * 
 * @author dor
 */
public class RGB {

	/** A constant representing an RGB instance without any color */
	public static final RGB NO_COLOR = new RGB(0, 0, 0);

	/** Red channel value */
	private double red;
	/** Green channel value */
	private double green;
	/** Blue channel value */
	private double blue;
	
	/**
	 * Constructs a new RGB instance with 0 values in all channels.
	 */
	public RGB() {
		this(NO_COLOR);
  }
	
	/**
	 * Constructs an RGB instance from another RGB instance.
	 * @param other The other instance
	 */
	public RGB(RGB other) {
		this(other.red, other.green, other.blue);
	}
	
	/**
	 * Constructs a new RGB instance from its 3 channels
	 * @param red Red channel value (0-1)
	 * @param green Green channel value (0-1)
	 * @param blue Blue channel value (0-1)
	 */
	public RGB(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
  }
	
	/**
	 * Constructs a new RGB instance from a Color object
	 * @param color Color object from which the RGB is created
	 */
	public RGB(Color color) {
		this.red = color.getRed() / 255.0;
		this.green = color.getGreen() / 255.0;
		this.blue = color.getBlue() / 255.0;
	}
	
	/**
	 * Constructs a new RGB instance from an RGB int value (correspoind to an
	 * awt.Color RGB value).
	 * @param rgb The RGB values.
	 */
	public RGB(int rgb) {
		this(new Color(rgb));
	}
	
	/**
	 * Makes sure the values of the 3 channels are in the required range, i.e.,
	 * 0-1 (inclusive).
	 */
	private void fixValues() {
		red = (red > 1) ? 1 : ((red < 0) ? 0 : red);
		green = (green > 1) ? 1 : ((green < 0) ? 0 : green);
		blue = (blue > 1) ? 1 : ((blue < 0) ? 0 : blue);
	}

	/**
	 * Returns a color representation of the RGB instance.
	 * @return A color constructed form the 3 channels' values
	 */
	public Color toColor() {
		fixValues();
		return new Color((int)(red * 255), (int)(green * 255), (int)(blue * 255));
	}
	
	@Override
	public String toString() {
	  return "Red=" + red + "; Green=" + green + "; Blue=" + blue;
	}

	/**
	 * Adds the values of another RGB instance to this one.
	 * @param other The other RGB instance
	 */
	public void add(RGB other) {
		red += other.red;
		green += other.green;
		blue += other.blue;
	}
	
	/**
	 * Multiplies each channel with the corresponding channel in the other RGB
	 * given as an argument.
	 * @param other The multiplication factors
	 */
	public void factor(RGB other) {
		red *= other.red;
		green *= other.green;
		blue *= other.blue;
	}
	
	/**
	 * Multiplies each channel with the given factor.
	 * @param factor The multiplication factor
	 */
	public void factor(double factor) {
		red *= factor;
		green *= factor;
		blue *= factor;
	}
}
