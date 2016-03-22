package ch.bfh.blk2.bitcoin.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.fabric.xmlrpc.base.Array;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class TransactionNodeQuery implements Query<Integer>{

	private static final String
			GET_ADDR_ID="SELECT addr_id FROM address WHERE addr_hash = ?",
			
			GET_ADDR_NEXT_TX =
					"SELECT spent_in_tx"
					+ " FROM output"
					+ " WHERE spent = 1"
					+ " AND addr_id = ?",
			
			GET_NEXT_TX=
				"SELECT spent_in_tx"
				+ " FROM output"
				+ " WHERE spent = 1",
			
			COUNT_ADDR =
				"SELECT COUNT(addr_id) as count"
				+ " FROM output"
				+ " WHERE addr_id = ?",
				
			TX_ID = "tx_id"
			;
	
	private static final int MAX = 6;
			
	private static final Logger logger = LogManager.getLogger("TransactionNodeQuery");
	
	private String addrA,addrB;

	private int result = -1;
	
	public TransactionNodeQuery(String addrA,String addrB){
		this.addrA=addrA;
		this.addrB=addrB;
	}
	
	@Override
	public void exequte(DatabaseConnection connection) {
		
		logger.info("Searching shortest path between adresses ["+addrA+"] and ["+addrB+"]" );
		
		int count=0;
		boolean found= false;

		logger.debug("max search depth : "+ MAX);

		try{
			long idA=-1,idB=-1;

			//Get id of addesses A and B
			PreparedStatement statement=connection.getPreparedStatement(GET_ADDR_ID);
			statement.setString(1, addrA);
			ResultSet result = statement.executeQuery();
			if(result.next()){
				idA =result.getLong("addr_id");
			}else{
				logger.fatal("address not found in database ["+addrA+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

			statement.setString(1, addrB);
			result = statement.executeQuery();
			if(result.next()){
				idB =result.getLong("addr_id");
			}else{
				logger.fatal("address not found in database ["+addrB+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
			
			
			result.close();
			statement.close();

			//get transaction id where the outputs of this address were spent
			List<Long> nextTx = new ArrayList<>();
			statement = connection.getPreparedStatement(GET_ADDR_NEXT_TX);
			statement.setLong(1, idA);
			result = statement.executeQuery();
			while(result.next()){
				nextTx.add(result.getLong("spent_in_tx"));
			};

			logger.debug("found transactions : "+nextTx.size()); 
			
			while(count < MAX && !found && !nextTx.isEmpty()){
				
				logger.debug("found transactions : "+nextTx.size());
				 logger.debug("steps : "+count);
					count ++;
					//check if addr b is in one of the next transactions
					int addrCount = 0;
					String countAddrIn = CreateWhereInStatement(COUNT_ADDR, TX_ID, nextTx.size());
					statement = connection.getPreparedStatement(countAddrIn);
					statement.setLong(1,idB);

					for(int i=0;i<nextTx.size();i++)
						statement.setLong(i+2,nextTx.get(i));

					result = statement.executeQuery();
					if(result.next()){
						addrCount = result.getInt("count");
					}
				
					if(addrCount > 0){
						logger.debug("found a path");
						found = true;
					}
					else{
                        logger.debug("path not jet found");
						//get next transactions
						String getNextTxIn = CreateWhereInStatement(GET_NEXT_TX, TX_ID, nextTx.size());
						statement = connection.getPreparedStatement(getNextTxIn);
						
						for(int i=0;i<nextTx.size();i++)
							statement.setLong(i+1,nextTx.get(i));
						
						//statement.set(1, nextTx);
						result = statement.executeQuery();
						nextTx = new ArrayList<>();
						while(result.next()){
							nextTx.add(result.getLong("spent_in_tx"));
						}
					}
				}
			
			if(found)
				this.result =count; 
			
            logger.debug("result : "+this.result);


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private String CreateWhereInStatement(String sql,String tableName,int size){
		
		if(size > 0 ){
			StringBuilder sb = new StringBuilder();
			sb.append(sql);
			sb.append(" AND ");
			sb.append(tableName);
			sb.append(" IN (?");
			
			for(int i=1;i<size;i++)
				sb.append(",?");
				
			sb.append(")");
			
			return sb.toString();
			
		}else
		return null;
	}
	
	@Override
	public Integer getResult() {
		return this.result;
	}

	@Override
	public String resultToString() {
		if(this.result < 1){
			return "No Path of Transactions found between ["
					+addrA+"] and ["+addrB+"]";
		}else{
			return "Shortest Path of Transactions  between ["
					+addrA+"] and ["+addrB+"] : "+ this.result;
		}
	}
	
	
	
}
