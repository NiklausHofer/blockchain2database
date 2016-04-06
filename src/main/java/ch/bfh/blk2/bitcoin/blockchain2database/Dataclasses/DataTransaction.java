package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class DataTransaction {

	private static final Logger logger = LogManager.getLogger("DataTransaction");

	private long blockId;
	private Transaction transaction;
	private DatabaseConnection connection;
	private Date date;
	private long tx_id;

	private String transactionInsertQuery = "INSERT INTO transaction"
			+ " (version, lock_time, blk_time, blk_id, tx_hash) "
			+ " VALUES (?, ?, ?, ?, ?);";

	private String outputInsertQuery = "INSERT INTO output"
			+ " (amount, tx_id, tx_index,address)"
			+ " VALUES(?, ?, ?, ?);";

	public DataTransaction(Transaction transaction, long blockId, DatabaseConnection connection, Date date) {
		this.transaction = transaction;
		this.blockId = blockId;
		this.connection = connection;
		this.date = date;
	}

	public void writeTransaction() {
		
		tx_id = -1;

		try {
			PreparedStatement transactionInsertStatement = (PreparedStatement) connection
					.getPreparedStatement(transactionInsertQuery);

			transactionInsertStatement.setLong(1, transaction.getVersion());
			transactionInsertStatement.setTimestamp(2, new java.sql.Timestamp(transaction.getLockTime() * 1000 + 1000));
			transactionInsertStatement.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
			transactionInsertStatement.setLong(4, blockId);
			transactionInsertStatement.setString(5, transaction.getHashAsString());

			transactionInsertStatement.executeUpdate();

			ResultSet rs = transactionInsertStatement.getGeneratedKeys();

			if (rs.next())
				tx_id = rs.getLong(1);
			else {
				logger.fatal(
						"Bad generatedKeySet or malformed response from Transaction " + transaction.getHashAsString());
				connection.commit();
				connection.closeConnection();
				System.exit(2);
			}

			rs.close();
			transactionInsertStatement.close();

		} catch (SQLException e) {
			logger.fatal("Failed to write transaction " + transaction.getHashAsString());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

		for (TransactionOutput output : transaction.getOutputs())
			writeOutput(output);

		for (int tx_index = 0;tx_index<transaction.getInputs().size();tx_index++){
			
			TransactionInput input = transaction.getInputs().get(tx_index);
			
			if (!input.isCoinBase()) {
				DataInput dataInput = new DataInput(input,tx_id,tx_index,date);
				dataInput.writeInput(connection);
			}
		}
	}

	private void writeOutput(TransactionOutput output) {

		String address = null;
 
		try {
			//Get Adress ID
			address = Utility.getAddressFromOutput(output).toString();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid Address: error" + e.getClass());
		}

		try {

			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(outputInsertQuery);

			statement.setLong(1, output.getValue().getValue());
			statement.setLong(2, tx_id);
			statement.setLong(3, output.getIndex());
			
			if(address == null)
				statement.setNull(4, java.sql.Types.NULL);
			else
				statement.setString(4, address);

			statement.executeUpdate();

			statement.close();
		} catch (SQLException e) {
			logger.fatal("Failed to write Output #"
					+ output.getIndex()
					+ " on transaction "
					+ transaction.getHashAsString());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
