package ch.bfh.blk2.bitcoin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesLoader {
	
	private static final Logger logger = LogManager.getLogger("PropertiesLoader");
	
	private final static String[] PROPERTIES_FILES = {
		"target/classes/blockchain.properties",
		"target/classes/db.properties"
	};
	
	private static PropertiesLoader propertiesLoader;

	private Map<String,String> properties;
	
	private PropertiesLoader(){
		loadProperties();
	}
	
	private void loadProperties(){
		this.properties = new HashMap<>();
		for(String fileName : PROPERTIES_FILES){
			try {
				Properties prop = new Properties();
				prop.load(new FileInputStream(fileName));
				for(Entry<Object,Object> entry : prop.entrySet()){
					String key = (String) entry.getKey(),
							value = (String) entry.getValue();
	
					this.properties.put(key,value);
					logger.debug(key+" : "+value);
				}
			} catch (IOException e) {
				logger.error("Unable to read Properties from: "+ fileName);
				System.exit(1);
			}
		}
	}
	
	public static PropertiesLoader getInstance(){
		
		if(propertiesLoader == null){
			propertiesLoader = new PropertiesLoader();
		}
		
		return propertiesLoader;
	}	
	
	public String getProperty(String key){
		logger.debug(properties.size());
		if(properties.containsKey(key)){
			return properties.get(key);
		}else{
			logger.error("propertie ["+key+"] not found");
			System.exit(1);
			return "";
		}
	}

}
