package ch.bfh.blk2.bitcoin.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class TimeTransactionCountQuerie implements Query<Long>{

	private static final Logger logger = LogManager.getLogger("TimeTransactionCountQuerie");
	
	private static final String SQL=
			"SELECT COUNT(tx_id) AS count FROM transaction WHERE"
			+ " blk_time > ?"
			+ " AND blk_time < ?";
	
	private String start,end;
	private long result=-1;
	
	/**
	 * Get the number of Transactions between two dates
	 * 
	 */
	public TimeTransactionCountQuerie(String start,String end) {		
		this.start=start;
		this.end=end;
	}
	
	@Override
	public void exequte(DatabaseConnection connection) {
		
		try{
		PreparedStatement statement = connection.getPreparedStatement(SQL);
		statement.setString(1, this.start);
		statement.setString(2, this.end);
		
		ResultSet resultSet = statement.executeQuery();
		
		if(resultSet.next())
			this.result = resultSet.getInt("count");
					
		} catch (SQLException e) {	
			e.printStackTrace();
			logger.fatal("Failed to exequte Query");
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		
	}

	@Override
	public Long getResult() {
		return this.result;
	}

	public String resultToString(){
		
		if(this.result<1)
			return "no Transactions between "+start+" and "+end;
		else
			return "found "+this.result+" transactions between "+start+" and "+end;
	}


}
