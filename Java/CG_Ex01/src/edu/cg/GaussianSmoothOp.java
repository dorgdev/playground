/*
 * This class implements Gaussian smoothing (aka blurring) on an image. Works on each color channel separately.
 */

package edu.cg;


public class GaussianSmoothOp extends ConvolutionOp {

	public GaussianSmoothOp(int size, float variance) {
		super(size, generateGaussianKernel(size, variance));
	}
	
	private static float[][] generateGaussianKernel(int size, float variance) {
		if (size % 2 == 0) {
			throw new IllegalArgumentException("Kernel size should be odd!");
		}
		// First, calculate the sum of all values in the Gaussian kernel
		float sum = 0;
		int median = size / 2;
		float[][] rc = new float[size][size];
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				float val = calcValue(x - median, y - median, variance);
				rc[x][y] = val;
				sum += val;
			}
		}
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				rc[x][y] /= sum;
			}
		}
		return rc;
	}

	private static float calcValue(int x, int y, float variance) {
		float scalar = (float)(1 / (2 * Math.PI * variance));
		float pow = (float)(Math.pow(Math.E, - (x * x + y * y) / (2 * variance)));
		return scalar * pow;
	}
}
