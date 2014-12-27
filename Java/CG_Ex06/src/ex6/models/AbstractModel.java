/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

import javax.media.opengl.GL;

/**
 * An abstract model, which allows several common control operations.
 */
public abstract class AbstractModel implements IRenderable {

	/** The no-color parameters for a material setting */
	protected static float[] NO_COLOR = new float[] {0f, 0f, 0f, 1f};
	/** The name of the model */
	private String modelName;

	/**
	 * Creates a new abstract model (as part of a deriving class) with a default
	 * model's name.
	 * @param name The name of the model
	 */
	public AbstractModel(String name) {
		modelName = name;
	}
	
	@Override
	public String toString() {
	  return modelName;
	}
	

	@Override
	public abstract void render(GL gl);
	
	/**
	 * Sets the material properties with the given values. NULL sets the empty
	 * parameters (as no color). 
	 * @param gl The GL instance to use
	 * @param diffuse The diffusion parameters (or null for none)
	 * @param specular The specular parameters (or null for none)
	 * @param shininess The shininess factor of the material
	 * @param emission The emission parameters (or null for none)
	 */
	protected void setMaterialProperties(GL gl, float[] diffuse, float[] specular,
			int shininess, float[] emission) {
		if (diffuse != null) {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
		} else {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, NO_COLOR, 0);
		}
		if (specular != null) {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
		} else {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, NO_COLOR, 0);
		}
		if (emission != null) {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, emission, 0);
		} else {
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, NO_COLOR, 0);
		}
	}
}
