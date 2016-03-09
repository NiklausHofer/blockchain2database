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
	
		Class.forName(driver);
		connection = DriverManager.getConnection(url,user,password);
		
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public Statement getStatement(){
			
		Statement statement = null;
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return statement;

	}
}
