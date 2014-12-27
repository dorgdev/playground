/*
 * Computer Graphics - Exercise 06
 * Students' name: Dor Gross, Itamar Benady and Shlomi Babluki
 * Students' ID:   039344999, 300157427 and 021541065     
 */

package ex6.models;

import javax.media.opengl.GL;


/**
 * A renderable model
 */
public interface IRenderable {
	

	/**
	 * Render the model
	 * 
	 * @param gl
	 *            GL context
	 */
	public void render(GL gl);

}
