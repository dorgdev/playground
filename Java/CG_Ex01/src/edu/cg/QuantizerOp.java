/*
 * This class quantizes image colors to a given amount of levels. Quantization is done on each color channel separately.
 */

package edu.cg;

import java.awt.Color;

public class QuantizerOp implements NeighborhoodOp {

	public static float MAX_RANGE = 255;
	private float step;
	
	public QuantizerOp(int levels) {
		if (levels <= 1 || levels > 255)
			throw new IllegalArgumentException();
		this.step = MAX_RANGE/(levels-1);
	}
	
	@Override
	public Color process(Neighborhood data) {
		Color p = data.get(0,0);
		return new Color(quantize(p.getRed()),
						 quantize(p.getGreen()),
						 quantize(p.getBlue()));
	}
	
	private int quantize(int x) {
		float previousLevel = Math.round(x / step) * step;
		float nextLevel = (Math.round(x / step) + 1) * step;
		if (x - previousLevel > nextLevel - x) {
			return Math.round(nextLevel);
		} else {
			return Math.round(previousLevel);
		}
	}

	@Override
	public int size() {
		return 1;
	}

}
