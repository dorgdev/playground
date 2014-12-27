/*
 * This class adds Gaussian noise to an image with given standard deviation. Works separately in each color channel.
 */

package edu.cg;

import java.awt.Color;
import java.util.Random;

public class AddNoiseOp implements NeighborhoodOp {

	private Random rand = new Random();
	private float stdev;
	
	public AddNoiseOp(float stdev) {
		if (stdev <= 0)
			throw new IllegalArgumentException();
		this.stdev = stdev;
	}
	
	@Override
	public Color process(Neighborhood data) {
		Color orig = data.get(0, 0);
		int red = ImageProc.colorTruncate(randGaussian(orig.getRed(), stdev));
		int green = ImageProc.colorTruncate(randGaussian(orig.getGreen(), stdev));
		int blue = ImageProc.colorTruncate(randGaussian(orig.getBlue(), stdev));
		return new Color(red, green, blue);
	}

	private float randGaussian(float mean, float stdev) {
		return (float)(stdev*rand.nextGaussian() + mean);
	}
	
	@Override
	public int size() {
		return 1;
	}

}
