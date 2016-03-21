package ch.bfh.blk2.bitcoin.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.fabric.xmlrpc.base.Array;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class TransactionNodeQuery implements Query<Long>{

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
				+ " AND spent = 1"
				+ " WHERE tx_id IN ?",
			
			COUNT_ADDR =
				"SELECT COUNT(addr_id) as count"
				+ " FROM OUTPUT"
				+ " WHERE addr_id = ?"
				+ " AND tx_id IN ?"
			;
	
	private static final int MAX = 6;
			
			
	
	
	private static final Logger logger = LogManager.getLogger("TransactionNodeQuery");
	
	private String addrA,addrB;

	public TransactionNodeQuery(String addrA,String addrB){
		this.addrA=addrA;
		this.addrB=addrB;
	}
	
	@Override
	public void exequte(DatabaseConnection connection) {

		long length=0;
		long count=0;
		boolean found= false;

		//create a List of output_id for address A and B
		List<Long> spentOutputsA = new ArrayList<>(),
				outputsB = new ArrayList<>();

		try{
			long idA=-1,idB=-1;

			//Get id of addesses A and B
			PreparedStatement statement=connection.getPreparedStatement(GET_ADDR_ID);
			statement.setString(1, addrA);
			ResultSet result = statement.executeQuery();
			if(result.next()){
				idA =result.getLong("addr_id");
			}

			statement.setString(1, addrB);
			result = statement.executeQuery();
			if(result.next()){
				idB =result.getLong("addr_id");
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

			do{
				count ++;
				//check if addr b is in one of the next transactions
				int addrCount = 0;
				statement = connection.getPreparedStatement(COUNT_ADDR);
				statement.setArray(1,nextTx.toArray());
				result = statement.executeQuery();
				if(result.next()){
					addrCount = result.getInt("count");
				}
				if(addrCount > 0){
					found = true;
				}
				else{
					//get next transactions
					statement = connection.getPreparedStatement(GET_NEXT_TX);
					//statement.set(1, nextTx);
					result = statement.executeQuery();
					nextTx = new ArrayList<>();
					while(result.next()){
						nextTx.add(result.getLong("spent_in_tx"));
					}
				}
			}while(count < MAX && !found && !nextTx.isEmpty());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	
	@Override
	public Long getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String resultToString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
