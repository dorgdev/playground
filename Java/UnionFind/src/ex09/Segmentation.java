/*
 * This file is part of "Data Structure - Exercise 9"
 */
package ex09;

import java.awt.Color;
import java.io.File;
import java.util.TreeMap;

/**
 * The class represents a segmented image.
 * It uses the image it gets in it CTOR to segment the image according to its
 * pixels' colors and then enables querying for segments' count and for each
 * 2 pixels whether or not they share the same segment.
 * It also enables creating a new image, based on the segments of the original
 * one, with random color for each segment.
 * @author Lee Khen (lee.khn@gmail.com)
 * @author Dor Gross (dorgross@gmail.com)
 */
public class Segmentation {

	/** Maximum color code when rendering an image */
	public static final int MAX_COLOR = 0xffffff;
	/** The image being segmented */
	private DisplayImage m_image;
	/** The UnionFind instance used to segment the image */
	private UnionFind m_unionFind;
	/** Segmented image width */
	private int m_width;
	/** Segmented image height */
	private int m_height;

	/* *************************** */
	/* Segmentation public methods */
	/* *************************** */

	/**
	 * Create a new Segmentation instance from the given image's file.
	 * Reads the file and then goes over the image's pixel to determine the
	 * segments of it.
	 * @param filename The image's filename
	 */
	public Segmentation(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Can't accept null value!");
		}
		if (!(new File(filename).exists())) {
			throw new IllegalArgumentException("Given file does not exist!");
		}
		m_image = new DisplayImage(filename);
		m_width = m_image.width();
		m_height = m_image.height();
		m_unionFind = new UnionFind(m_height * m_width);
		buildUnionFind();
	}
	
	/**
	 * Connects in a segment the pixels in the (x1,y1) and (x2,y2) coordinates.
	 * @param x1 First coordinate's X axis
	 * @param y1 First coordinate's Y axis
	 * @param x2 Second coordinate's X axis
	 * @param y2 Second coordinate's Y axis
	 */
	public void connect(int x1, int y1, int x2, int y2) {
		validateCoordinates(x1, y1);
		validateCoordinates(x2, y2);
		if (x1 == x2 && y1 == y2) {
			throw new IllegalArgumentException("Can't connect a pixel to " +
					"itself; Given coordinates were (" + x1 + "," + y1 + ")");
		}
		int leader1 = m_unionFind.find(coordinatesToIndex(x1, y1));
		int leader2 = m_unionFind.find(coordinatesToIndex(x2, y2));
		if (leader1 != leader2) {
			m_unionFind.union(leader1, leader2);
		}
	}
	
	/**
	 * Checks whether the two given pixel (presented by their coordinates) are
	 * in the same segment or not.
	 * @param x1 First coordinate's X axis
	 * @param y1 First coordinate's Y axis
	 * @param x2 Second coordinate's X axis
	 * @param y2 Second coordinate's Y axis
	 * @return True if share the same segment, false otherwise
	 */
	public boolean areConnected(int x1, int y1, int x2, int y2) {
		validateCoordinates(x1, y1);
		validateCoordinates(x2, y2);
		return (m_unionFind.find(coordinatesToIndex(x1, y1)) ==
			    m_unionFind.find(coordinatesToIndex(x2, y2)));
	}
	
	/**
	 * Returns the amount of segments (components) found in the image
	 * @return The amount of components
	 */
	public int getNumComponents() {
		return m_unionFind.getNumSets();
	}
	
	/**
	 * Renders a new image based on the segments of the original image.
	 * Each segment is assigned with a random color so the new image is a
	 * colorful version of the grey-level image.
	 * 
	 * IMPORTANT NOTE: We used here a TreeMap in order to correlate between the
	 * leader's ID (in range [1,height*width]) to its matching color. It could
	 * be made using an array but it would be less efficient, especially when
	 * dealing with images with a large number of segments. So instead of 
	 * writing our own BinaryTree, which was implemented already in earlier 
	 * exercises, we used JAVA's API tree.
	 * 
	 * @return A new colorful image
	 */
	public DisplayImage getComponentImage() {
		DisplayImage image = new DisplayImage(m_width, m_height);
		TreeMap<Integer, Color> map = new TreeMap<Integer, Color>();
		int leader;
		for (int y = 0; y < m_height; ++y) {
			for (int x = 0; x < m_width; ++x) {
				leader = m_unionFind.find(coordinatesToIndex(x, y));
				if (!map.containsKey(leader)) {
					map.put(leader, 
							new Color((int)(Math.random() * MAX_COLOR)));
				}
				image.set(x, y, map.get(leader));
			}
		}
		return image;
	}
	
	/* **************************** */
	/* Segmentation private methods */
	/* **************************** */
	
	/**
	 * Iterates over the image and connects the segments in the it using the
	 * UnionFind instance.
	 */
	private void buildUnionFind() {
		for (int y = 0; y < m_height; ++y) {
			for (int x = 0; x < m_width; ++x) {
				if (x != m_width - 1) {
					// Neighbor to the right
					if (m_image.isOn(x, y) == m_image.isOn(x + 1, y)) {
						connect(x, y, x + 1, y);
					}
				}
				if (y != m_height - 1) {
					// Neighbor to the bottom
					if (m_image.isOn(x, y) == m_image.isOn(x, y + 1)) {
						connect(x, y, x, y + 1);
					}
				}
			}
		}
	}
	
	/**
	 * Validates the given coordinates of (x,y) to make sure it's in bound
	 * @param The X axis
	 * @param The Y axis
	 */
	private void validateCoordinates(int x, int y) {
		if (x < 0 || x >= m_width) {
			throw new IllegalArgumentException("X value must be in the range "
					+ " of [0," + (m_height - 1) + "]");
		}
		if (y < 0 || y >= m_height) {
			throw new IllegalArgumentException("Y value must be in the range "
					+ " of [0," + (m_width - 1) + "]");
		}
	}
	
	/**
	 * Translates the given coordinates into a UnionFind element index
	 * @param x The X axis
	 * @param y The Y axis
	 * @return The matching element's index
	 */
	private int coordinatesToIndex(int x, int y) {
		return y * m_width + x + 1;
	}
}
