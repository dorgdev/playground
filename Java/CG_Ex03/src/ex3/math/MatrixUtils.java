package ex3.math;

/**
 * Handles some matrix calculation utilities.
 * 
 * @author dor
 */
public final class MatrixUtils {

	/**
	 * Helper method for the intersect method. Solves the matrix representing the
	 * intersection of the 3 points of a triangle and the ray. If there's a single
	 * possible intersection point, returns it, otherwise (line intersection or
	 * no intersection) returns null.
	 * @param values The coefficient of the t, u1, u2 and free val values.
	 * @return The single solution to the matrix.
	 */
	public static Point3D solveMatrix(double[][] mat) {
		// We can use Cramer's rule for solving this matrix :)
		// First, calculate the determinant of the delta:
		double delta = determinant(new double[][] { mat[0], mat[1], mat[2] });
		if (delta == 0) {
			// No solution!
			return null;
		}
		// Find the determinants for all variables:
		double tDet = determinant(new double[][] { mat[3], mat[1], mat[2] });
		double u1Det = determinant(new double[][] { mat[0], mat[3], mat[2] });
		double u2Det = determinant(new double[][] { mat[0], mat[1], mat[3] });
		// Return the solutions
		return new Point3D(tDet / delta, u1Det / delta, u2Det / delta);
	}
	
	/**
	 * Calculates the determinant of 3X3 matrix
	 * @param mat The matrix
	 * @return The determinant
	 */
	public static double determinant(double[][] mat) {
		return
			mat[0][0] * (mat[1][1] * mat[2][2] - mat[1][2] * mat[2][1]) -
			mat[0][1] * (mat[1][0] * mat[2][2] - mat[1][2] * mat[2][0]) +
			mat[0][2] * (mat[1][0] * mat[2][1] - mat[1][1] * mat[2][0]);
	}

	
	/**
	 * Private CTOR to prevent instantiation.
	 */
	private MatrixUtils() {}
	
}
