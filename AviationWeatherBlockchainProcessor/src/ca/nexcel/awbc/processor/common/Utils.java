package ca.nexcel.awbc.processor.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nexcel.awbc.processor.model.Metric;

/**
 * A collection of utility methods.
 * 
 * @author George Franciscus
 *
 */
public class Utils {
	private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
	
	/**
	 * Create a list of tokens from a comma delimited String.
	 * @param text comma delimited String
	 * @return list of tokens
	 */
	static public List buildListFromString(String text) {
		StringTokenizer st = new StringTokenizer(text, ",");
		List tokenList = new ArrayList();
		while (st.hasMoreTokens()) tokenList.add(st.nextToken());
		return tokenList;
	}
	
	
	/**
	 * Obtain a properties file. This method attempts to find the properties file
	 * in a physical location. If not found, it attempts to find the properties
	 * file on the classpath.
	 * 
	 * @param physicalFilenameAndPath physical file name and path to properties file
	 * @return Properties file. A null value means its not found. 
	 */
	static public Properties getConfigProperfiesFile(String physicalFilenameAndPath) {
		 return loadPropertiesFileFromAbsolutePath(physicalFilenameAndPath);
	}
	
	static public Properties loadPropertiesFileFromClasspath(String propertiesFileName) {
		Properties properties = null;
		InputStream input = null;

		try {
			input = Utils.class.getClassLoader().getResourceAsStream(propertiesFileName);
			if (input == null) {
				return null;
			}

			properties = new Properties();
			properties.load(input);

		} catch (IOException ex) {
			properties = null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.out.println("Warning: cannot close input stream after trying to "
							+ "load Properties File from classpath");
				}
			}
		}
		return properties;
	}
	
	/**
	 * Load the properties file usign the absolute path to the file system
	 * 
	 * @param physicalFilenameAndPath the file name and path the the properties file
	 * @return the loaded properties file. A null value means its not found.
	 */
	static public Properties loadPropertiesFileFromAbsolutePath(String physicalFilenameAndPath) {
        Properties properties = null;
        try {
               properties = new Properties();
               properties.load(new FileInputStream(physicalFilenameAndPath));
        } catch (FileNotFoundException e) {
               properties = null;
        } catch (IOException e) {
               properties = null;
        }

        return properties;
	}
	

	/**
	 * Extracts a value for a property in JSON.
	 * @param property the JSON property to be extracted.
	 * @param json Valid JSON
	 * @return property value
	 */
	static public String getValueFromJSON(String property, String json) {
		int startPosition = json.indexOf(property + "\":");
		if (-1 == startPosition) {
			return null;
		}
		startPosition = json.indexOf(":\"", startPosition);

		int endPosition = json.indexOf("\",", startPosition + 2);
		if (endPosition == -1) {
			endPosition = json.indexOf("\"}", startPosition + 2);
		}
		if (-1 == endPosition) {
			return null;
		}
		return json.substring(startPosition + 2, endPosition);

	}
	
	
	/**
	 * Adds metrics to a map of metrics. 
	 * Values are added to an existing metric if one is already associated with the key.
	 * Otherwise, a new metric is created and added to the map.  
	 * 
	 * @param metrics the existing map of metrics. Must not be null.
	 * @param key the key associated with the metrics
	 * @param successCount the number of successes
	 * @param failureCount the number of failures
	 * @param attemptCount the number of attempts. Should equal the failures + successes
	 */
	static public void addToMap(Map<String, Metric> metrics, String key, int successCount, int failureCount, int attemptCount) {
        Metric metric = metrics.get(key);
      if (null == metric) {
    	  metrics.put(key, new Metric(successCount, failureCount, attemptCount));
        } else {
          metric.addToSuccessCount(successCount);
          metric.addToFailureCount(failureCount); 
          metric.addToAttemptCount(attemptCount); 
        }
  }
	
	
    /**
     * Converts an integer in String form to an int.
     * Malformed input will return the defaultResult, and log
     * the default message.
     * 
     * @param input the string to convert to an int
     * @param defaultResult the value to return when unable to convert to int
     * @param defaultMessage the message to logn wehn unable to convert to int
     * @return the convered int
     */
    static public int stringToIntegerConverter(String input, int defaultResult, String defaultMessage) {
        int result = 0;
        try {
               result = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
               result = defaultResult;
               LOGGER.warning(defaultMessage);
        }     

        return result;
  }
    
    
    /**
     * Extract the value from a JSON string as presented
     * by the JSON RPC server.
     * 
     * @param text the text containing JSON key and value
     * @param jsonKey the key associated with the value to be extracted
     * @return the value associated with the key
     */
    public static List<String> extractTextFromJSon(String text, String jsonKey) {
      	List<String> list = new ArrayList<String>();
     	String regex =  jsonKey +  "=(.*?)(\\, | \\} | \\])";
     	text = text.replaceAll("\\{", "");
     	text = text.replaceAll("\\}", "");
     	Pattern pattern = Pattern.compile(regex);
     	Matcher matcher = pattern.matcher(text);
     	while(matcher.find()) {
     	  list.add(matcher.group(1));
    	}
    	
    	return list;
    }

}
