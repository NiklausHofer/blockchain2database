package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataTransaction {

	private static final Logger logger = LogManager.getLogger("DataTransaction");

	private long blockId;
	private Transaction transaction;
	private DatabaseConnection connection;
	private Date date;
	private long tx_id, blk_index;

	private String transactionInsertQuery = "INSERT INTO transaction"
			+ " (version, lock_time, blk_time, blk_id, tx_hash, blk_index) "
			+ " VALUES (?, ?, ?, ?, ?, ?);";

	public DataTransaction(Transaction transaction, long blockId, DatabaseConnection connection, Date date,
			long blk_index) {
		this.transaction = transaction;
		this.blockId = blockId;
		this.connection = connection;
		this.date = date;
		this.blk_index = blk_index;
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
			transactionInsertStatement.setLong(6, blk_index);

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

		for (TransactionOutput output : transaction.getOutputs()) {
			DataOutput dataOutput = new DataOutput(output, tx_id);
			dataOutput.writeOutput(connection);
		}

		for (int tx_index = 0; tx_index < transaction.getInputs().size(); tx_index++) {

			TransactionInput input = transaction.getInputs().get(tx_index);

			DataInput dataInput = new DataInput(input, tx_id, tx_index, date, connection);
			if (transaction.isCoinBase()) {
				logger.trace("Inserting a Coinbase Input");
				long totalAmount = 0;
				for (TransactionOutput out : transaction.getOutputs())
					totalAmount += out.getValue().getValue();
				dataInput.writeInput(totalAmount);
			} else
				dataInput.writeInput();
		}
	}
}
