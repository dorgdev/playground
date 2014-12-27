package ex3.math;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * This class represents a point in a 3D world.
 * @author dor
 */
public class Point3D {

	/**
	 * Point data. Allowed to be accessed publicly for performance reasons
	 */
	public double x, y, z;
	
	/**
	 * Creates a point from a vec. The created point will have the same coordinates
	 * as the vector - as if it's the point lying at the end of the vector when its
	 * tail is positioned at (0,0,0).
	 * @param v The vector to build the point from
	 */
	public Point3D(Vec v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	/**
	 * Creates a point from another point (copy CTOR).
	 * @param other The other point to take the values from
	 */
	public Point3D(Point3D other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	/**
	 * Creating a point from its coordinates.
	 * @param x The point's X coordinate
	 * @param y The point's Y coordinate
	 * @param z The point's Z coordinate
	 */
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Calculates the vector pointing from another point to this one (the affine
	 * points subtraction operation).
	 * @param other The other point, lying at the tail of the returned vector
	 * @return The vector pointing from the other point to this point
	 */
	public Vec vecFrom(Point3D other) {
		return new Vec(x - other.x, y - other.y, z - other.z);
	}

	/**
	 * Finds the point at the end of the vector starting at this point, and ending
	 * at the head of the vector.
	 * @param v The vector for the calculation
	 * @return The distant point
	 */
	public Point3D addVec(Vec v) {
		return new Point3D(x + v.x, y + v.y, z + v.z);
	}
	
	/**
	 * Parses a string with the 3 point's values and create a point instance.
	 * @param s The formatted string: "x y z"
	 * @return The point (x,y,z)
	 */
	public static Point3D ParsePoint3D(String s) {
		return ParseSeveral(s).get(0);
	}

	public double distance(Point3D p) {
		return Math.sqrt((x - p.x) * (x - p.x) + 
										 (y - p.y) * (y - p.y) + 
										 (z - p.z) * (z - p.z));
	}
	
	/**
	 * Parses a string with several points values, and return a list of all the
	 * parsed points.
	 * @param s The formatted string: "x1 y1 z1 [x2 y2 z2 [x3 y3 z3 ...]]"
	 * @return A list of the points: (x1,y1,z1) [(x2,y2,z2) [(x3,y3,z3) ...]]
	 */
	public static List<Point3D> ParseSeveral(String s) {
		Scanner scanner = new Scanner(s);
		List<Point3D> points = new LinkedList<Point3D>();
		while (scanner.hasNextDouble()) {
			double x = scanner.nextDouble();
			double y = scanner.nextDouble();
			double z = scanner.nextDouble();
			points.add(new Point3D(x, y, z));
		}
		return points;
	}
	
	/**
	 * Returns a string that contains the values of this point. The form is
	 * (x,y,z).
	 * 
	 * @return the String representation
	 */
	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

}
