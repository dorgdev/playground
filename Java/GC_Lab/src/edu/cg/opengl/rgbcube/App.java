package edu.cg.opengl.rgbcube;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;

import com.sun.opengl.util.FPSAnimator;

/**
 * OpenGL RGB cube visualizer
 *
 */
public class App implements GLEventListener {

	private Point prevMouse;
	private double rotY;
	private double rotX;
	
	public static void main(String[] args) {
		
		final App app = new App();
		
		// AWT Init
		Frame frame = new Frame();
		GLCanvas canvas = new GLCanvas();		
		frame.setSize(300, 300);
		frame.setLayout(new BorderLayout());
		frame.add(canvas,BorderLayout.CENTER);
		
		// Event handling
		canvas.addGLEventListener(app);
		frame.addWindowListener(new WindowAdapter() {			
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				super.windowClosing(e);
			}			
		});
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				
				// Remember click position
				app.prevMouse = e.getPoint();
			}			
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				
				// Calc difference from previous mouse location
				Point prev = app.getPreviousMouseLocation();
				int dx = prev.x - e.getX();
				int dy = prev.y - e.getY();
				
				// Rotate model
				app.rotate(dx, dy);
				
				// Remember mouse location 
				app.setPreviousMouseLocation(e.getPoint());
			}					
		});
		
		// Display GUI
		frame.setVisible(true);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		//TODO: Uncomment the following 2 lines. See how each affects the final result.

		// Clear buffers
		//gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		// BTW, the previous two lines could be comined into gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT); 
		// Bonus riddle: if you don't clear the color buffer, and then rotate the scene, it flickers. Why?

		//TODO: Play with the following3 lines. Test what happens when you:
		//		1. Disable all.
		//		2. Enable only the first two
		//		3. Enable only the last one
		//gl.glCullFace(GL.GL_BACK); // Set Culling Face To Back Face
        //gl.glEnable(GL.GL_CULL_FACE); //Enable back face culling
        //gl.glPolygonMode(GL.GL_BACK, GL.GL_POINT); //Make the back faces be shown as points rather than filled polygons
        
		// Create camera transformation
		setupCamera(gl);		
		
		// Save camera transformation
		gl.glPushMatrix();
		
		//TODO: Set point and line attributes: point size of 5 pixels, and line width of 5 pixels.

		
		// Draw Line RGB cube
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		// The following line makes the cube be drawn at x=-3
		gl.glTranslated(-3, 0, 0);
		drawRGBCube(gl);
		
		// Draw Fill RGB cube
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);		
		// The following line makes the cube be drawn at x=-3+3=0
		gl.glTranslated(3, 0, 0);		
		drawRGBCube(gl);

		// Draw Point RGB cube
		//TODO: Translate the model-view to wherever you want to draw (which is at x=3), and draw the point cube.
		
		// Load camera transformation
		gl.glPopMatrix();
	}
	
	/**
	 * Draw a unit size RGB cube
	 * @param gl Current GL context
	 */
	private void drawRGBCube(GL gl) {						
		
		gl.glBegin(GL.GL_QUADS);
		
		//TODO: Make each vertex colorful. XYZ going from -1 to +1 should correspond to RGB going from 0 to 1.
		gl.glVertex3d(-1,-1,+1);
		gl.glVertex3d(+1,-1,+1);
		gl.glVertex3d(+1,+1,+1);
		gl.glVertex3d(-1,+1,+1);						
		
		//TODO: Add the other faces of the cube
		
		gl.glEnd();				
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		
		// Create debugging pipe
		drawable.setGL(new DebugGL(drawable.getGL()));
				
		// Setup animation callback to call display 60 times per second
		FPSAnimator ani = new FPSAnimator(60, true);
		ani.add(drawable);
		ani.start();
		
		GL gl = drawable.getGL();
		
		//TODO: Uncomment the following line.
		// Set background color to gray
		//gl.glClearColor(0.5f, 0.5f, 0.5f, 0);
		

		//TODO: Uncomment the following line. See what's its influence on the final scene.
		// Enable depth test
		//gl.glEnable(GL.GL_DEPTH_TEST);

		// Place camera at (0,0,10)
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();			
		gl.glTranslated(0, 0, -10);	
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {			
		
		GL gl = drawable.getGL();
		
		// Create projection transformation
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		double w,h;
		w = 10;
		h = 10*((double)height/width);
		gl.glOrtho(-w/2, w/2, -h/2, h/2, -1, 1000);				
	}
	
	@Override
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {		
	}	

	/**
	 * Create camera transformation such that the model is rotated around the
	 * world's X-axis and Y-axis. This is a very simple viewer.
	 * @param gl OpenGL context
	 */
	private void setupCamera(GL gl) {
		
		double [] temp = new double[16];
		
		gl.glMatrixMode(GL.GL_MODELVIEW);		
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, temp, 0);
		
		// Rotate along the (0 1 0) in world coordinates
		gl.glRotated(-rotX, temp[1], temp[5], temp[9]);
		rotX = 0;

		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, temp, 0);
		
		// Rotate along the (1 0 0) in world coordinates
		gl.glRotated(-rotY, temp[0], temp[4], temp[8]);			
		rotY = 0;				
	}
	
	/**
	 * Get previously registered mouse location on the canvas
	 * @return
	 */
	public Point getPreviousMouseLocation() {
		return prevMouse;
	}
	
	/**
	 * Set current mouse location on the canvas
	 * @param prev
	 */
	public void setPreviousMouseLocation(Point prev) {
		this.prevMouse = prev;
	}
	
	/**
	 * Rotate view along x and y axes 
	 * @param x Angles to rotate around x
	 * @param y Angles to rotate around y
	 */
	public void rotate(double x, double y) {
		rotX += x;
		rotY += y;
	}
}
