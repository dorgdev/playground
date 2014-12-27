/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;

import ex6.models.Spaceship;


/**
 * Entry point of the game, and also user IO handler (Controller in the MVC
 * paradigm)
 */
public class App {
	
	/** The main frame of the game */
	private static Frame frame;
	/** The GL canvas drawing the game */
	private static GLCanvas canvas;
	/** The model part in the MVC pattern. Handles the game state */
	private static GameLogic game;
	/** The view part in the MVC pattern. Handles the drawing */
	private static Viewer viewer;
	
	/**
	 * Create frame, canvas and viewer, and load the first model.
	 * 
	 * @param args
	 *            No arguments
	 */
	public static void main(String[] args) {

		frame = new Frame("ex6: AsteroidBelt");

    // Create game logic and viewer
		Spaceship spaceship = new Spaceship();
		game = new GameLogic(spaceship);
		viewer = new Viewer(game, spaceship);
		
		canvas = new GLCanvas();
		
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());		
		frame.add(canvas, BorderLayout.CENTER);
		
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
				case 'r':
	        game.restart();
	        break;
				case 'p':
					game.togglePause();
					break;
				case 's':
					viewer.toggleShip();
					break;
				case 'j':
					viewer.toggleProjection();
					break;
				case 'm':
					viewer.toggleShipMark();
					break;
				case 'g':
					game.toggleGhostMode();
					break;
				case 'c':
					game.toggleCruiseControl();
					break;
				case 'd':
					viewer.toggleShowDirection();
					break;
				case 't':
					viewer.toggleShowTextInfo();
					break;
				case 'a':
					game.toggleMusic();
					break;
				case '-':
					game.decreaseSpeed();
					break;
				case '+':
					game.increaseSpeed();
					break;
				case 'q':
					System.exit(0);
				default:
					System.out.println("Unknown key pressed: " + e.getKeyChar());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT:					
					game.setTurnLeft(true);
					break;
				case KeyEvent.VK_RIGHT:
					game.setTurnRight(true);
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT:					
					game.setTurnLeft(false);
					break;
				case KeyEvent.VK_RIGHT:
					game.setTurnRight(false);
					break;
				}
			}
			
		});

		// Put the frame in the middle of the screen
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenDimension.width - frame.getWidth()) / 2,
										  (screenDimension.height - frame.getHeight()) / 2);

		// Show frame
		frame.setVisible(true);
		canvas.requestFocus();
		canvas.requestFocusInWindow();
	}
}
