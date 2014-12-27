package tests.ex09;

import java.awt.Color;
import java.awt.Toolkit;

import ex09.DisplayImage;
import ex09.Segmentation;
import junit.framework.TestCase;

public class SegmentationTest extends TestCase {

	public static final String SMALL = "/Users/dor/Workspace/Java/UnionFind/src/barcode_small.JPG";
	public static final String BARCODE = "/Users/dor/Workspace/Java/UnionFind/src/barcode.jpg";
	public static final String BW = "/Users/dor/Workspace/Java/UnionFind/src/bw.jpg";
	public static final String CIRCLES = "/Users/dor/Workspace/Java/UnionFind/src/circles.png";
	public static final String TEST = "/Users/dor/Workspace/Java/UnionFind/src/test.jpg";
	
	public void showImage(Segmentation s) {
		DisplayImage image = s.getComponentImage();
		image.show();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// Ignores it...
		}
	}
	
	public void testSmallBarcode() {
		Segmentation s = new Segmentation(SMALL);
		assertEquals(22, s.getNumComponents());
		showImage(s);
	}
	
	public void testBarcode() throws Exception {
		Segmentation s = new Segmentation(BARCODE);
		assertEquals(48, s.getNumComponents());
		showImage(s);
	}

	public void testSmallBW() {
		Segmentation s = new Segmentation(BW);
		assertEquals(124, s.getNumComponents());
		showImage(s);
	}

	public void testSmallCircles() {
		Segmentation s = new Segmentation(CIRCLES);
		assertEquals(528, s.getNumComponents());
		showImage(s);
	}
	
	public void testFileNotExist() {
		try {
			new Segmentation("Filenotfound");
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			new Segmentation(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	public void testImageBuilder() throws Exception {
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int squareFactor = 101;
		DisplayImage newImage = new DisplayImage(width, height);
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if ((x / squareFactor ) % 2 != (y / squareFactor) % 2) {
					newImage.set(x, y, Color.BLACK);
				} else {
					newImage.set(x, y, Color.WHITE);
				}
			}
		}
		newImage.show();
		Thread.sleep(1500);
		
		newImage.save(TEST);
		Segmentation s = new Segmentation(TEST);
		
		int cols = (width % squareFactor == 0 ? 0 : 1) + width / squareFactor;
		int rows = (height % squareFactor == 0 ? 0 : 1) + height / squareFactor;
		assertEquals(rows * cols, s.getNumComponents());
		
		for (int i = 1; i < squareFactor; ++i) {
			for (int j = 1; j < squareFactor; ++j) {
				assertTrue(s.areConnected(0, 0, i, j));
			}
		}
		for (int x = squareFactor; x < width; x += squareFactor) {
			for (int y = squareFactor; y < height; y += squareFactor) {
				assertFalse(s.areConnected(x, y, x - squareFactor, y - squareFactor));
			}
		}
		
		showImage(s);
		newImage.save(TEST);
	}
}
