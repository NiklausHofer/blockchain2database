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
	 * For use in tests. Use the default constructor for production code
	 * 
	 * @param propertiesFile
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

	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
