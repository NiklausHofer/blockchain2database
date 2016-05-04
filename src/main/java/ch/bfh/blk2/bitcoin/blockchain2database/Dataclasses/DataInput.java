package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionInput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.PropertiesLoader;

public class DataInput {

	private static int maxScriptSize = Integer
			.parseInt(PropertiesLoader.getInstance().getProperty("max_inmemory_input_script"));

	private static final Logger logger = LogManager.getLogger("DataInput");

	// initialized in constructor
	private TransactionInput input;
	private long tx_id;
	private long tx_index;
	private Date date;
	private DatabaseConnection connection;

	// get from DB querry
	private long prev_tx_id;
	private int prev_script_type;
	private long amount;

	private byte[] script = null;

	private long input_id = -1;

	// Querry Strings
	private String inputAmountQuery = "SELECT transaction.tx_id, output.amount, output.script_type"
			+ " FROM transaction RIGHT JOIN output ON transaction.tx_id = output.tx_id"
			+ " WHERE transaction.tx_hash = ? AND output.tx_index = ?;";

	private String dataInputQuery = "INSERT INTO input "
			+ " (tx_id,tx_index,prev_tx_id,prev_output_index,sequence_number,amount, largescript)"
			+ " VALUES( ?, ?, ?, ?, ?, ?, ?);";

	private String outputUpdateQuery = "UPDATE output"
			+ " SET spent_by_tx = ?, spent_by_index = ?, spent_at = ?"
			+ " WHERE tx_id = ?"
			+ " AND tx_index = ?;";

	public DataInput(TransactionInput input, long tx_id, long tx_index, Date date, DatabaseConnection con) {
		this.input = input;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.date = date;
		this.connection = con;

	}

	public void writeInput() {
		retrievePrevOutInformation();
		dowriteInput();
	}

	public void writeInput(long amount) {
		this.amount = amount;
		dowriteInput();
	}

	private void dowriteInput() {

		try {
			PreparedStatement statement = connection.getPreparedStatement(dataInputQuery);

			statement.setLong(1, tx_id);
			statement.setLong(2, tx_index);
			statement.setLong(3, prev_tx_id);
			statement.setLong(4, input.getOutpoint().getIndex());
			statement.setLong(5, input.getSequenceNumber());
			statement.setLong(6, amount);

			try {
				script = input.getScriptBytes();
			} catch (ScriptException e) {
				logger.debug("invalid input script");
			}
			if (script == null)
				statement.setNull(7, java.sql.Types.NULL);
			else if (script.length > maxScriptSize)
				statement.setBoolean(7, true);
			else
				statement.setBoolean(7, false);

			statement.executeUpdate();
			statement.close();

			updateOutputs();

			// TODO insert the scripts

		} catch (SQLException e) {
			logger.fatal("Failed to write Input #" + input_id + " on Transaction #" + tx_id);
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

	private void updateOutputs() {

		try {
			PreparedStatement statement = connection.getPreparedStatement(outputUpdateQuery);

			statement.setLong(1, tx_id);
			statement.setLong(2, tx_index);
			statement.setTimestamp(3, new Timestamp(date.getTime()));
			statement.setLong(4, prev_tx_id);
			statement.setLong(5, input.getOutpoint().getIndex());

			statement.executeUpdate();

			statement.close();

		} catch (SQLException e) {
			logger.fatal("Failed to update Output [tx: " + prev_tx_id + " , # " + input.getOutpoint().getIndex());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

	private void retrievePrevOutInformation() {
		try {
			PreparedStatement statement = connection.getPreparedStatement(inputAmountQuery);
			statement.setString(1, input.getOutpoint().getHash().toString());
			statement.setLong(2, input.getOutpoint().getIndex());

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				prev_tx_id = rs.getLong(1);
				amount = rs.getLong(2);
				prev_script_type = rs.getInt(3);
			} else {
				logger.fatal(
						"Got a malformed response from the database while looking for an output reffered to by one of "
								+ "Tx # "
								+ tx_id
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
					+ "tx # "
					+ tx_id
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
}
