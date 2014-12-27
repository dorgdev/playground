/*
 * This class defines a neighborhood around a specific point in an image.
 */

package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Neighborhood {

	public static final int ZERO_PAD = 0;
	public static final int MIRROR_PAD = 1;

	private final BufferedImage img;
	private final int size;
	private final int type;
	private final int cx, cy;
	
	public Neighborhood(BufferedImage img, int x, int y, int size, int type) {
		this.img = img;
		this.size = size;
		this.type = type;
		this.cx = x - size/2;
		this.cy = y - size/2;
	}
	
	public Color get(int x, int y) {
		if (x < 0 || y < 0 || x > size-1 || y > size-1)
			throw new ArrayIndexOutOfBoundsException();
		int height = img.getHeight();
		int width = img.getWidth();
		int curx = cx + x;
		int cury = cy + y;
		
		switch (type) {
		case MIRROR_PAD:
			if (curx < 0 && curx > -width) {
				curx = -curx;
			} else if (curx >= width && curx < width * 2) {
				curx = 2 * cx - curx;
			}
			if (cury < 0 && cury > -height) {
				cury = -cury;
			} else if (cury >= height && cury < height * 2) {
				cury = 2 * cy - cury;
			}
			if (curx >= 0 && cury >= 0 && curx < width && cury < height) {
				return new Color(img.getRGB(curx, cury));
			}
			return new Color(0,0,0);
		case ZERO_PAD:
		default:
			if (curx >= 0 && cury >= 0 && curx < width && cury < height) {
				return new Color(img.getRGB(curx, cury));
			}
			return new Color(0,0,0);
		}
	}
	
	public int size() {
		return size;
	}
	
}
