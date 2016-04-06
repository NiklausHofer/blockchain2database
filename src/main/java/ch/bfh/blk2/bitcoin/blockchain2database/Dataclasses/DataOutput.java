package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class DataOutput {
	
	private static final Logger logger = LogManager.getLogger("DataOutput");
	
	private static final String INSERT_OUTPUT = "INSERT INTO output"
			+ " (amount, tx_id, tx_index,address)"
			+ " VALUES(?, ?, ?, ?);",
			
			INSERT_SCRIPT = "INSERT IGNORE INTO ? (tx_id,tx_index,script_size,script)"
			+ " VALUES(?, ?, ?, ?);",
			
			SMALL_SCRIPT_TABLE="small_out_script",
			
			LARGE_SCRIPT_TABLE="large_out_script";
	
	private static int maxScriptSize = 5; // TODO get from Properties

	private TransactionOutput output;
	private long txId;
	
	public DataOutput(TransactionOutput output,long txId){
		this.output = output;
		this.txId = txId;
	}
	
	public void writeOutput(DatabaseConnection connection){

			String address = null;
	 
			try {
				address = Utility.getAddressFromOutput(output).toString();
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Invalid Address: error" + e.getClass());
			}

			try {

				PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(INSERT_OUTPUT);

				statement.setLong(1, output.getValue().getValue());
				statement.setLong(2, txId);
				statement.setLong(3, output.getIndex());
				
				if(address == null)
					statement.setNull(4, java.sql.Types.NULL);
				else
					statement.setString(4, address);

				statement.executeUpdate();

				statement.close();
				insertScript(connection);
				
			} catch (SQLException e) {
				logger.fatal("Failed to write Output #"
						+ output.getIndex()
						+ " on transaction "
						+ txId);
				logger.fatal(e);
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
		}
	
	private void insertScript(DatabaseConnection connection){
		byte[] script = null;
		try{	
			script = output.getScriptBytes();
		}catch(ScriptException e){
			logger.debug("invalid output script");
		}
		
		if(script == null)
			return;
		else{ 
			try{
			PreparedStatement insertScriptStatement =(PreparedStatement) connection.getPreparedStatement(INSERT_SCRIPT);
			
			if(script.length > maxScriptSize)
				insertScriptStatement.setString(1, SMALL_SCRIPT_TABLE);
			else
				insertScriptStatement.setString(1, LARGE_SCRIPT_TABLE);
			
			insertScriptStatement.setLong(2, txId);
			insertScriptStatement.setLong(3, output.getIndex());
			insertScriptStatement.setLong(4,script.length);
			insertScriptStatement.setBytes(5, script);

			insertScriptStatement.executeUpdate();
			insertScriptStatement.close();
			
			}catch(SQLException e){
				logger.error("failed to insert output script");
				logger.error("output [tx : "+txId+", #"+output.getIndex()+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
		}
	}
	
}
