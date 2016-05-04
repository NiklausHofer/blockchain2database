package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.PropertiesLoader;
import ch.bfh.blk2.bitcoin.util.Utility;

public class DataOutput {

	private static final Logger logger = LogManager.getLogger("DataOutput");

	private static final String INSERT_OUTPUT = "INSERT INTO output"
			+ " (amount, tx_id, tx_index, address, scriptType)"
			+ " VALUES(?, ?, ?, ?, ?);",

	INSERT_SMALL_SCRIPT = "INSERT IGNORE INTO small_out_script (tx_id,tx_index,script_size,script, isOpReturn, isPayToScriptHash, isSentToAddress, isSentoToMultiSig, isSentToRawPubKey)"
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);",

	INSERT_LARGE_SCRIPT = "INSERT IGNORE INTO large_out_script (tx_id,tx_index,script_size,script, isOpReturn, isPayToScriptHash, isSentToAddress, isSentoToMultiSig, isSentToRawPubKey)"
			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static int maxScriptSize = Integer
			.parseInt(PropertiesLoader.getInstance().getProperty("max_inmemory_output_script"));

	private TransactionOutput output;
	private byte[] script = null;
	private long txId;

	public DataOutput(TransactionOutput output, long txId) {
		this.output = output;
		this.txId = txId;
	}

	public void writeOutput(DatabaseConnection connection) {

		String address = null;

		OutputScript outScript;

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

			if (address == null)
				statement.setNull(4, java.sql.Types.NULL);
			else
				statement.setString(4, address);

			outScript = OuputScriptCreator.parseScript(output);

			statement.setInt(5, outScript.getType().getValue());

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
