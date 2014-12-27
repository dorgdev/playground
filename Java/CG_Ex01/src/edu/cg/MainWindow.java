/*
 * This is the main application window.
 */

package edu.cg;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;


@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private BufferedImage img;
	private StringBuffer imgTitle;
	private Vector<JButton> operationButtons = new Vector<JButton>();
	
	private JPanel contentPane;
	private JTextField txtFilename;
	private JFormattedTextField txtGaussSize;
	private JFormattedTextField txtGaussVar;
	private JFormattedTextField txtShrinkFactor;
	private JFormattedTextField txtQuantizationLevels;
	private JFormattedTextField txtNoiseVar;
	private JFormattedTextField txtMedianSize;
	private JCheckBox chkInputKernelGray;
	private JTextArea txtStatus;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		JFrame wnd = new MainWindow();
		wnd.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setTitle("ex1: Image Processing Application");
		// The following line makes sure that all application threads are
		// terminated when this window is closed.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panelFileSelection = new JPanel();
		panelFileSelection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(panelFileSelection, BorderLayout.NORTH);
		panelFileSelection.setLayout(new BoxLayout(panelFileSelection, BoxLayout.X_AXIS));
		
		txtFilename = new JTextField();
		txtFilename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open(txtFilename.getText());
			}
		});
		panelFileSelection.add(txtFilename);
		txtFilename.setColumns(40);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				int ret = fileChooser.showOpenDialog(MainWindow.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					String filename = fileChooser.getSelectedFile().getPath();
					txtFilename.setText(filename);
					open(filename);
				}
			}
		});
		panelFileSelection.add(btnBrowse);
		
		JButton btnReload = new JButton("Reset to original");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open(txtFilename.getText());
			}
		});
		panelFileSelection.add(btnReload);
		
		JPanel panelOperations = new JPanel();
		contentPane.add(panelOperations, BorderLayout.CENTER);
		panelOperations.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panelGaussBlur = new JPanel();
		panelGaussBlur.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelGaussBlur);
		panelGaussBlur.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnGaussBlur = new JButton("Gaussian blur");
		btnGaussBlur.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int gaussianSize = Integer.parseInt(txtGaussSize.getText()); 
				float gaussianVar = Float.parseFloat(txtGaussVar.getText()); 
				try {
					NeighborhoodOp op = new GaussianSmoothOp(gaussianSize, gaussianVar);
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("GaussianBlur(size=" + gaussianSize + ",var=" + gaussianVar + ")");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in Gaussian blurring, check the parameters!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnGaussBlur.setEnabled(false);
		panelGaussBlur.add(btnGaussBlur);
		operationButtons.add(btnGaussBlur);
		
		JLabel lblSize = new JLabel("Size:");
		panelGaussBlur.add(lblSize);
		
		txtGaussSize = new JFormattedTextField(new Integer(5));
		txtGaussSize.setColumns(5);
		panelGaussBlur.add(txtGaussSize);
		
		JLabel lblGaussVar = new JLabel("Variance:");
		panelGaussBlur.add(lblGaussVar);
		
		txtGaussVar = new JFormattedTextField(new Float(1.0));
		txtGaussVar.setColumns(5);
		panelGaussBlur.add(txtGaussVar);
		
		JPanel panelShrink = new JPanel();
		panelShrink.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelShrink);
		panelShrink.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnShrink = new JButton("Shrink");
		btnShrink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int factor = Integer.parseInt(txtShrinkFactor.getText());
				img = ImageProc.shrink(img, Integer.parseInt(txtShrinkFactor.getText()));
				present("Shrink(factor=" + factor + ")");
			}
		});
		btnShrink.setEnabled(false);
		panelShrink.add(btnShrink);
		operationButtons.add(btnShrink);
		
		JLabel lblFactor = new JLabel("Factor:");
		panelShrink.add(lblFactor);
		
		txtShrinkFactor = new JFormattedTextField(new Integer(4));
		txtShrinkFactor.setColumns(5);
		panelShrink.add(txtShrinkFactor);
		
		JPanel panelQuantize = new JPanel();
		panelQuantize.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelQuantize);
		panelQuantize.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnQuantize = new JButton("Quantize");
		btnQuantize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int levels = Integer.parseInt(txtQuantizationLevels.getText());
				try {
					NeighborhoodOp op = new QuantizerOp(levels);
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("Quantize(levels=" + levels + ")");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in quantization, check the parameters!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnQuantize.setEnabled(false);
		panelQuantize.add(btnQuantize);
		operationButtons.add(btnQuantize);
		
		JLabel lblLevels = new JLabel("Levels:");
		panelQuantize.add(lblLevels);
		
		txtQuantizationLevels = new JFormattedTextField(new Integer(8));
		txtQuantizationLevels.setColumns(5);
		panelQuantize.add(txtQuantizationLevels);
		
		JPanel panelMedian = new JPanel();
		panelMedian.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelMedian);
		panelMedian.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnMedianFilter = new JButton("Median filter");
		btnMedianFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int medianSize = Integer.parseInt(txtMedianSize.getText()); 
				try {
					NeighborhoodOp op = new MedianFilterOp(medianSize);
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("Median(size=" + medianSize + ")");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in median filterring, check the parameters!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnMedianFilter.setEnabled(false);
		panelMedian.add(btnMedianFilter);
		operationButtons.add(btnMedianFilter);
		
		JLabel lblMedianSize = new JLabel("Size:");
		panelMedian.add(lblMedianSize);
		
		txtMedianSize = new JFormattedTextField(new Integer(3));
		txtMedianSize.setColumns(5);
		panelMedian.add(txtMedianSize);
		
		JPanel panelNoise = new JPanel();
		panelNoise.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelNoise);
		panelNoise.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNoise = new JButton("Add noise");
		btnNoise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int noiseVar = Integer.parseInt(txtNoiseVar.getText());
				try {
					NeighborhoodOp op = new AddNoiseOp((float)Math.sqrt(noiseVar));
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("Noise(var=" + noiseVar+ ")");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in noise addition, check the parameters!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNoise.setEnabled(false);
		panelNoise.add(btnNoise);
		operationButtons.add(btnNoise);
		
		JLabel lblNoiseVar = new JLabel("Variance:");
		panelNoise.add(lblNoiseVar);
		
		txtNoiseVar = new JFormattedTextField(new Float(100.0));
		txtNoiseVar.setColumns(5);
		panelNoise.add(txtNoiseVar);
		
		JPanel panelGrayScale = new JPanel();
		panelGrayScale.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelGrayScale);
		panelGrayScale.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnGrayScale = new JButton("To gray scale");
		btnGrayScale.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					NeighborhoodOp op = new GrayScaleOp();
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("GrayScale()");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in gray scale convertion!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnGrayScale.setEnabled(false);
		panelGrayScale.add(btnGrayScale);
		operationButtons.add(btnGrayScale);
		
		JPanel panelInputKernel = new JPanel();
		panelInputKernel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelInputKernel);
		panelInputKernel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnInputKernel = new JButton("Input Kernel");
		btnInputKernel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JOptionPane.showMessageDialog(
							MainWindow.this, "Please provide an input file...", 
							"Input Kernel", JOptionPane.INFORMATION_MESSAGE);
					JFileChooser fileChooser = new JFileChooser();
					int ret = fileChooser.showOpenDialog(MainWindow.this);
					if (ret != JFileChooser.APPROVE_OPTION) {
						return;
					}
					String filename = fileChooser.getSelectedFile().getPath();
					boolean grayScaled = chkInputKernelGray.isSelected();
					NeighborhoodOp op = InputKernelOp.CreateInputKernelOp(filename, grayScaled);
					BufferedImage img = ImageProc.invoke(MainWindow.this.img, op);
					MainWindow.this.img = img;
					present("InputKernel(" + filename + ")");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(MainWindow.this, "Error in input kernel, check the parameters!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnInputKernel.setEnabled(false);
		chkInputKernelGray = new JCheckBox("As gray scaled");
		chkInputKernelGray.setSelected(true);
		chkInputKernelGray.setEnabled(false);
		panelInputKernel.add(btnInputKernel);
		panelInputKernel.add(chkInputKernelGray);
		operationButtons.add(btnInputKernel);
		
		JPanel panelPadType = new JPanel();
		panelPadType.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelPadType);
		panelPadType.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JRadioButton rbZeroPad = new JRadioButton("Zero Pad");
		JRadioButton rbMirrorPad = new JRadioButton("Mirror Pad");
		ButtonGroup btnGroupPadding = new ButtonGroup();
		btnGroupPadding.add(rbZeroPad);
		btnGroupPadding.add(rbMirrorPad);
		rbZeroPad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ImageProc.padType != Neighborhood.ZERO_PAD) {
					ImageProc.padType = Neighborhood.ZERO_PAD;
					imgTitle.append("Zero Pad; ");
				}
			}
		});
		rbMirrorPad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ImageProc.padType != Neighborhood.MIRROR_PAD) {
					ImageProc.padType = Neighborhood.MIRROR_PAD;
					imgTitle.append("Mirror Pad; ");
				}
			}
		});
		rbZeroPad.setEnabled(true);
		rbMirrorPad.setEnabled(true);
		rbZeroPad.setSelected(true);
		panelPadType.add(rbZeroPad);
		panelPadType.add(rbMirrorPad);
		
		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelOperations.add(panelStatus);
		panelStatus.setLayout(new BorderLayout(0, 0));
		
		JLabel lblStatus = new JLabel(" Current image:   ");
		panelStatus.add(lblStatus, BorderLayout.WEST);
		
		txtStatus = new JTextArea(); 
		txtStatus.setEditable(false);
		txtStatus.setLineWrap(true);
		JScrollPane scrollStatus = new JScrollPane(txtStatus);
		panelStatus.add(scrollStatus);
		
		pack();
	}
	
	void open(String filename) {
		try {
			BufferedImage img = ImageIO.read(new File(filename));
			if (img == null) {
				throw new NullPointerException();
			}
			this.img = img;
			imgTitle = new StringBuffer();
			present("Opened " + new File(filename).getName());
			for (JButton btn : operationButtons)
				btn.setEnabled(true);
			chkInputKernelGray.setEnabled(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Can't open file!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	void present(String txt) {
		imgTitle.append(txt + "; ");
		txtStatus.setText(imgTitle.toString());
		ImageWindow imageWin = new ImageWindow(img, imgTitle.toString());
		imageWin.setVisible(true);
	}
	
}
