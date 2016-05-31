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
	
	public static PropertiesLoader getInstance(){
		
		if(propertiesLoader == null){
			propertiesLoader = new PropertiesLoader();
		}
		
		return propertiesLoader;
	}	
	
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
