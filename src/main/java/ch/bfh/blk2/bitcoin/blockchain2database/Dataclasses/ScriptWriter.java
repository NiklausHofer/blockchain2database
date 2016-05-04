package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class ScriptWriter {

	private static final Logger logger = LogManager.getLogger("ScriptWriter");

	private Script script;

	private final String currentScriptIdQuery = "SELECT MAX(script_id) FROM script;";
	private final String insertInstruction = "INSERT INTO script (script_id, script_index, op_code, data) VALUES(?, ?, ?, ?);";

	public ScriptWriter(Script script) {
		this.script = script;
	}

	/**
	 * Writes the script into the database and returns the id of the script
	 *
	 * @return
	 */
	public long writeScript(DatabaseConnection connection) {
		long scriptId = -2;

		// First, figure out current script id in the script table
		try {
			PreparedStatement queryStatement = connection.getPreparedStatement(currentScriptIdQuery);
			ResultSet rs_1 = queryStatement.executeQuery();
			scriptId = rs_1.getLong(1);
		} catch (SQLException e) {
			logger.fatal("Unable to retrieve current max script_id!", e);
			System.exit(1);
		}

		scriptId++;

		int index = 0;
		for (ScriptChunk chunk : script.getChunks()) {
			PreparedStatement insertStatement = connection.getPreparedStatement(insertInstruction);
			try {
				insertStatement.setLong(1, scriptId);
				insertStatement.setInt(2, index++);
				insertStatement.setInt(3, chunk.opcode);
				if (chunk.data != null)
					insertStatement.setBytes(4, chunk.data);
				else
					insertStatement.setNull(4, java.sql.Types.NULL);
			} catch (SQLException e) {
				logger.fatal("Unable to write chunk #" + index + " for this script", e);
				System.exit(1);
			}
		}

		return scriptId;
	}

}
