package ch.bfh.blk2.bitcoin.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.GetAddrMessage;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class SimpleAddressNodeQuerry implements Query<Double>{

	private static final Logger logger = LogManager.getLogger("AdressNodeQuerry");

	private static final String
			GET_TX_AMOUNT="SELECT input_amount,output_amount"
					+ " FROM transaction"
					+ " WHERE tx_id = ?",
			
			GET_ADDR_SPENT_OUTPUT_TX ="SELECT spent_in_tx"
					+ " FROM output"
					+ " WHERE pent = 1"
					+ " AND addr_id = ?",
					
			GET_TX_ADDR_ID = "SELECT addr_id"
					+ " FROM output"
					+ " WHERE tx_id = ?",
					
			GET_ADDR_ID = "SELECT addr_id"
					+ " FROM address"
					+ " WHERE addr_hash = ?"
	;
			
	
	private static final int MAX = 15;
	
	private String addrA,addrB;
	
	private Double result = -1.0;
	
	/**
	 *  Find all paths from A to B
	 *  Depth first search
	 */
	public SimpleAddressNodeQuerry(String addrA,String addrB) {
		this.addrA=addrA;
		this.addrB=addrB;
	}
	
	@Override
	public void exequte(DatabaseConnection connection) {
		
		logger.info("Adress Node calculation");
		
		
		double factor= 0.0;
		long idA=-2,idB=-2;
		
		List<Long> travrsedTransactions = new ArrayList<>();
		
		try{
		
		//get Addr_id of A and B
		PreparedStatement statement = connection.getPreparedStatement(GET_ADDR_ID);
		statement.setString(1, addrA);
		ResultSet result = statement.executeQuery();
		
		if(result.next()){
			idA = result.getLong("addr_id");
		}else{
			logger.fatal("Address not found in database: ["+addrA+"]");
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		result.close();
		statement.close();
		
		statement = connection.getPreparedStatement(GET_ADDR_ID);
		statement.setString(1, addrB);
		result = statement.executeQuery();
		if(result.next()){
			idB = result.getLong("addr_id");
		}else{
			logger.fatal("Address not found in database: ["+addrB+"]");
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		result.close();
		statement.close();
		
		getGraph(idA, idB, connection);
				
		//get spent outputs of A and output amount 
		
		
		
		
		}catch(SQLException e){
			e.printStackTrace();
		}
		
 	}
	
	


	@Override
	public Double getResult() {
		return this.result;
	}

	@Override
	public String resultToString() {
		return null;
	}
	
	// assemble the graph
	private void getGraph(long addrA,long addrB,DatabaseConnection connection){
		
		long currAddrId = addrA;
		int depth=0;
		List<AddressNode> nextNodes = new ArrayList<>();
		
		//add start node
		nextNodes.add(new AddressNode(currAddrId));
		
		while(depth<MAX && !nextNodes.isEmpty()){
			
			AddressNode currNode = nextNodes.get(0);
			long currId = currNode.getAddrId();
			
			
			
			//get all transactions where output of the current Node are spent 
			PreparedStatement statement=connection.getPreparedStatement(GET_ADDR_SPENT_OUTPUT_TX);
			
			
		}
		
	}
	
	// Address Graph
	
	private Map<Long,AddressNode> addressNodes = new HashMap<>();
	
	private class AddressNode{
		
		long addrId;
		Set<TransactionEdge> outgoingTransaction = new HashSet<>();
		Set<TransactionEdge> incommingTransaction = new HashSet<>();
		
		public AddressNode(long addrId){
			this.addrId=addrId;
		}
	
		public void addIncommingTransaction(TransactionEdge txEdge){
			incommingTransaction.add(txEdge);
		}
		
		public void addOutgoingTransaction(TransactionEdge txEdge){
			outgoingTransaction.add(txEdge);
		}
		
		public Long getAddrId(){
			return this.addrId;
		}
	}
	
	private class TransactionEdge{
		
		long txId;
		AddressNode from,to;
		
		public TransactionEdge(long txId, AddressNode from,AddressNode to){
			this.txId=txId;
			this.from=from;
			this.to=to;
			to.addIncommingTransaction(this);
			from.addOutgoingTransaction(this);
		}
	}
}
