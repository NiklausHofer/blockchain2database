package ch.bfh.blk2.bitcoin.blockchain2database;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBHelper {
	
	private static final Logger logger = LogManager.getLogger("QueryTool");
	
	public static ResultSet runQuery(String query, DatabaseConnection connection){
		ResultSet rs = null;
		PreparedStatement statement = connection.getPreparedStatement(query);
		try{
			statement.executeQuery();
			rs = statement.getResultSet();
			statement.close();
		} catch (SQLException e){
			logger.fatal("Unable to execute query: " + query, e);
		}
		
		return rs;
	}

}
