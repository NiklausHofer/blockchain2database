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
		
//		String start="2005-01-01",end="2005-03-01";
//		
//		Query<Long> query0 = new TimeTransactionCountQuery(start, end);
//		query0.exequte(connection);
//		logger.info(query0.resultToString());
//		
//		String address="foo",date="2005-06-01"; 
//		
//		Query<Long> query1 = new TimeAdressSaldoQuery(address, date);
//		query1.exequte(connection);
//		logger.info(query1.resultToString());
//		
//		
//		long amount = 20; 
//		
//		Query<Long> query2 = new CountAddressReceivedMoreQuery(amount);
//		query2.exequte(connection);
//		logger.info(query2.resultToString());
//		
//		amount = 11; 
//		
//		Query<Long> query3= new CountAddressHigherSaldoQuery(amount);
//		query3.exequte(connection);
//		logger.info(query3.resultToString());
		
		String addrA="1GYAcAecK23Z2pZ5aZbAsEdj17Jb1Nz7nB",
				addrB="1Yvnzx4t6YPGWVzfMddFtZFBbAqg3isMm";
		
		//3 hops
		
		Query<Integer> query4= new TransactionNodeQuery(addrA, addrB);
		query4.exequte(connection);
		logger.info(query4.resultToString());
	}

}
