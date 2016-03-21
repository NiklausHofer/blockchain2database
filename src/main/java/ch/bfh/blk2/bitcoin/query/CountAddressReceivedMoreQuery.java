package ch.bfh.blk2.bitcoin.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class CountAddressReceivedMoreQuery implements Query<Long>{

	private static final Logger logger = LogManager.getLogger("CountAddressReceivedMoreQuery");
	
	private static final String SQL=
			"SELECT COUNT(addr_id) AS count FROM"
			+ " (SELECT addr_id,SUM(amount) AS sum_amount"
			+ " FROM output "
			+ " GROUP BY addr_id) AS sum_addr"
			+ " WHERE sum_amount > ?";
	
	private long amount;
	private long result=-1;
	
	/**
	 * Get the number of addresses that received more than a given amount
	 * 
	 */
	public CountAddressReceivedMoreQuery(long amount) {		
		this.amount= amount;
	}
	
	@Override
	public void exequte(DatabaseConnection connection) {
		
		try{
		PreparedStatement statement = connection.getPreparedStatement(SQL);
		statement.setLong(1, this.amount);
		
		ResultSet resultSet = statement.executeQuery();
		
		if(resultSet.next())
			this.result = resultSet.getLong("count");
					
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
			return "no address received mor than "+ this.amount;
		else{
			return "found "+this.result+" addresses that received more than "
					+this.amount+" satoshi";
		}
	}


}
