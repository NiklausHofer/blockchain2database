package ch.bfh.blk2.bitcoin.util;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

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
