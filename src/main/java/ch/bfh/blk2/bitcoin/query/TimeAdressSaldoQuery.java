package ch.bfh.blk2.bitcoin.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class TimeAdressSaldoQuery implements Query<Long> {
	
	private static final Logger logger = LogManager.getLogger("TimeAdressSaldoQuery");

	private static final String SQL=
			" SELECT SUM(output.amount) AS amount"
			+ " FROM block INNER JOIN ("
			+ " transaction INNER JOIN output"
			+ " ON transaction.tx_id = output.tx_id)"
			+ " ON block.blk_id = transaction.blk_id"
			+ " WHERE output.spent_by_tx IS NULL"
			+ " AND block.time < ?"
			+ " AND output.address = ?";
	
	//select transaction.tx_id,output_id,address.addr_id, output.amount from address,output, transaction where blk_time < '2005-06-01' and transaction.tx_id = output.tx_id and address.addr_id = output.addr_id and address.addr_hash = 'foo' and spent = 0;
	
	//select sum(output.amount) as amount from address,output, transaction where blk_time < '2005-06-01' and transaction.tx_id = output.tx_id and address.addr_id = output.addr_id and address.addr_hash = 'foo' and spent = 0;

	
	private long result = -1;
	private String address,date;
	
	/**
	 * get saldo of an address on this date
	 * @param address
	 * @param date
	 */
	public TimeAdressSaldoQuery(String address,String date) {
		this.address=address;
		this.date = date;
	}
	
	
	@Override
	public void exequte(DatabaseConnection connection) {
		
		logger.info("Get saldo of address ["+this.address+"] on "+this.date);
		
		try{
		PreparedStatement statement = connection.getPreparedStatement(SQL);
		statement.setString(1, this.date);
		statement.setString(2, this.address);
		
		ResultSet resultSet = statement.executeQuery();
		
		if(resultSet.next())
			this.result = resultSet.getLong("amount");
					
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

	@Override
	public String resultToString() {

		return "found "+this.result
				+" toshi on address ["+this.address+"] on "+this.date; 
	}
	
	

}
