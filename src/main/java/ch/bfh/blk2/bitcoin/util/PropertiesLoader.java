package ch.bfh.blk2.bitcoin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class reads and manages the settings for our application. Since we use
 * java properties files to configure the application, this class is mostly handling
 * java properties.
 * 
 * @author stefan
 *
 */
public class PropertiesLoader {
	
	private static final Logger logger = LogManager.getLogger("PropertiesLoader");
	
	private final static String[] PROPERTIES_FILES = {
		"blockchain.properties",
		"db.properties"
	};
	
	private static PropertiesLoader propertiesLoader;

	private Map<String,String> properties;
	
	private PropertiesLoader(){
		loadProperties();
	}
	
	private void loadProperties(){
		this.properties = new HashMap<>();
		for(String fileName : PROPERTIES_FILES){
			loadFromFile(fileName);
		}
	}
	
	/**
	 * Used to load in attributes from properties files. You can call this from another class
	 * if you need any non-default configuration files to be read in. Note that already existing
	 * values will be overwritten by new ones of the same key.
	 * 
	 * @param fileName The filename from which properties are to be read
	 */
	public void loadFromFile(String fileName){
			Properties prop = new Properties();
			if( loadConfigFile(prop, fileName)){
				for(Entry<Object,Object> entry : prop.entrySet()){
					String key = (String) entry.getKey(), value = (String) entry.getValue();
	
					this.properties.put(key,value);
					logger.debug(key+" : "+value);
				}
				logger.debug("loaded "+properties.size()+" properties");
			}else{
				logger.error("Unable to read Properties from: "+ fileName);
				System.exit(1);
			}
	}
	
	private boolean loadConfigFile(Properties prop, String fileName){
		// Look in working directory
		Path localPath = Paths.get("./", fileName);
		File localConfigFile = localPath.toFile();
		if( localConfigFile.exists() ){
			try {
				prop.load(new FileInputStream(localConfigFile));
				return true;
			} catch (IOException e) {
				logger.debug("Config file " + localConfigFile.getAbsolutePath().toString() + " exists, but I was unable to read it", e);
			}
		}
		
		// Look in the system config
		Path systemConfigPath = Paths.get("/etc/blockchain2database/", fileName);
		File systemConfigFile = systemConfigPath.toFile();
		if( systemConfigFile.exists() ){
			try {
				prop.load(new FileInputStream(systemConfigFile));
				return true;
			} catch (IOException e) {
				logger.debug("Config file " + systemConfigFile.getAbsolutePath().toString() + " exists, but I was unable to read it", e);
			}
		}
		
		// Look in the classpath
		try{
			prop.load(this.getClass().getClassLoader().getResourceAsStream(fileName));
			return true;
		} catch (IOException e){
			logger.debug("Unable to find the file in the local classpath", e);
		}
		
		return false;
	}
	
	/**
	 * PropertiesLoader is a singleton. This funciton gives you access to the instance.
	 * 
	 * @return The one instance of PropertiesLoader
	 */
	public static PropertiesLoader getInstance(){
		
		if(propertiesLoader == null){
			propertiesLoader = new PropertiesLoader();
		}
		
		return propertiesLoader;
	}	
	
	/**
	 * Query the value of a property. If the propperty is not known, this will
	 * terminate the application though, so be careful with what you do.
	 * 
	 * @param key the name of the property which's value you want to know
	 * @return the value of that property
	 */
	public String getProperty(String key){
		if(properties.containsKey(key)){
			return properties.get(key);
		}else{
			logger.error("propertie ["+key+"] not found");
			System.exit(1);
			return "";
		}
	}

}
