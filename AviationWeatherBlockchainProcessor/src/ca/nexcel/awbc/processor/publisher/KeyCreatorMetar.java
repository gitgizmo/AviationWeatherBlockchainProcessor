package ca.nexcel.awbc.processor.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ca.nexcel.awbc.processor.common.Utils;


/**
 * The key creator used for Metars. This key creator
 * keys on station identifier and observation date (CCYYMMDD)
 * 
 * @author George Franciscus
 *
 */

public class KeyCreatorMetar implements KeyCreator {
	
	private static final Logger LOGGER = Logger.getLogger( KeyCreatorMetar.class.getName());
	
	
	/* (non-Javadoc)
	 * @see KeyCreator#createKey(java.lang.String)
	 */
	public List <String> createKey(String json) {
		List <String> keys = new ArrayList<String>();
		
		keys.add(Utils.getValueFromJSON("station_id", json).trim());
		
		String observationDate = Utils.getValueFromJSON("observation_time", json);
		if (observationDate.trim().length() >= 10) {
			keys.add(observationDate.trim().substring(0, 10).replaceAll("-", ""));
		}
		
		return keys;
	}
}
