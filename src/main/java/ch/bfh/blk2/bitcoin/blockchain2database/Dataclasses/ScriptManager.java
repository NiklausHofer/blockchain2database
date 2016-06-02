package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Manages the script table in the database. Allows to easily write new scripts.
 * 
 * @author niklaus
 */
public class ScriptManager {

	private static final Logger logger = LogManager.getLogger("ScriptManager");

	private final String currentScriptIdQuery = "SELECT MAX(script_id) FROM script;";
	private final String insertInstruction = "INSERT INTO script (script_id, script_index, op_code, data) VALUES(?, ?, ?, ?);";

	/**
	 * Writes the script into the database.
	 * 
	 * @param connection the connection to the database to be used
	 * @param script The script to be written to the database
	 * @return The new script's database ID
	 */
	public long writeScript(DatabaseConnection connection, Script script) {
		long scriptId = -2;

		// First, figure out current script id in the script table
		try {
			PreparedStatement queryStatement = connection.getPreparedStatement(currentScriptIdQuery);
			ResultSet rs_1 = queryStatement.executeQuery();
			if (rs_1.next()) {
				rs_1.close();
				queryStatement.close();
				scriptId = rs_1.getLong(1);
			} else
				throw new SQLException("Did not get a result as expected");
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
					insertStatement.setString(4, Utils.HEX.encode(chunk.data));
				else
					insertStatement.setNull(4, java.sql.Types.NULL);

				insertStatement.executeUpdate();

				insertStatement.close();
			} catch (SQLException e) {
				logger.fatal("Unable to write chunk #" + index + " for this script: " + script.toString(), e);
				System.exit(1);
			}
		}

		return scriptId;
	}

}
