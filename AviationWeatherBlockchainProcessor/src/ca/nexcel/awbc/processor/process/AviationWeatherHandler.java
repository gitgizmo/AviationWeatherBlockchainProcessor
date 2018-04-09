package ca.nexcel.awbc.processor.process;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.nexcel.awbc.processor.common.Utils;
import ca.nexcel.awbc.processor.publisher.KeyCreator;
import ca.nexcel.awbc.processor.publisher.Publisher;

/*
 * This SAX handler is responsible for parsing XML, capturing data of interest
 * and outputting the data. The output data consists of a key-value pair and a namespace
 * intended to classify the key-value key pair.
 * 
 * The Publisher object is responsible for output. The implementation of the Publisher
 * is created external to this class and injected as a property prior to parsing.
 * 
 * This SAX handler may optionally receive a KeyCreator responsible for creating the
 * key for the output record. If The KeyCreator is not injected into this class, the
 * value of the key property is employed by default.
 * 
 * The summary of the processing logic is as follows
 * 1. Set class properties externally
 * 2. Invoke parsing externally
 * 3. For each tag identified as "root", extract the list of XML entities
 *    injected as a property, and transform them to JSON format.
 * 4. Once all of the entities for a "root" is harvested, create a key
 *    and publish the record. 
 *    
 * @author George Franciscus    
 */

public class AviationWeatherHandler extends DefaultHandler {
	
   private static final Logger LOGGER = Logger.getLogger( AviationWeatherHandler.class.getName());

   //Handler property: The name of the XML element that embodies the data elements
   private  String rootElement = "";
   
   //Handler property: The name of the XML element that contains the key to the output
   private  String key = "";
   
   //Handler property: A means of further classifying the key-value pair
   private  String namespace = "";
   
   //Handler property: The object responsible for outputting the data captured
   private Publisher publisher;
   
   //Handler property: The object responsible for manufacturing a key for publication
   private KeyCreator keyCreator = null;
   
   //The list of fields of interest
   private  List<String> fieldList = null;
   
   //Determine if a field is to be captured for output
   private boolean isElementValueCaptured = false;
   
   //is this the first output for a record
   private boolean isFirstRecordEntityCaptured = true;	
   
   //The XML data in JSON format
   private String recordJSON = "";
   
   //The name of the field being processed
   private String fieldName = "";
   
   //The number of successful publications in the parsing of the file
   private int numberOfSuccessfulPublications = 0;
   
   //The number of successful publications in the parsing of the file
   private int numberOfFailedPublications = 0;
   
   //The number of attempted publications in the parsing of the file
   private int numberOfAttemptedPublications = 0;

   //Sets the key creator
   public void setKeyCreator(KeyCreator keyCreator) {
	this.keyCreator = keyCreator;
   }

   //Sets the namespace
   public void setNamespace(String namespace) {
	this.namespace = namespace;
   }

   //Sets the value of the key
   public void setKey(String key) {
	   this.key = key;
   }

   //Sets the implementation of the publisher
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	//Gets the implementation of the publisher
	public Publisher getPublisher() {
			return publisher;
	}

	//Sets the name of the XML element wrapping the data of interest
	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}
	
	//Sets the name of the XML elements to be captured
	public void setAllFields(String allFields) {
		fieldList = Utils.buildListFromString(allFields);
	}

	//Gets the number of success publications performed in the parse
	public int getNumberOfSuccessfulPublications() {
		return numberOfSuccessfulPublications;
	}

	//Gets the number of failed publications performed in the parse
	public int getNumberOfFailedPublications() {
		return numberOfFailedPublications;
	}

	public int getNumberOfAttemptedPublications() {
		return numberOfAttemptedPublications;
	}

	//Gets the number of attempted publications performed in the parse
	public void startDocument() throws SAXException {
		numberOfSuccessfulPublications = 0;
		numberOfFailedPublications = 0;
		numberOfAttemptedPublications = 0;
    }

	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		if (qName.trim().equalsIgnoreCase(rootElement.trim())) {
			recordJSON = "{";
		}
		
		isElementValueCaptured = false;	
		
		if (fieldList.contains(qName)) {
			fieldName = qName;
			isElementValueCaptured = true;
		}
	}
	

	public void endElement(String uri, String localName, String qName) throws SAXException {
		List<String> keys = new ArrayList<String>();
		
		if (qName.trim().equalsIgnoreCase(rootElement.trim())) {
			recordJSON += "}";
			isFirstRecordEntityCaptured = true;
			
			if (null == keyCreator) {
				keys.add(Utils.getValueFromJSON(key, recordJSON));
			} else {
				keys.addAll(keyCreator.createKey(recordJSON));
			}
		
			numberOfAttemptedPublications++;
			if (publisher.publish(namespace, keys, recordJSON)) {
				numberOfSuccessfulPublications++;
			} else {
				numberOfFailedPublications++;
			}
		}
	}
	

	public void characters(char ch[], int start, int length) throws SAXException {
		
		if (isElementValueCaptured) {
			if (! isFirstRecordEntityCaptured) {
				recordJSON += ",";
			}

			isFirstRecordEntityCaptured = false;
			recordJSON += "\"" + fieldName + "\":\"" + new String(ch, start, length) + "\"";
		}
		
		isElementValueCaptured = false;

	}

}

