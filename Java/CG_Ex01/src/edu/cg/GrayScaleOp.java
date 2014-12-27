package edu.cg;

import java.awt.Color;

/**
 * This class converts the image to a gray scale image using the average value
 * of the 3 channels (red, green and blue).
 */
public class GrayScaleOp implements NeighborhoodOp {

	public static Color grayScale(Color c) {
		int value = (c.getBlue() + c.getRed() + c.getGreen()) / 3;
		return new Color(value, value, value);
	}
	
	public GrayScaleOp() {}
	
	@Override
	public Color process(Neighborhood data) {
		return grayScale(data.get(0, 0));
	}
	
	@Override
	public int size() {
		return 1;
	}
}
