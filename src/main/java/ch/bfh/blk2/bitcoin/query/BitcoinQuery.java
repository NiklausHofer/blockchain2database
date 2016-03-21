package ch.bfh.blk2.bitcoin.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class BitcoinQuery {

	private static final Logger logger = LogManager.getLogger("BitcoinQuery");
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		DatabaseConnection connection = new DatabaseConnection();
		
		String start="2005-01-01",end="2005-03-01";
		
		Query<Long> query0 = new TimeTransactionCountQuery(start, end);
		query0.exequte(connection);
		logger.info(query0.resultToString());
		
		String address="foo",date="2005-06-01"; 
		
		Query<Long> query1 = new TimeAdressSaldoQuery(address, date);
		query1.exequte(connection);
		logger.info(query1.resultToString());
	}

}
