package ca.nexcel.awbc.processor.publisher;

import java.util.List;

/**
 * 
 * A interface supporting the key creation implementations
 * @author George Franciscus
 *
 */
public interface KeyCreator {
	/**
	 * 
	 * Create a key
	 * @param data data available to create a key
	 * @return a key
	 */
	public List <String>  createKey(String data);
}
