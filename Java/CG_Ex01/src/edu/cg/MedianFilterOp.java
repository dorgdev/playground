/*
 * This class implements a median filter with a given window size.
 */

package edu.cg;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MedianFilterOp implements NeighborhoodOp {

	/** Red channel comparator */
	private final Comparator<Color> RED_COMPARATOR = new Comparator<Color>() {
		@Override
		public int compare(Color o1, Color o2) {
			return o1.getRed() - o2.getRed();
		}
	};

	/** Green channel comparator */
	private final Comparator<Color> GREEN_COMPARATOR = new Comparator<Color>() {
		@Override
		public int compare(Color o1, Color o2) {
			return o1.getGreen() - o2.getGreen();
		}
	};

	/** Blue channel comparator */
	private final Comparator<Color> BLUE_COMPARATOR = new Comparator<Color>() {
		@Override
		public int compare(Color o1, Color o2) {
			return o1.getBlue() - o2.getBlue();
		}
	};

	/** The size of the operator's required neighborhood */
	private int size;

	/**
	 * Creates a new instance of the MedianFilterOp from the expected size of
	 * the median window.
	 * @param size The expected median window size
	 */
	public MedianFilterOp(int size) {
		this.size = size;
	}

	@Override
	public Color process(Neighborhood data) {
		List<Color> colors = new ArrayList<Color>(size * size);
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				colors.add(data.get(x, y));
			}
		}
		Collections.sort(colors, RED_COMPARATOR);
		int red = colors.get(colors.size() / 2).getRed();
		Collections.sort(colors, BLUE_COMPARATOR);
		int blue = colors.get(colors.size() / 2).getBlue();
		Collections.sort(colors, GREEN_COMPARATOR);
		int green = colors.get(colors.size() / 2).getGreen();
		return new Color(red, green, blue);
	}
	
	@Override
	public int size() {
		return size;
	}	
	
	
}
