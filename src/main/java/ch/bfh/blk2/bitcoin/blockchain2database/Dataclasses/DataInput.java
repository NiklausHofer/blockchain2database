package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionInput;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataInput {

	private static int maxScriptSize = 5; //TODO create a properties Utility
	
	private static final Logger logger = LogManager.getLogger("DataInput");

	// initialized in constructor
	private TransactionInput input;
	private long tx_id;
	private long tx_index;
	private Date date;

	// get from DB querry
	private long prev_tx_id;
	private long amount;
	
	private long input_id = -1;
	
	// Querry Strings
	private String inputAmountQuery = 
			"SELECT transaction.tx_id, output.amount"
			+ " FROM transaction RIGHT JOIN output ON transaction.tx_id = output.tx_id"
			+ " WHERE transaction.tx_hash = ? AND output.tx_index = ?;";

	private String dataInputQuery = "INSERT INTO input "
			+ " (tx_id,tx_index,prev_tx_id,prev_output_index,sequence_number,amount)"
			+ " VALUES( ?, ?, ?, ?, ?, ?);";

	private String outputUpdateQuery = "UPDATE output"
			+ " SET spent_by_tx = ?, spent_by_index = ?, spent_at = ?"
			+ " WHERE tx_id = ?"
			+ " AND tx_index = ?;";
	
	private String insertScript = "INSERT IGNORE INTO ? (tx_id,tx_index,script_size,script)"
			+ " VALUES(?, ?, ?, ?);";
	
	private static final String SMALL_SCRIPT_TABLE="small_in_script",
				LARGE_SCRIPT_TABLE="large_in_script";	

	public DataInput(TransactionInput input,long tx_id,long tx_index,Date date) {
		this.input = input;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.date=date;
		
	}

	public void writeInput(DatabaseConnection connection) {

		try {

			getPrevAmount(connection);
			
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(dataInputQuery);

			statement.setLong(1, tx_id);
			statement.setLong(2, tx_index);
			statement.setLong(3, prev_tx_id);
			statement.setLong(4, input.getOutpoint().getIndex());
			statement.setLong(5, input.getSequenceNumber());
			statement.setLong(6, amount);

			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();

			if (rs.next())
				input_id = rs.getLong(1);
			else {
				logger.fatal("Malformed response from Database when reading ID for a new Input");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

			rs.close();
			statement.close();
			
			updateOutputs(connection);
			insertScript(connection);
			
		} catch (SQLException e) {
			logger.fatal("Failed to write Input #" + input_id + " on Transaction #" + tx_id);
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

	private void updateOutputs(DatabaseConnection connection) {

		try {			
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(outputUpdateQuery);

			statement.setLong(1, tx_id);
			statement.setLong(2, tx_index);
			statement.setTimestamp(3, new Timestamp(date.getTime()));
			statement.setLong(4, prev_tx_id);
			statement.setLong(5, input.getOutpoint().getIndex());

			statement.executeUpdate();

			statement.close();
			
		} catch (SQLException e) {
			logger.fatal("Failed to update Output [tx: "+prev_tx_id+" , # "+input.getOutpoint().getIndex());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}
	
	private void getPrevAmount(DatabaseConnection connection){
		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(inputAmountQuery);
			statement.setString(1, input.getOutpoint().getHash().toString());
			statement.setLong(2, input.getOutpoint().getIndex());

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				prev_tx_id = rs.getLong(1);
				amount = rs.getLong(2);
				
			} else {
				logger.fatal(
						"Got a malformed response from the database while looking for an output reffered to by one of "
								+ "Tx # " + tx_id
								+ " Inputs can not be found\n"
								+ "The specific output we were looking for  is: "
								+ input.getOutpoint().getHash().toString()
								+ " index "
								+ input.getOutpoint().getIndex());
				connection.commit();
				connection.closeConnection();
				System.exit(2);
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to find output for one of transaction "
					+ "tx # "+tx_id
					+ "'s Inputs. We were looking for output #"
					+ input.getOutpoint().getIndex()
					+ " of Tranasaction "
					+ input.getOutpoint().getHash().toString());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}
	
	private void insertScript(DatabaseConnection connection){
		
		byte[] script = null;
		try{	
			script = input.getScriptBytes();
		}catch(ScriptException e){
			logger.debug("invalid input script");
		}
		
		if(script == null)
			return;
		else{ 
			try{
			PreparedStatement insertScriptStatement =(PreparedStatement) connection.getPreparedStatement(insertScript);
			
			if(script.length > maxScriptSize)
				insertScriptStatement.setString(1, SMALL_SCRIPT_TABLE);
			else
				insertScriptStatement.setString(1, LARGE_SCRIPT_TABLE);
			
			insertScriptStatement.setLong(2, tx_id);
			insertScriptStatement.setLong(3, tx_index);
			insertScriptStatement.setLong(4,script.length);
			insertScriptStatement.setBytes(5, script);

			insertScriptStatement.executeUpdate();
			
			}catch(SQLException e){
				logger.error("failed to insert input script");
				logger.error("input [tx : "+tx_id+", #"+tx_index+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
		}
		
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setPrev_tx_id(long prev_tx_id) {
		this.prev_tx_id = prev_tx_id;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void setTx_id(long tx_id) {
		this.tx_id = tx_id;
	}

	public void setTxIndex(long tx_index){
		this.tx_index = tx_index;
	}
	
	public long getTxIndex(){
		return tx_index;
	}
}
