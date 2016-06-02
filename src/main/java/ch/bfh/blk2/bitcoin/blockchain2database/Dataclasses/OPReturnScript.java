package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Represents OP_RETURN output scripts
 * 
 * @author niklaus
 */
public class OPReturnScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OPReturnScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;

	private final String insertQuery = "INSERT INTO out_script_op_return(tx_id, tx_index, script_size, information) VALUES(?, ?, ?, ?);";

	/**
	 * @param script The output script. Must be of type bare OP_RETURN
	 * @param scriptSize The size (in Byte) of the script
	 * @param tx_id The database Id of the transaction which the script is part of
	 * @param tx_index The index of the transaction which the script is part of within the block (and the database)
	 * @throws IllegalArgumentException If the script is not of type OP_RETURN
	 */
	public OPReturnScript(Script script, int scriptSize, long tx_id, int tx_index) throws IllegalArgumentException {
		if (!script.isOpReturn())
			throw new IllegalArgumentException("Script must be of type OP_RETURN");

		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_OP_RETURN;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		try {
			List<ScriptChunk> chunks = script.getChunks();

			PreparedStatement insertStatement = connection.getPreparedStatement(insertQuery);
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, scriptSize);

			byte[] data = chunks.get(1).data;
			if (data == null)
				data = new byte[0];

			insertStatement.setString(4, Utils.HEX.encode(data));

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (ScriptException | NullPointerException e) {
			logger.fatal(
					"Something went wrong when parsing the script for OP_Return output #"
							+ tx_index
							+ " for transaction with id "
							+ tx_id
							+ ". The script in question is: "
							+ script.toString(),
					e);
			System.exit(1);
		} catch (SQLException e) {
			logger.fatal("Unable to write OP_RETURN output #"
					+ tx_index
					+ " for transaction with id "
					+ tx_id
					+ ". Script: "
					+ script.toString(), e);
			System.exit(1);
		}

	}

}
