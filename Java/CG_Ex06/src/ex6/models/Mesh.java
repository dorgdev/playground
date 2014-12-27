/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;


/** 
 * This class defines a mesh of polygons, and provides the following 
 * functionality:
 * 1. Perform surface subdivision, including average normal computation for 
 *    each vertex.
 * 2. Display the mesh along with the normals at each vertex.
 */
public class Mesh implements Subdividable {
	public List<int[]> faces;
	public List<Vec> vertices;
	public List<Vec> normals;

	/**
	 * Creates an empty mesh
	 */
	public Mesh() {
		this(new ArrayList<int[]>(), new ArrayList<Vec>());
	}
	
	/**
	 * Creates a mesh from the given faces and vertices
	 * @param faces The mesh's faces
	 * @param vertices The mesh's vertices
	 */
	public Mesh(List<int[]> faces, List<Vec> vertices) {
		this.faces = faces;
		this.vertices = vertices;
		calculateNormals();
	}

	/**
	 * Creates a copy of the given Mesh
	 * @param s The mesh to copy
	 */
	public Mesh(Mesh s) {
		faces = new ArrayList<int[]>(s.faces.size());
		for (int[] face : s.faces) {
			faces.add(Arrays.copyOf(face, face.length));
		}
		vertices = new ArrayList<Vec>(s.vertices.size());
		for (Vec vertex : s.vertices) {
			vertices.add(new Vec(vertex));
		}
		normals = new ArrayList<Vec>(s.normals.size());
		for (Vec normal : s.normals) {
			normals.add(new Vec(normal));
		}
	}

	@Override
	public void subdivide(int levels) {
		for (int i = 0; i < levels; i++) {
			refineTopology();
			refineGeometry();
		}
		calculateNormals();
	}

	/**
	 * Recalculates the normal of the mesh (in its vertices).
	 * Assumes that the vertices of each face are given CCW.
	 */
	public void calculateNormals() {
		// Create an list of empty vector as the new normals
		normals = new ArrayList<Vec>(vertices.size());
		for (int i = 0; i < vertices.size(); ++i) {
			normals.add(new Vec(0, 0, 0));
		}
		// For each face, add its normal to all its vertices
		for (int[] face : faces) {
			// Calculate the face's normal
			Vec faceVec1 = Vec.sub(vertices.get(face[0]), vertices.get(face[1]));
			Vec faceVec2 = Vec.sub(vertices.get(face[2]), vertices.get(face[1]));
			Vec faceNoraml = Vec.crossProd(faceVec2, faceVec1);
			// Add it to the total normals of the each vertex
			for (int vertexIndex : face) {
				normals.get(vertexIndex).add(faceNoraml);
			}
		}
		// Normalize the result vectors
		for (Vec normal : normals) {
			normal.normalize();
		}
	}

	/**
	 * Refines the topology of the mesh by adding more vertices on each edge in
	 * the average location.
	 */
	private void refineTopology() {
		List<int[]> newfaces = new ArrayList<int[]>();
		for (int[] faceVertices : faces) {
			Vec middleVertex = new Vec(0, 0, 0);
			int newVertices[] = new int[faceVertices.length];
			// Calculate the vertices and the new middle vertex
			for (int i = 0; i < faceVertices.length; ++i) {
				// The new vertex at the middle of the edge
				Vec v1 = vertices.get(faceVertices[i]);
				Vec v2 = vertices.get(faceVertices[(i + 1) % faceVertices.length]);
				Vec newV = Vec.scale(0.5, new Vec(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z));
				newVertices[i] = vertices.indexOf(newV);
				if (newVertices[i] == -1) {
					vertices.add(newV);
					newVertices[i] = vertices.size() - 1;
				}
				// Sum for the middle vertex
				middleVertex.x += v2.x;
				middleVertex.y += v2.y;
				middleVertex.z += v2.z;
			}
			// Add the middle vertex to the list of vertices
			middleVertex.scale(1.0 / faceVertices.length);
			vertices.add(middleVertex);
			int middleVertexIndex = vertices.size() - 1;
			// Create the new (sub) faces
			for (int i = 0; i < faceVertices.length; ++i) {
				int[] newFace = {faceVertices[i], 
												 newVertices[i], 
												 middleVertexIndex, 
												 newVertices[(i + newVertices.length- 1) % newVertices.length]};
				newfaces.add(newFace);
			}
		}
		// Replace the old faces with the new ones
		faces = newfaces;
	}
	
	/**
	 * Refines the geometry of the mesh by moving the vertices according to their
	 * normals' environment. 
	 */
	private void refineGeometry() {
		Vec[] newVertices = new Vec[vertices.size()];
		int[] counts = new int[vertices.size()];
		for (int[] face : faces) {
			// Find the face's center point
			Vec faceCenter = new Vec(vertices.get(face[0]));
			for (int i = 1; i < face.length; ++i) {
				faceCenter.add(vertices.get(face[i]));
			}
			faceCenter.scale(1.0 / face.length);
			// "Add" the center point to each of the face's vertices
			for (int i = 0; i < face.length; ++i) {
				if (newVertices[face[i]] == null) {
					newVertices[face[i]] = new Vec(0, 0, 0);
				}
				newVertices[face[i]].add(faceCenter);
				counts[face[i]]++;
			}
		}
		// Calculate final vertices values
		for (int i = 0; i < newVertices.length; ++i) {
			newVertices[i].scale(1.0 / counts[i]);
		}
		// Replace the old vertices with the new ones
		vertices.clear();
		for (Vec vertex : newVertices) {
			vertices.add(vertex);
		}
	}
	
	/**
	 * Drawing the mesh in the given GL instance's environment.
	 * @param gl The GL instance to draw the mesh in.
	 */
	public void draw(GL gl) {
		for (int[] face : faces) {
    	gl.glBegin(GL.GL_POLYGON);
			for (int vertexIndex : face) {
				Vec vertex = vertices.get(vertexIndex);
				Vec normal = normals.get(vertexIndex);
				gl.glNormal3d(normal.x, normal.y, normal.z);
				gl.glVertex3d(vertex.x, vertex.y, vertex.z);
			}
			gl.glEnd();
		}
	}
	
}
