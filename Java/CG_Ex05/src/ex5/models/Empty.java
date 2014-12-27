/*
 * Computer Graphics - Exercise 05
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */
package ex5.models;

import javax.media.opengl.GL;

/**
 * A simple empty model. 
 */
public class Empty extends AbstractModel {	

	/**
	 * Default CTOR.
	 */
	public Empty() {
		super("Empty");
	}
	
	@Override
	public void render(GL gl) {
		// Nothing to render, extending classes may override this behavior.
	}

}
