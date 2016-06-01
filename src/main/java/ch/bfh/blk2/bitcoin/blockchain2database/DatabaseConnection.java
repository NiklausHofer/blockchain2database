package ch.bfh.blk2.bitcoin.blockchain2database;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.util.PropertiesLoader;

/**
 * This class handles some of the tedious work when talking to the database. It opens and manages the connection and it provides easy
 * handles for creating new stored procedures. As long as you pass it from object to object rather than creating new ones, you can be sure
 * that only one connection to the database is opened at a time.
 * 
 * @author stefan
 */
public class DatabaseConnection {
	
	private static final Logger logger = LogManager.getLogger("DatabaseConnection");


	private static final String  DRIVER = "dbdriver", URL = "dburl",
			USER = "user", PASSWORD = "password";

	private String driver, url, user, password;

	private Connection connection;
	
	public DatabaseConnection(){
		this("db.properties");
	}

	/**
	 * Unit tests might want to force the propertiesLoader to load in a non standard configuration file
	 * 
	 * @param propertiesFile An additional file of properties to be loaded. Will probably contain database configuration
	 */
	public DatabaseConnection(String propertiesFile) {
		PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
		propertiesLoader.loadFromFile(propertiesFile);

		driver = propertiesLoader.getProperty(DRIVER);
		url = propertiesLoader.getProperty(URL);
		user = propertiesLoader.getProperty(USER);
		password = propertiesLoader.getProperty(PASSWORD);
		logger.info("user: " + user + "\tpassword: " + password + "\tdriver: " + driver + "\turl: " + url);

		connect();
	}

	/**
	 * Returns a new PreparedStatement from the String you provided. It is your responsibility to close the statment once you're done with it.
	 * 
	 * @param sql A String of the SQL statement you want to turn into a prepared statement
	 * @return A PreparedStatement created from the provided String sql
	 */
	public PreparedStatement getPreparedStatement(String sql) {

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException | NullPointerException e) {
			e.printStackTrace();
		}

		return preparedStatement;
	}

	private void connect() {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * By default, DatabaseConnection does not commit after statements have been executed.
	 * With this function, you force DatabaseConnection to execute a commit.
	 */
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes the database connection.
	 */
	public void closeConnection() {
		try {
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			connection.close();
		} catch (Throwable t) {
			throw t;
		} finally {
			super.finalize();
		}
	}
}
