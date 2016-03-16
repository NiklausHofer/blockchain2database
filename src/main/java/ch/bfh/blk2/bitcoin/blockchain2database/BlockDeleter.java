package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockDeleter {


	private static final Logger logger = LogManager.getLogger("BlockDeleter");

	String GET_BLOCK_ID = "SELECT blk_id FROM block WHERE hash = ?",
			GET_TRANSACTION_ID = "SELECT tx_id FROM transaction WHERE blk_id = ?",
			GET_OUTPUT_ID = "SELECT output_id FROM output WHERE tx_id = ?",
			GET_INPUT_ID = "SELECT input_id FROM input WHERE tx_id= ?",

			REMOVE_OUTPUT_SCRIPT = "DELETE FROM script WHERE output_id = ?",
			REMOVE_INPUT_SCRIPT = "DELETE FROM script WHERE input_id = ?",
			REMOVE_OUTPUT = "DELETE FORM output WHERE tx_id = ?",
			REMOVE_INPUT = "DELETE FROM input WHERE tx_id = ?",
			REMOVE_BLOCK = "DELETE FROM block WHERE blk_id = ?",
			REMOVE_TRANSACTION = "DELETE FROM transaction WHERE tx_id = ?",


			MARK_AS_UNSPENT="UPDATE output"
					+ "SET spent=0,"
					+ "spent_by_input = NULL,"
					+ "spent_in_tx = NULL,"
					+ "spent_at = NULL,"
					+ "WHERE spent_in_tx = ?";

	private List<PreparedStatement> statements= new ArrayList<>();

	public void deleteBlock(String blockHash,DatabaseConnection connection){

		try {

			//get block id
			long blkId=-1;
			ResultSet result = getFromDB(GET_BLOCK_ID, blkId, connection);

			if(result.next())
				blkId = result.getLong(0);
			result.close();

			if(blkId <0){
				logger.trace("block not in database: "+blockHash );
				return;
			}

			//get transactions of this block

			ResultSet transactions=getFromDB(GET_TRANSACTION_ID, blkId, connection);

			for(int i = 0;transactions.next();i++){

				long txId =transactions.getLong(i);

				//get output and input ids and remove scripts
				ResultSet outputs = getFromDB(GET_OUTPUT_ID, txId, connection);
				for(int j = 0;outputs.next();j++){
					long outputId = result.getLong(j);
					removeFromDB(REMOVE_OUTPUT_SCRIPT, outputId, connection);
				}
				outputs.close();

				ResultSet inputs = getFromDB(GET_INPUT_ID, txId, connection);
				for(int j = 0;inputs.next();j++){
					long inputId = result.getLong(j);
					removeFromDB(REMOVE_INPUT_SCRIPT, inputId, connection);
				}
				inputs.close();

				//remove outputs and inputs
				removeFromDB(REMOVE_OUTPUT, txId, connection);

				//remove inputs
				removeFromDB(REMOVE_INPUT, txId , connection);

				//set outputs to unspent
				PreparedStatement unspentstatement = connection.getPreparedStatement(MARK_AS_UNSPENT);
				unspentstatement.executeUpdate();
				unspentstatement.close();

				removeFromDB(REMOVE_TRANSACTION, txId, connection);
			}

			//remove block
			removeFromDB(REMOVE_BLOCK, blkId, connection);
			connection.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			closeStatements();
		}

		logger.trace("removed block from datbase: "+blockHash );

	}

	private void closeStatements(){
		try {
			for(PreparedStatement statement: statements){
				statement.close();
				statements.remove(statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private ResultSet getFromDB(String sql,long id, DatabaseConnection connection){

		PreparedStatement statement = connection.getPreparedStatement(sql);	
		statements.add(statement);
		ResultSet result=null;
		try {
			statement.setLong(1, id);
			result = statement.executeQuery();	
			statement.closeOnCompletion();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void removeFromDB(String sql,long id,DatabaseConnection connection){
		PreparedStatement statement = connection.getPreparedStatement(sql);	
		statements.add(statement);
		try {
			statement.setLong(1, id);
			statement.executeUpdate();	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
