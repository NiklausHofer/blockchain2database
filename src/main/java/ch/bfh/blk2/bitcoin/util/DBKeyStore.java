package ch.bfh.blk2.bitcoin.util;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Class to handle metadata in the database.
 * 
 * We have a key-value-store table in the databae that can hold various inforamation.
 * For now, all it holds is the dirty flag though.
 * 
 * @author niklaus
 */
public class DBKeyStore {
	
	private static final Logger logger = LogManager.getLogger("DBKeyStore");

	private final String
		INSERT = "INSERT INTO parameter"
			+ " (p_key,p_value)"
			+ " VALUES (?, ?)"
			+ " ON DUPLICATE KEY UPDATE p_value = ?",
			
		UPDATE = "UPDATE parameter SET p_value = ? WHERE p_key = ?",
			
		SELECT = "SELECT p_value FROM parameter WHERE p_key = ?"
		;
		
	// default keys
	public static String DYRTY = "DIRTY";
	
	/**
	 * Store a key-value pair into the metadata table 
	 * 
	 * @param connection the database connection to be used
	 * @param key the key or identifier of the value to be stored
	 * @param value the value associated with this key
	 */
	public void setParameter(DatabaseConnection connection,String key,String value){
		
		try{
		PreparedStatement statement = connection.getPreparedStatement(UPDATE);
		
		statement.setString(1, value);
		statement.setString(2, key);
		statement.executeUpdate();
		
		logger.debug("Set parameter in db key store ["+key+" : "+value+"]");
		
		}catch(SQLException e){
			logger.fatal("unable to set parameter in db parameter key store",e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}
	
	/**
	 * Read out a parameter that has previously been written to the metadata table.
	 * 
	 * @param connection The database connection to be used
	 * @param key the key for which you want to know the associated value
	 * @return the value associated with the given key
	 */
	public String getParameter(DatabaseConnection connection,String key){

		try{
			PreparedStatement statement = connection.getPreparedStatement(SELECT);
			statement.setString(1, key);
			ResultSet result = statement.executeQuery();
			
			if(result.next()){
				return result.getString(1);
			}else{
				logger.fatal("key not found in db parameter key store:" + key);
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

		}catch(SQLException e){
			logger.fatal("unable to get parameter from parameter key store",e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		return null;
	}
}
