package ca.nexcel.awbc.processor.publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ca.nexcel.awbc.processor.common.Utils;
import ca.nexcel.awbc.processor.model.Metric;


/*
 * An implementation of Publisher that simply print out data to
 * the console. Generally used for testing.
 * 
 * @author George Franciscus
 */
public class PublisherPrint implements Publisher {
	private static final Logger LOGGER = Logger.getLogger(PublisherPrint.class.getName());
	
	/**
	 * publishing metrics
	 */
	private Map<String, Metric> metrics = new HashMap<String, Metric>(); 
	
	public void initialize() {
		metrics = new HashMap();
	}

	/* (non-Javadoc)
	 * @see Publisher#publish(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean publish(String namespace, List<String> keys, String value) {
		String raw_text = Utils.getValueFromJSON("raw_text", value);
		Utils.addToMap(metrics, namespace, 1, 0, 1);
		System.out.println("namespace=" + namespace + " key=" + keys.toString() + " value=" + raw_text);
		return true;
	}

	
	public void finalize() {
	}

	@Override
	public Map<String, Metric> getMetrics() {
		return metrics;
	}

}
