import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Simple app that will:
 * 1. loop on a directory, and display images until it finish
 * 2. when it over, demonstrate a 'system failure' with lots of error msg pops
 * 3. lock everything (alt + f4, esc, taskbar, ctrl+alt+del - this will be implemented from the OS)
 * 
 * @author benr
 *
 */

public class fullscreen extends JFrame {

	private static final String DIRECTORY = "/Users/dor/Workspace/Java/Ben/";
	private static final String IMAGE_PREFIX = "img_";
	private static final String IMAGE_FORMAT = "jpg";

	private static final String ERROR_IMAGE = "error2.jpg";


	private JLabel imageLabel;
	private Image screenImage;
	private objWin window;

	private int w,h;
	private boolean flood = false;
	private static int img_count = 0;

	public class objWin extends JWindow {
		private Image img;

		public objWin() {
		}
		public objWin(Image img) {
			this.img = img;
		}

		public void paint (Graphics g) {
			Image imageToDisplay;
			System.out.println("Repaint!");

			w = this.getWidth();
			h = this.getHeight();

			if (img != null) {
				imageToDisplay = img;
			} else {
				imageToDisplay = screenImage;
			}
			System.out.println(imageToDisplay);
			if (imageToDisplay != null) // if screenImage is not null (image loaded and ready) 
				g.drawImage(imageToDisplay, // draw it  
						w/2 - imageToDisplay.getWidth(this) / 2, // at the center  
						h/2 - imageToDisplay.getHeight(this) / 2, // of screen 
						this);
		}
	} 

	// use the count, to get the next relevant img
	private Image get_image() {

		String filename = DIRECTORY + "/" + IMAGE_PREFIX + String.valueOf(img_count) + "." + IMAGE_FORMAT;
		System.out.println("Trying to get image: " +  filename + " number: " + String.valueOf(img_count));
		if (new File(filename).exists()) { 
			img_count++;
			return Toolkit.getDefaultToolkit().getImage(filename);
		}
		return null;
	}

	// get the error image inside the directory
	private Image get_error_image() {

		String filename = DIRECTORY + "/" + ERROR_IMAGE;
		if (new File(filename).exists()) {
			return Toolkit.getDefaultToolkit().getImage(filename);
		}
		return null;
	}


	public fullscreen()  {

		screenImage = get_image();
		if (screenImage == null) { System.out.println("Cant get images!") ;System.exit(1); }

		imageLabel = new JLabel(new ImageIcon(screenImage));

		// Exiting program on mouse click 
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { System.out.println("bla!"); System.exit(0); } 
		});


		setLayout(new BorderLayout());
		add(imageLabel, BorderLayout.CENTER);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(0,0,screenSize.width, screenSize.height);
		w = this.getWidth();
		h = this.getHeight();

		setUndecorated(true);
		setVisible(false);
		// make a window on all the screen
		//		window = new objWin(); 
		//		window.setBounds(0, 0, screenSize.width, screenSize.height);
		//		window.setVisible(true);
		//		window.addMouseListener(new MouseAdapter() {
		//			public void mouseClicked(MouseEvent e) { System.out.println("From main window!"); System.exit(0); } 
		//		});

		setFocusable(true);
		imageLabel.setFocusable(true);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				System.out.println(e.getKeyChar());
				// TODO Auto-generated method stub
				if ((e.getKeyChar() >= '1' ) && ( e.getKeyChar() <= '6')) {
					System.out.println("Got here! user tpyed: " + e.getKeyChar());
					screenImage = get_image();
					if (screenImage == null) { flood = true; /*setVisibile(false);*/ }

				} else if (e.getKeyChar() == '0' ) {

					//Down arrow key code
					System.exit(0);
				}
				System.out.println("Repaint window!");
				repaint();		
			}
		});

	}

	void floodScreens() throws Exception {
		Image img = get_error_image();
		JLabel label[] = new JLabel[30];

		JFrame f[] = new JFrame[30];
		int col = 0, row = 0;

		for (int i=0; i<10; i++) {
			label[i] = new JLabel(new ImageIcon(img));
			f[i] = new JFrame();
			f[i].setLayout(new BorderLayout());
			System.out.println(label);
			f[i].add(label[i], BorderLayout.CENTER);
			f[i].setSize(439, 156);
			f[i].setUndecorated(true);
			if ((i % 7 == 0) && (i != 0)) { col += 300; row = 0;}
			row++;
			System.out.println(col + " " + row);
			f[i].setLocation(col + (row * 100), 0 + (row * 100));
			f[i].setAlwaysOnTop(true);

		}
//		Thread.sleep(1000);
		for (int j=0; j<10 ;j++) {
		  final JFrame frame = f[j];
		  SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          frame.setVisible(true);
        }
      });
//      Thread.sleep(50);

		}
	}

	void focus() {
		requestFocus();
		imageLabel.requestFocus();
		imageLabel.requestFocusInWindow();
		System.out.println(imageLabel.isFocusable());
	}

	public void paint (Graphics g) {
		System.out.println("flood: " + flood);
		if (flood) {
			System.out.println("Good flooding");
			try {
				floodScreens();
				flood = false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		System.out.println("Repaint! jframe");

		w = this.getWidth();
		h = this.getHeight();

		if (screenImage != null) // if screenImage is not null (image loaded and ready) 
			g.drawImage(screenImage, // draw it  
					w/2 - screenImage.getWidth(this) / 2, // at the center  
					h/2 - screenImage.getHeight(this) / 2, // of screen 
					this);
		this.getContentPane().setBackground(Color.white);
	}

	public static void main(String[] args) 
	{

		fullscreen fs = new fullscreen();
		fs.setAlwaysOnTop(true);
		fs.setVisible(true);
		fs.focus();

    AltTabStopper stopper = new AltTabStopper(fs);
    new Thread(stopper, "Alt-Tab Stopper").start();

	}

	public static class AltTabStopper implements Runnable
	{
	     private boolean working = true;
	     private JFrame frame;

	     public AltTabStopper(JFrame frame)
	     {
	          this.frame = frame;
	     }

	     public void stop()
	     {
	          working = false;
	     }

	     public void run()
	     {
	         try
	         {
	             Robot robot = new Robot();
	             while (working)
	             {
	                  robot.keyRelease(KeyEvent.VK_ALT);
	                  robot.keyRelease(KeyEvent.VK_TAB);
	                  frame.requestFocus();
	                  try { Thread.sleep(10); } catch(Exception e) {}
	             }
	         } catch (Exception e) { e.printStackTrace(); System.exit(-1); }
	     }
	}

}