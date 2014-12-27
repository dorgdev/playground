/*
 * This interface defines a general neighborhood operation.
 */

package edu.cg;

import java.awt.Color;

public interface NeighborhoodOp {

	public Color process(Neighborhood data);
	public int size();
	
}
