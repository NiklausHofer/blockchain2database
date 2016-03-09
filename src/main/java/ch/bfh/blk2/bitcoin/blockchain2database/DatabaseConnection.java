package ch.bfh.blk2.bitcoin.blockchain2database;

import java.io.FileInputStream;
import java.util.Properties;
import java.sql.*;

public class DatabaseConnection {


	private static final String PROPERTIES_FILE="src/resources/db.properties",
			DRIVER = "dbdriver",
			URL = "dburl",
			USER = "user",
			PASSWORD = "password";

	private String driver,url,user,password;

	private Properties properties = new Properties();

	private Connection connection;

	public DatabaseConnection(){

		try{
			properties.load(new FileInputStream(PROPERTIES_FILE));

			driver = properties.getProperty(DRIVER);
			url = properties.getProperty(URL);
			user = properties.getProperty(USER);
			password = properties.getProperty(PASSWORD);
			
			connect();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public PreparedStatement getPreparedStatement(String sql){
		
		PreparedStatement preparedStatement = null;
		
		try {
			if(!connection.isValid(0)){
				connection.close();
				connect();
			}
			
			preparedStatement = connection.prepareStatement(
					sql,Statement.RETURN_GENERATED_KEYS);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return  preparedStatement;
	}

	private void connect(){
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url,user,password);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeConnection(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void finalize( ) throws Throwable   
	{
        try{
        	connection.close();        	
        }catch(Throwable t){
            throw t;
        }finally{
            super.finalize();
        }
	}
}
