package ca.nexcel.awbc.processor.publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import ca.nexcel.awbc.processor.common.JSONUtil;
import ca.nexcel.awbc.processor.common.JsonRpcClient;
import ca.nexcel.awbc.processor.common.Utils;
import ca.nexcel.awbc.processor.model.Metric;

/**
 * An implementation of Publisher that write data to the 
 * MultiChain Blockchain stream
 * @author George Franciscus
 *
 */
public class PublisherMultiChain implements Publisher {
	/**
	 * The default number of transactions to package in a block. The 
	 * value 10 was chosen because that is the default value configured
	 * in the multichain params.dat file.
	 */
	private static final int DEFAULT_MAX_STD_OP_RETURNS_COUNT = 10;
	
	/**
	 * Multichain command to obtain streams
	 */
	private static final String LISTSTREAMS = "liststreams";


	private static final Logger LOGGER = Logger.getLogger(PublisherMultiChain.class.getName());
	
	
	/**
	 * Publishing metrics
	 */
	private Map<String, Metric> metrics = new HashMap<String, Metric>(); 
	
	/**
	 * The queue used to store transactions until they are used to write to a block
	 */
	private List<Triple> queue = new ArrayList<Triple>();
	
	/**
	 * The address used to write the raw transaction to the chain
	 */
	private String fromAddress;
	
	/**
	 * The value of the multichain property maxStdOpReturnsCount. Used
	 * to specify how many transactions participate in a raw transaction. 
	 */
	private int maxStdOpReturnsCount = DEFAULT_MAX_STD_OP_RETURNS_COUNT;
	
	
	/**
	 * A list of a streams (except root) available in the chain
	 */
	private List<String> streams = new ArrayList<String>();
	
	
	/**
	 * Sets the multichain maxStdOpReturnsCount property 
	 * 
	 * @param count the multichain maxStdOpReturnsCount property 
	 */
	public void setMaxStdOpReturnsCount(String count) {
		if ((null == count) || count.trim().equals("")) {
			LOGGER.warning("No max_std_op_returns_count property defined. Defaulted to " + DEFAULT_MAX_STD_OP_RETURNS_COUNT);
			maxStdOpReturnsCount = DEFAULT_MAX_STD_OP_RETURNS_COUNT;
			return; 
		} 
		
		maxStdOpReturnsCount = Utils.stringToIntegerConverter(count, DEFAULT_MAX_STD_OP_RETURNS_COUNT
				, "invalid max_std_op_returns_count property defined. Defaulted to " + DEFAULT_MAX_STD_OP_RETURNS_COUNT);
	}

	/**
	 * Sets the address used to write the raw transaction to the chain
	 * 
	 * @param fromAddress the address used to write the raw transaction to the chain 
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}


	/**
	 * JSON RPC client
	 */
	private JsonRpcClient jsonRpcClient;
	
	/**
	 * Stores an instance of the RPC Client
	 * 
	 * @param jsonRpcClient
	 */
	public void setJsonRpcClient(JsonRpcClient jsonRpcClient) {
		this.jsonRpcClient = jsonRpcClient;
	}

	public void initialize() {
		metrics = new HashMap<String, Metric>();
		streams = obtainStreams();
	}
	
	/**
	 * Publishes a value to a Multichain stream. 
	 * This method collects and stores publication 
	 * requests in a queue. When the queue is reaches 
	 * the limit, it writes all of the publication
	 * requests at once. This allows several publication 
	 * requests to be bundled in a single block as a means
	 * to reduce the the size of the chain.
	 * 
	 * 
	 * @param stream the blockchain stream name
	 * @param keys the keys used to index the value in the chain.
	 * @param value the value to be written to the chain
	 */
	public boolean publish(String stream, List<String> keys, String value) {
		if ( ! streams.contains(stream)) {
			LOGGER.severe("Record discarded. Stream " + stream + " does not exist in the chain. keys="+ keys.toString() + " value="+value);
			return false;
		}
		
		
		
		//publish when the queue is full
		if (queue.size() >= maxStdOpReturnsCount) {
			flush();
			queue = new ArrayList<Triple>(); //clear the queue
		}
		
		String rawText = Utils.getValueFromJSON("raw_text", value);
		String escapedRawText = JSONUtil.escape(rawText);
		
		queue.add(new Triple(stream, keys, escapedRawText));
		return true;
	}
	
	public void finalize() {
		/* 
		 * The queue may contain data, so flush
		 * out anything not yet published
		 */
		flush();
	}
	
	/**
	 * Writes out all transactions stored in the queue 
	 */
	private void flush() {
		if (queue.size() == 0) {
			return;
		}
		
		List transactions = new ArrayList();
		Iterator<Triple> queueIterator = queue.iterator();
		while (queueIterator.hasNext()) {
			transactions.add(createTransaction(queueIterator.next()));
		}	
		
		boolean flushSuccessful = false;
		try {
			jsonRpcClient.invokeRaw(fromAddress, transactions);
			flushSuccessful = true;
		} catch (Exception ex) {
			LOGGER.severe("Unable to execute Multichain blockchain command"
					+ " fromAddress=" + fromAddress
					+ " transactions=" + transactions.toString());
		}
		
		/* Since all transactions are written to the same block
		 * They will either succeed or fail together
		 */
		
		int successCount = 0;
		int failureCount = 0;
		if (flushSuccessful) {
			successCount = 1;
			failureCount = 0;
		} else {
			successCount = 0;
			failureCount = 1;
		}
		
		queueIterator = queue.iterator();
		while (queueIterator.hasNext()) {
			Triple item = queueIterator.next();
			Utils.addToMap(metrics, item.getStream(), successCount, failureCount, 1);
		}
	}
	
	/**
	 * Creates a transaction from an item
	 * 
	 * @param item contains data to create the transaction
	 * @return A transaction to be written out
	 */
	private Map createTransaction(Triple item) {
    	Map transaction = new LinkedHashMap<>();
    	transaction.put("for", item.getStream());
    	transaction.put("keys",item.getKeys());
    	
    	Map data = new HashMap();
    	data.put("text", item.getValue());
    	transaction.put("data",data);
        	
        return transaction;
	}
	
	
	/**
	 * A convenience class used to group
	 * a stream, a list of keys and a value.
	 * 
	 * @author George Franciscus
	 *
	 */
	private class Triple {
		private String stream;
		private List<String> keys;
		private String value;
		
		
		public Triple (String stream, List<String> keys, String value) {
			this.stream = stream;
			this.keys = keys;
			this.value = value;
		}
		
		public String getStream() {
			return stream;
		}


		public List<String> getKeys() {
			return keys;
		}


		public String getValue() {
			return value;
		}
	}


	@Override
	public Map<String, Metric> getMetrics() {
		return metrics;
	}
	
	
	/**
	 * Obtains a list of all streams
	 * 
	 * @return
	 */
	private List<String> obtainStreams() {
		Object[] obs = new Object[]{};
		String returnValue = jsonRpcClient.invoke(LISTSTREAMS, obs);
		List<String> streamNames = Utils.extractTextFromJSon(returnValue, "name");
		streamNames.remove("root"); //remove default stream
		return streamNames;
	}


}
