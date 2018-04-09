package ca.nexcel.awbc.processor.process;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import ca.nexcel.awbc.processor.common.JsonRpcClient;
import ca.nexcel.awbc.processor.common.Utils;
import ca.nexcel.awbc.processor.model.Metric;
import ca.nexcel.awbc.processor.publisher.KeyCreator;
import ca.nexcel.awbc.processor.publisher.KeyCreatorMetar;
import ca.nexcel.awbc.processor.publisher.PublisherMultiChain;
import ca.nexcel.awbc.processor.publisher.PublisherPrint;

/**
 * Processes weather input by reading from an XML source and publishes
 * to a data store. This class is the only class invoked, and done so from
 * the main method.
 * <p/>
 * Although the current class only implements metars, the design of this
 * class plans for the addition of other weather data.
 * <p/>
 * This class expects that the name and path to a config path be provided
 * as a argument to the main method. See ARG_NAME_CONFIG_PROPERTY_FILE.
 *  
 * @author George Franciscus
 *
 */
public class AviationWeatherProcessor {
	private static final String ARG_NAME_CONFIG_PROPERTY_FILE = "-config=";
	private static final String CONFIG_WEATHER_KEY_PREFIX_DEFAULT = "metar";

	private static final Logger LOGGER = Logger.getLogger(AviationWeatherProcessor.class.getName());
	private Properties configProperties = null;
       
	   /**
	    * Aviation processor. This method is called by the runnable jar file. This class expects only
	    * the location of the config file as a mandatory parameter.
	    * 
	    * -config=/somepath/myconfig.properties
	    * 
	    * @param argv location of the config file as a mandatory parameter.
	    */
		public static void main (String argv []) {
	    	AviationWeatherProcessor aviationWeatherProcessor = new AviationWeatherProcessor();
	    	aviationWeatherProcessor.process(argv);
	    }
       
     /**
     * The main processing method
     * 
     * @param argv parameters passed at runtime
     */
    public void process(String argv []) {
    	   
    	   //obtain properties from argument 
    	   String config_file_system_file_name = "";
    	   for (int i=0; i < argv.length; i++) {
    		   if (argv[i].startsWith(ARG_NAME_CONFIG_PROPERTY_FILE)) {
    			   config_file_system_file_name = argv[i].substring(ARG_NAME_CONFIG_PROPERTY_FILE.length());
    		   }
      	   }
    	   
    	   if (config_file_system_file_name.trim().equals("")) {
    		   System.out.println("** FAIL TO START!!! *** " + ARG_NAME_CONFIG_PROPERTY_FILE + " runtime parameter is missing");
    		   System.out.println("Please run your application with using the location of your config file. For example " + ARG_NAME_CONFIG_PROPERTY_FILE + "\\path\\to\\myconfig.properties. See readme file.");
    		   return;
    	   }
  
    	   //obtain the configuraiton properties file
    	   configProperties = Utils.getConfigProperfiesFile(config_file_system_file_name);
    	   if (null == configProperties) {
    		  String errorMessage = "Unable to load property file from the file system. " + ARG_NAME_CONFIG_PROPERTY_FILE + config_file_system_file_name;
    	      LOGGER.severe(errorMessage);
    	     return; //hard stop
    	   }
    	   
    	   //configure loggers
           configLogger(configProperties.getProperty("logging.file"));
            
	       LOGGER.info("aviation weather processor started");
           metar(CONFIG_WEATHER_KEY_PREFIX_DEFAULT);
           LOGGER.info("aviation weather processor completed!");
           LOGGER.info("-------------------------------------"); 
       }

	/**
	 * Configures the logger. The location and name of the logging properties file is set
	 * using the logging.file property in the config.properties file. If the 
	 * logging properties file does not exist, then this method defaults to the
	 * logging.properties file found in the classpath.
	 */
	private void configLogger(String absolutePathToLoggingFile) {
		InputStream inputStream = null;
		
		try {
			absolutePathToLoggingFile = ((null == absolutePathToLoggingFile) || (absolutePathToLoggingFile.trim().equals(""))) ? "" : absolutePathToLoggingFile;
			inputStream = new FileInputStream(absolutePathToLoggingFile);
		    LogManager.getLogManager().readConfiguration(inputStream);
		}
		catch (IOException e1) {
			inputStream = AviationWeatherProcessor.class.getResourceAsStream("/awbcProcessorLogging.properties");
			try {
			    LogManager.getLogManager().readConfiguration(inputStream);
			}
			catch (IOException e2) {
			    Logger.getAnonymousLogger().severe("Could not load default awbcProcessorLogging.properties file from classpath");
			}
		}
	}

		/**
		 * Process metar data source
		 * 
		 * @param config_weather_key_prefix the key used to obtain property configs specific to metars
		 * 
		 */
		private void metar(String config_weather_key_prefix) {
			
			try {
				LOGGER.info("metar processing started");
				
				//Obtain the list of countries that metars will be processed
	            List countries = Utils.buildListFromString(configProperties.getProperty(config_weather_key_prefix + ".countries"));

	            //Configure the Json RPC client
	            JsonRpcClient jsonRpcClient = new JsonRpcClient(configProperties.getProperty("multichain.url")
	            		, configProperties.getProperty("multichain.username")
	            		, configProperties.getProperty("multichain.password"));
	            

	            //Create and configure a publisher
	            PublisherMultiChain publisherMultiChain = new PublisherMultiChain();
	            publisherMultiChain.setJsonRpcClient(jsonRpcClient);
	            publisherMultiChain.setFromAddress(configProperties.getProperty("multichain.fromAddress"));
	            publisherMultiChain.setMaxStdOpReturnsCount(configProperties.getProperty("multichain.max-std-op-returns-count"));
	            publisherMultiChain.initialize();
	            
	            //Create a key creator to be used by the publisher to create keys to index metars
	            KeyCreator metarKeyCreator = new KeyCreatorMetar();
  	            
	            LOGGER.info("number of countries to be processed is " + countries.size());
	            
	            //Process each country
	        	Iterator countryIterator = countries.iterator();
	        	while (countryIterator.hasNext()) {
	        		String country = (String) countryIterator.next();
	        		
	        		//The data source is XML. Create an XML parser. A SAX parser was chosen for its speed and efficiency
			        SAXParserFactory factory = SAXParserFactory.newInstance();
		            SAXParser      saxParser = factory.newSAXParser();
		            AviationWeatherHandler handler   = new AviationWeatherHandler();
 
		            //Configure the XML Parser
		            handler.setRootElement(configProperties.getProperty(config_weather_key_prefix+ ".root"));
		            handler.setAllFields(configProperties.getProperty(config_weather_key_prefix + ".fields"));
		            handler.setKey(configProperties.getProperty(config_weather_key_prefix + ".key"));
		            handler.setKeyCreator(metarKeyCreator);
		            handler.setPublisher(publisherMultiChain);
		            handler.setNamespace(country);

		            //Set up parsing parameters and parse XML
		            Object[] objects = {country};
		            MessageFormat form = new MessageFormat(configProperties.getProperty(config_weather_key_prefix + ".url"));
		            String url = form.format(objects);
		            URL sourceUrl = new URL(url);
		            saxParser.parse(new InputSource(sourceUrl.openStream()), handler);
		            LOGGER.info("Processed country:" + country);

	        	}  
	        	
	        	//Must be called to clean up.
	        	publisherMultiChain.finalize();
	        	
	        	//Present publication statistics in the log
	        	logMetrics(publisherMultiChain.getMetrics());

	        } catch (Throwable err) {
	        	LOGGER.severe("unable to fully process all metars in all countries");
	        	err.printStackTrace();
	            new RuntimeException("unable to process METAR");
	        }
			
			LOGGER.info("metar processing completed");
		}
		
		/**
		 * Log statistics
		 * 
		 * @param metrics publication statistics
		 */
		private void logMetrics(Map <String, Metric> metrics) {
			
            int totalAttemptCount = 0;
            int totalSuccessCount = 0;
            int totalFailureCount = 0;
			
			Iterator<String> metricsIterator = metrics.keySet().iterator();
			while (metricsIterator.hasNext()) {
				String key = metricsIterator.next();
				Metric metric = metrics.get(key);
				
				totalAttemptCount += metric.getAttemptCount();
				totalSuccessCount += metric.getSuccessCount();
				totalFailureCount += metric.getFailureCount();
				
	            String failFlag = (metric.getFailureCount() > 0) ? "  *" : "   ";
	            LOGGER.info(failFlag + "country " + key + " publication metrics."
	            		+ " attempted="  + metric.getAttemptCount()
	            		+ " successful=" + metric.getSuccessCount()
	            		+ " failed="     + metric.getFailureCount());
				
			}
			
			int successPercentage = (0 == totalAttemptCount) ? 0 :  ((totalSuccessCount *100) / totalAttemptCount);
            LOGGER.info("total processing metrics: " 
            		+ " success percentage="     + successPercentage + "%"
            		+ " total attempted="  + totalAttemptCount
            		+ " successful=" + totalSuccessCount
            		+ " failed="     + totalFailureCount);
			
			
		}
}
