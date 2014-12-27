package ex3;

import java.io.File;

import ex3.gui.MainFrame;

/**
 * You basically don't need to add things here to the this package
 */
public class Ex3 {

	private static String getAboutMessage() {
		return "GUI for exercise3:\n" +
				"1.  Created an RGB class, handling 3 channels color, with some abilities\n" +
				"    to perform operations on colors, other RGBs, parsing, etc.\n" +
				"2.  Changed the parsing so it will add elements to the scene one by one\n" +
				"    as they are parsed (so it won't need to hold all the raw elements).\n" +
				"3.  Created a common SceneElement ancestor to all elements, and a Light\n" +
				"    ancestor to all light sources.\n" +
				"4.  Spheres have also the ability to have a texture over them (instead\n" +
				"    of plain color). It is possible to control the texture direction.\n" +
				"    Use the space.xml scene for an example.\n" +
				"5.  I've created a Utils class holding all XML values, and several utilities\n" +
				"    values and static method.\n" +
				"6.  I moved the scene file control to the Utils class, so it would be\n" +
				"    and added a GetResource method for loading external resources (i.e.,\n" +
				"    the texture on the sphere, or the background image).\n" +
				"7.  Created an abstract Plain class representing a plain. Intersection\n" +
				"    with a plain is found using Cramer's rule for solving a matrix.\n" +
				"8.  A side from Triangle, also add a Circle class which could be used for\n" +
				"    an empty circle (ring) or as a full circle. See saturn*.xml scenes.\n" +
				"9.  Created a Pyramid class which derives from the Trimesh and creates\n" +
				"    Triangles meshes according to the tip of the pyramid and a base (for\n" +
				"    polygonial base). See pyramids.xml scene.\n" +
				"10. Added support for transparency (including a visible background image).\n" +
				"    See transparency.xml scene.\n" +
				"11. Accelerated the trimesh intersection by surrounding it with a bounding\n" +
				"    box and intersect it before the triangles themselves.\n\n" +
				"Please see the description document submitted with the exercise for\n" +
				"more explanations and implemetation notes.";
	}

	/**
	 * Main method. Command line usage is: <input scene filename> <canvas width>
	 * <canvas height> <target image filename>
	 */
	public static void main(String[] args) {

		String sceneFilename = null;
		String imageFilename = null;
		int canvasWidth = 480;
		int canvasHeight = 360;

		if (args.length > 0) {
			sceneFilename = args[0];
		}
		if (args.length > 2) {
			canvasWidth = Integer.valueOf(args[1]);
		}
		if (args.length > 2) {
			canvasHeight = Integer.valueOf(args[2]);
		}
		if (args.length > 3) {
			imageFilename = args[3];
		}

		// Init GUI
		MainFrame mainFrame = new MainFrame();
		mainFrame.initialize(sceneFilename, canvasWidth, canvasHeight,
				getAboutMessage());

		if (imageFilename == null) {
			mainFrame.setVisible(true);
		} else {
			// Render to file and quit
			mainFrame.render();
			mainFrame.saveRenderedImage(new File(imageFilename));
			System.exit(1);
		}
	}
}
