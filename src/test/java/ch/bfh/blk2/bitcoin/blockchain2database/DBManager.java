package ch.bfh.blk2.bitcoin.blockchain2database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

public class DBManager {
	
	private static final Logger logger = LogManager.getLogger("DBManager");
	
	private Flyway flyway;
	
	public DBManager(){
		flyway = new Flyway();
		Properties flywayConfig = new Properties();
		
		// Load Database admin credentials form our flyway configuration file
		try {
			flywayConfig.load(new FileInputStream("flyway.properties"));
		} catch (IOException e) {
			logger.fatal("Unable to load flyway configuration", e);
			System.exit(1);
		}
		flywayConfig.setProperty("flyway.schemas", "unittest");
		flywayConfig.setProperty("flyway.placeholders.unittest_password", "foobar");
		flywayConfig.setProperty("flyway.locations", "filesystem:src/main/resources/db/migration/unittest,filesystem:src/main/resources/db/migration/all");
		flywayConfig.setProperty("flyway.url", "jdbc:mysql://localhost:3306/");
		
		logger.debug("Flyway config used:");
		for( Entry<Object,Object> entry: flywayConfig.entrySet())
			logger.debug("\t" + entry.getKey().toString() + ":\t" + entry.getValue().toString());

		flyway.configure(flywayConfig);
	}
	
	public void initDB(){
		flyway.migrate();
	}
	
	public void resetDB(){
		logger.debug("Cleaning Database");
		flyway.clean();
		logger.debug("Migrating Database");
		flyway.migrate();
	}
	
	public void cleanDB(){
		flyway.clean();
	}

}