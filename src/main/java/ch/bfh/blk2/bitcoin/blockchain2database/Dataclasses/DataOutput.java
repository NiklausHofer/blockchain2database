package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.TransactionOutput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataOutput {

	private static final Logger logger = LogManager.getLogger("DataOutput");

	private static final String INSERT_OUTPUT = "INSERT INTO output"
			+ " (tx_id, tx_index, amount, scriptType)"
			+ " VALUES(?, ?, ?, ?);";

	private TransactionOutput output;
	private long txId;

	public DataOutput(TransactionOutput output, long txId) {
		this.output = output;
		this.txId = txId;
	}

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
			logger.fatal("Failed to write Output #" + output.getIndex() + " on transaction " + txId);
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
