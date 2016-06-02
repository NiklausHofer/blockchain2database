package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.TransactionOutput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Represents and writes to the database a Bitcoin Transaction output.
 * This class is mainly a glorified wrapper around Bitcoinj's TransactionOutput class 
 * that can apply some logic to it and write it into the database.
 * 
 * @author niklaus
 *
 */
public class DataOutput {

	private static final Logger logger = LogManager.getLogger("DataOutput");

	private static final String INSERT_OUTPUT = "INSERT INTO output"
			+ " (tx_id, tx_index, amount, script_type_id)"
			+ " VALUES(?, ?, ?, ?);";

	private TransactionOutput output;
	private long txId;

	/**
	 * This only instanciates the DataOutput object. Nothing will be written to the database
	 * until writeOutput() is called
	 * 
	 * @param output The Bitcoinj TransactionOutput object to be written into the database
	 * @param txId The database ID of the transaction to which this output belongs
	 */
	public DataOutput(TransactionOutput output, long txId) {
		this.output = output;
		this.txId = txId;
	}

	/**
	 * Writes the output information into the database. This will also initialize parsing of the 
	 * output script which will then be written to the database as well.
	 * 
	 * @param connection A database connection to be used to access the database
	 */
	public void writeOutput(DatabaseConnection connection) {

		try {
			PreparedStatement statement = connection.getPreparedStatement(INSERT_OUTPUT);

			statement.setLong(1, txId);
			statement.setLong(2, output.getIndex());
			statement.setLong(3, output.getValue().getValue());

			OutputScript outScript = OuputScriptCreator.parseScript(output, txId, output.getIndex());
			statement.setInt(4, outScript.getType().getValue());

			statement.executeUpdate();
			statement.close();

			outScript.writeOutputScript(connection);

		} catch (SQLException e) {
			logger.fatal("Failed to write Output #" + output.getIndex() + " on transaction " + txId, e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
