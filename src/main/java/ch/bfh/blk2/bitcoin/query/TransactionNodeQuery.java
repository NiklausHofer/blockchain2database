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
			GET_ADDR_OUTPUT_TX="SELECT tx_id FROM output WHERE address = ?",
			
			GET_NEXT_TX =
					"SELECT spent_by_tx"
					+ " FROM output"
					+ " WHERE spent_by_tx IS NOT NULL"
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
			List<Long> listA=new ArrayList<>(),listB= new ArrayList<>();

			//Get List of transactions where outputs go to Adresses A and B
			PreparedStatement statement=connection.getPreparedStatement(GET_ADDR_OUTPUT_TX);
			statement.setString(1, addrA);
			ResultSet result = statement.executeQuery();
			while(result.next()){
				 listA.add(result.getLong("tx_id"));
			}
			
			if(listA.isEmpty()){
				logger.fatal("address not found in database ["+addrA+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

			statement=connection.getPreparedStatement(GET_ADDR_OUTPUT_TX);
			statement.setString(1, addrB);
			result = statement.executeQuery();
			while(result.next()){
				 listB.add(result.getLong("tx_id"));
			}
			
			if(listB.isEmpty()){
				logger.fatal("address not found in database ["+addrB+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
			
			result.close();
			statement.close();

			logger.debug("start transactions : "+listA.size()); 
			logger.debug("end transactions : "+listB.size());
			
			
			while(count < MAX && !found && !listA.isEmpty()){
				
				count ++;
				
				logger.debug("found transactions : "+listA.size());
				logger.debug("steps : "+count);
				//check if addr b is in one of the next transactions

				String countAddrIn = CreateWhereInStatement(GET_NEXT_TX, "tx_id", listA.size());
				statement = connection.getPreparedStatement(countAddrIn);

				for(int i=0;i<listA.size();i++)
					statement.setLong(i+1,listA.get(i));

				result = statement.executeQuery();

				listA = new ArrayList<>();

				while(result.next()){
					long nextTx =result.getLong("spent_by_tx");

					if(listB.contains(nextTx))
						found = true;

					listA.add(nextTx);
				}

				result.close();
				statement.close();

				if(found){
					logger.debug("found a path");
					break;
				}

				logger.debug("path not jet found");
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
