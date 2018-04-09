package ca.nexcel.awbc.processor.publisher;

import java.util.List;
import java.util.Map;

import ca.nexcel.awbc.processor.model.Metric;


/**
 * 
 * Publish data. The implementation class of this interface
 * may publish to the console, a blockchain, or theoretically
 * anything.
 * 
 * @author George Franciscus
 */
public interface Publisher {
	
	/**
	 * Performs any initializing activities. Must be called before publish.
	 */
	public void initialize();
	
	/**
	 * Publish data
	 * 
	 * @param namespace a classification of the key-value pair
	 * @param key the publication key. See implementation class for guidance on uniqueness.
	 * @param value The data to be published
	 * 
	 * @return true for success and false for failure
	 */
	public boolean publish (String namespace, List<String> keys, String value);
	
	/**
	 * Performs any finalizing activities.
	 */
	public void finalize();
	
	/**
	 * Obtains publishing metrics
	 * 
	 * @return publishing metrics
	 */
	public Map<String, Metric> getMetrics();

}
