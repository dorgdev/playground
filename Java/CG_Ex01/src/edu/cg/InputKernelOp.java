package edu.cg;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * This class finds edges in the image.
 */
public class InputKernelOp extends ConvolutionOp {

	private boolean grayScaled;
	
	public static InputKernelOp CreateInputKernelOp(String filename) throws IOException {
		return CreateInputKernelOp(filename, false);
	}

	public static InputKernelOp CreateInputKernelOp(String filename, boolean grayScaled) throws IOException {
		float[][] kernel = generateKernelFromFile(filename);
		return new InputKernelOp(kernel, grayScaled);
	}

	private static float[][] generateKernelFromFile(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist: " + filename);
		}
		float sum = 0;
		Scanner scanner = new Scanner(file);
		List<Float> vals = new ArrayList<Float>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			StringTokenizer tokenizer = new StringTokenizer(line, "\t ,");
			while (tokenizer.hasMoreTokens()) {
				float val = Float.parseFloat(tokenizer.nextToken());
				sum += Math.abs(val);
				vals.add(val);
			}
		}
		scanner.close();
		int size = (int)Math.sqrt(vals.size());
		float[][] kernel = new float[size][size];
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				kernel[i][j] = vals.get(i * size + j) / sum;
			}
		}
		return kernel;
	}
	
	private InputKernelOp(float[][] kernel, boolean grayScaled) {
		super(kernel.length, kernel);
		this.grayScaled = grayScaled;
	}
	
	@Override
	public Color process(Neighborhood data) {
		Color c = super.process(data);
		if (grayScaled) {
			return GrayScaleOp.grayScale(c);
		}
		return c;
	}
}
