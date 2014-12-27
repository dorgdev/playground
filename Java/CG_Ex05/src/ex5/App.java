/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;

import ex5.models.CubeModel;
import ex5.models.Empty;
import ex5.models.IRenderable;
import ex5.models.Spaceship;


public class App {

	/** The default width of the frame */
	public static int DEFAULT_WIDTH = 600;
	/** The default height of the frame */
	public static int DEFAULT_HEIGHT = 500;
	/** A list of all the supported models */
	static IRenderable[] models = {new CubeModel(), new Spaceship(), new Empty()};
	/** The current used model */
	static int currentModel;
	/** The previous point where the mouse was pressed on */
	static Point prevMouse;
	/** The main frame of the application */
	static Frame frame;
	
	/**
	 * Create frame, canvas and viewer, and load the first model.
	 * 
	 * @param args
	 *            No arguments
	 */
	public static void main(String[] args) {

		frame = new JFrame();
		
		// Create viewer and initialize with first model
		final Viewer viewer = new Viewer();
		viewer.setModel(nextModel());
		
		final GLJPanel canvas = new GLJPanel();
		
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		frame.setLayout(new BorderLayout());		
		frame.add(canvas, BorderLayout.CENTER);
		
		// Put it in the center of the screen
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenDimension.width - DEFAULT_WIDTH) / 2,
										  (screenDimension.height - DEFAULT_HEIGHT) / 2);

		// Add event listeners
		canvas.addGLEventListener(viewer);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});
		
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				switch (e.getKeyChar()) {
          case 'p':
    				// Toggles wireframe mode
  					viewer.toggleRenderMode();
	          break;
          case 'a':
          	// Toggle axes
          	viewer.toggleAxes();
          	break;
          case 'l':
          	// Toggle light spheres
        		viewer.toggleLightSpheres();
        		break;
          case 'm':
          	// Show next model
          	IRenderable model = nextModel();
          	viewer.setModel(model);
          	break;
          case 's':
          	// Show next model
          	viewer.subdivide();
          	break;
          case 'r':
          	// Reset the model
          	viewer.resetModel();
          	break;
          case 'v':
          	// Reset the view
          	viewer.resetView();
          	break;
          case 'q':
          	// Quits the application
          	System.exit(1);
          default:
	          break;
				}
				
				canvas.repaint();
			}
		});
		
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				
				// Let mouse drag affect trackball view
				if (prevMouse.equals(e.getPoint())) {
					// Dragging resolution is probably very delicate - don't rotate
					// similar equivalent points
					return;
				}
				viewer.trackball(prevMouse, e.getPoint());
				prevMouse = e.getPoint();
			}
		});
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				prevMouse = e.getPoint();
				viewer.startAnimation();
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				viewer.stopAnimation();
				super.mouseReleased(arg0);
			}
		});
		
		canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				
				// Let mouse wheel affect zoom 
				int rot = e.getWheelRotation();
				
				if (rot == 0) {
					return;
				}
				viewer.zoom(-rot);
				canvas.repaint();
			}
		});
		
		// Show frame
		canvas.setFocusable(true);
		canvas.requestFocus();
		frame.setVisible(true);
		canvas.repaint();
	}
	
	/**
	 * Return the next model in the array
	 * 
	 * @return Renderable model
	 */
	private static IRenderable nextModel() {
		IRenderable model = models[currentModel++];
		frame.setTitle("Exercise 5 - " + model.toString());
		currentModel = currentModel % models.length;
		
		return model;
	}
}
