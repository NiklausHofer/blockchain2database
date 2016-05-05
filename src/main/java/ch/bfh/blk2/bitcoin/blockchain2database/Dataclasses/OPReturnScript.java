package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class OPReturnScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OPReturnScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;

	private final String insertQuery = "INSERT INTO out_script_op_return(tx_id, tx_index, script_size, information) VALUES(?, ?, ?, ?);";

	public OPReturnScript(Script script, int scriptSize, long tx_id, int tx_index) {
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
			if (chunks.size() > 2)
				logger.warn("OP_RETURN output script #"
						+ tx_index
						+ " of transaction with id "
						+ tx_id
						+ " has more than two script chunks. Will use the second one for pushdata and ignore the other ones");

			PreparedStatement insertStatement = connection.getPreparedStatement(insertQuery);
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, scriptSize);
			if (chunks.size() >= 2)
				insertStatement.setBytes(4, chunks.get(2).data);
			else {
				logger.warn("OP_RETURN output script #"
						+ tx_index
						+ " of transaction with id "
						+ tx_id
						+ " had less than two script chunks!");
				insertStatement.setNull(4, java.sql.Types.NULL);

				insertStatement.executeUpdate();

				insertStatement.close();
			}
		} catch (ScriptException e) {
			logger.fatal("Something went wrong when parsing the script for OP_Return output #"
					+ tx_index
					+ " for transaction with id "
					+ tx_id, e);
			System.exit(1);
		} catch (SQLException e) {
			logger.fatal("Unable to write OP_RETURN output #" + tx_index + " for transaction with id " + tx_id, e);
		}

	}

}
