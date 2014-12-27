/*
 * This class implements a general convolution with a given kernel, separately in each color channel.
 */

package edu.cg;

import java.awt.Color;

public class ConvolutionOp implements NeighborhoodOp {

	/** The kernel of the convolution */
	private float[][] kernel;
	/** The size of the convolution (max X and Y axis of the kernel) */
	private int size;

	public ConvolutionOp(int size, float[][] kernel) {
		this.size = size;
		this.kernel = kernel;
	}
	
	@Override
	public Color process(Neighborhood data) {
		float red = 0;
		float blue = 0;
		float green = 0;
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				Color c = data.get(size - x - 1, size - y - 1);
				float scalar = kernel[x][y];
				red += scalar * c.getRed();
				blue += scalar * c.getBlue();
				green += scalar * c.getGreen();
			}
		}
		return new Color(Math.abs((int)red), Math.abs((int)green), Math.abs((int)blue));
	}
	
	@Override
	public int size() {
		return size;
	}
}
