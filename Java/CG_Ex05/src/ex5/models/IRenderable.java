/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5.models;

import javax.media.opengl.GL;

/**
 * A renderable model
 */
public interface IRenderable {

	/** Control option: Toggle light sphere */
	public static final int TOGGLE_LIGHT_SPHERES = 0;
	/** Control option: subdivide current model */
	public static final int SUBDIVIDE = 1;
	/** Control option: Resets current model */
	public static final int RESET_MODEL = 2;

	/**
	 * Render the model
	 * 
	 * @param gl
	 *            GL context
	 */
	public void render(GL gl);
	
	/**
	 * Render the model
	 * 
	 * @param  type
	 *            which setting to change
	 * @param params
	 * 			  Optional parameters needed to control the setting
	 */
	public void control(int type, Object params);
}
