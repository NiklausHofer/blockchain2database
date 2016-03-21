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
		
		Query<Long> query = new TimeTransactionCountQuerie(start, end);
		query.exequte(connection);
		logger.info(query.resultToString());
	}

}
