package ca.nexcel.awbc.processor.common;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

/**
 * 
 * The JSON RPC Client
 * 
 * @author George Franciscus
 *
 */
public class JsonRpcClient {
	
	private static final Logger LOGGER = Logger.getLogger( JsonRpcClient.class.getName());
	
	
	/**
	 * RPC client
	 */
	private JsonRpcHttpClient client;
	
	/**
	 * JSON RPC Constructor.
	 * 
	 * The Multichain RPC username and password are found in the multichain.conf file
	 * found in the chain's directory. Please see Multichain documentation for instructions
	 * on how to find the multichain.conf file for the desired operating system.
	 * 
	 *  multichain.conf file propertyu values rpcuser and rpcpassword contain
	 *  the username and password.
	 * 
	 * @param url the URL of the JSON RPC server
	 * @param username the JSON RPC server chain username
	 * @param password JSON RPC server chain password
	 */
	public JsonRpcClient(String url, String username, String password) {
		try {
			Map<String, String> headers = new HashMap<String, String>(1);

			final String un = username;
			final String pw = password;

			//Register JSON RPC chain credentials
			Authenticator.setDefault(new Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(un, pw.toCharArray());
				}
			});

			
			client = new JsonRpcHttpClient(new URL(url),headers);
			
		} catch (MalformedURLException e) {
			LOGGER.severe("JSON RPC client creation failed calling URL "+ url
					+ " In addition to JSON RPC server availability, please check username and password");
            new RuntimeException(e);
		}
		
	}
	
	
	/**
	 * Call to JSON RPC Server
	 * 
	 * @param method JSON RPC method name
	 * @param params JSON RPC call parameters
	 * 
	 * @return return value from the call
	 */
	public String invoke(String method,Object [] params) {
		Object returnValue = "";
        try {
        	returnValue = client.invoke(method, params, Object.class);
		} catch (Throwable e) {
			LOGGER.severe("JSON RPC client call failed"
					+ " url=" + client.getServiceUrl()
					+ " method=" + method
            		+ " params="+ params.toString());
			return "";
		}
		
		return returnValue.toString();
	}

	
	/**
	 * Call to JSON RPC Server using createrawsendfrom method
	 * 
	 * @param fromAddress the address that is associated with the call
	 * @param transactions the transactions sent in the Json RPC call
	 */
	public void invokeRaw(String fromAddress, List transactions) {
		String method = "createrawsendfrom";
		
		try {
	
			Map toaddresses =  new HashMap();
            Object[] obs = new Object[]{fromAddress, toaddresses,  transactions, "send"};
            
            Object s = client.invoke(method, obs, Object.class);
            LOGGER.fine("JSON RPC successfully processed"
            		+ " url=" + client.getServiceUrl()
            		+ " fromAddress=" + fromAddress
            		+ " tramsactions="+ transactions.toString()
            		+ " transactionId="+s.toString());

		} catch (Exception e) {
			LOGGER.severe("JSON RPC client call failed #2"
					+ " url=" + client.getServiceUrl()
					+ " fromAddress=" + fromAddress
            		+ " tramsactions="+ transactions.toString());
			e.printStackTrace();
			throw new RuntimeException(e);

		} catch (Throwable e) {
			LOGGER.severe("JSON RPC client call failed #3"
					+ " url=" + client.getServiceUrl()
					+ " fromAddress=" + fromAddress
            		+ " tramsactions="+ transactions.toString());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
