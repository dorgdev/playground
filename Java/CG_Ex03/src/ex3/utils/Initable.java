package ex3.utils;

import java.util.Map;

/**
 * An interface for all types that can be initialized from a map of values.
 * @author dor
 */
public interface Initable {

	/**
	 * Initializes the object from a given map of values
	 * @param attributes Initialization values
	 */
	public void init(Map<String, String> attributes);
}
