/*
 * This class defines some static methods of image processing.
 */

package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProc {

	public static int padType;

	public static BufferedImage invoke(BufferedImage img, NeighborhoodOp op) {
		BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Neighborhood nb = new Neighborhood(img, x, y, op.size(), padType);
				Color rgb = op.process(nb);
				out.setRGB(x, y, rgb.getRGB());
			}
		}
		return out;
	}
	
	public static int colorTruncate(float x) {
		int ret = Math.round(x);
		if (ret > 255)
			return 255;
		if (ret < 0)
			return 0;
		return ret;
	}
	
	public static BufferedImage shrink(BufferedImage img, int factor) {
		if (factor <= 0)
			throw new IllegalArgumentException();
		int newHeight = img.getHeight()/factor;
		int newWidth = img.getWidth()/factor;
		BufferedImage out = new BufferedImage(newWidth, newHeight, img.getType());
		for (int x = 0; x < newWidth; x++)
			for (int y = 0; y < newHeight; y++)
				out.setRGB(x, y, img.getRGB(x*factor, y*factor));
		return out;
	}
	
}
