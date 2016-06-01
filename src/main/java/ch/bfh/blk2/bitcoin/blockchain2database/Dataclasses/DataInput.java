package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.TransactionInput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Represents a Bitcoin transaction input. This is basically a wrapper with some added intelligence aroudn the bitcoinj Input object
 * that writes the input and all corresponding data, such as the script, into the database.
 *  
 * @author niklaus
 */
public class DataInput {

	private static final Logger logger = LogManager.getLogger("DataInput");

	// initialized in constructor
	private TransactionInput input;
	private long tx_id;
	private int tx_index;
	private Date date;
	private DatabaseConnection connection;

	// get from DB querry
	private long prev_tx_id;
	private ScriptType prev_script_type;
	private long amount;

	private byte[] script = null;

	private long input_id = -1;

	// Querry Strings
	private String inputAmountQuery = "SELECT transaction.tx_id, output.amount, output.script_type_id"
			+ " FROM transaction RIGHT JOIN output ON transaction.tx_id = output.tx_id"
			+ " WHERE transaction.tx_hash = ? AND output.tx_index = ?;";

	private String dataInputQuery = "INSERT INTO input "
			+ " (tx_id,tx_index,prev_tx_id,prev_output_index,sequence_number,amount, script_type_id)"
			+ " VALUES( ?, ?, ?, ?, ?, ?, ?);";

	private String outputUpdateQuery = "UPDATE output"
			+ " SET spent_by_tx = ?, spent_by_index = ?, spent_at = ?"
			+ " WHERE tx_id = ?"
			+ " AND tx_index = ?;";

	/**
	 * Creates a new DataInput object. No data will be written into the database until writeInput() is called.
	 * 
	 * @param input
	 * @param tx_id
	 * @param tx_index
	 * @param date
	 * @param con
	 */
	public DataInput(TransactionInput input, long tx_id, int tx_index, Date date, DatabaseConnection con) {
		this.input = input;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.date = date;
		this.connection = con;

	}

	/**
	 * Writes the input into the database. Make sure that the corresponding previous output is in the database before you call this method.
	 * This is because the entry for the previous output will be read to retrieve some more information on the input and its script.
	 */
	public void writeInput() {
		retrievePrevOutInformation();
		dowriteInput();
	}

	/**
	 * Use this to write coinbase inputs (aka Coinbase). Pass the amount of the input. No previous output will be searched and the 
	 * type of the script will be fixed to represent Coinbase.
	 * 
	 * @param amount The amount of money coming from this Coinbase
	 */
	public void writeInput(long amount) {
		this.amount = amount;
		this.prev_script_type = ScriptType.NO_PREV_OUT;
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

			InputScript inScript = InputScriptCreator.parseScript(input, tx_id, tx_index, prev_script_type, prev_tx_id,
					(int) input.getOutpoint().getIndex());
			statement.setInt(7, inScript.getType().getValue());

			statement.executeUpdate();
			statement.close();

			updateOutputs();

			inScript.writeInput(connection);

		} catch (SQLException e) {
			logger.fatal("Failed to write Input #" + input_id + " on Transaction #" + tx_id, e);
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
			logger.fatal("Failed to update Output [tx: " + prev_tx_id + " , # " + input.getOutpoint().getIndex(), e);
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

				//TODO check if this works !!WARNING!!
				// only if value = index in Enum ScriptType
				prev_script_type = ScriptType.values()[rs.getInt(3)];

				/*
				 *  if value and index does not match
				 *
				for(ScriptType t : ScriptType.values()){
					if(t.getValue() == rs.getInt(4)){
						prev_script_type = t;
						break;
					}
				}
				*/

			} else
				throw new SQLException("Got a malformed response from the database while looking for the output");

			rs.close();
			statement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to find output for one of transaction "
					+ "tx # "
					+ tx_id
					+ "'s Inputs. We were looking for output #"
					+ input.getOutpoint().getIndex()
					+ " of Tranasaction "
					+ input.getOutpoint().getHash().toString(), e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}
}
