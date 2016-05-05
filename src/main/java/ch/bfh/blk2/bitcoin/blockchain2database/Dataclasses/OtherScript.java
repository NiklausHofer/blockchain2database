package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class OtherScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OtherScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;

	private final String insertQuery = "INSERT INTO out_script_other(tx_id, tx_index, script_size, script_id) VALUES(?, ?, ?, ?);";

	public OtherScript(Script script, int scriptSize, long tx_id, int tx_index) {
		// Don't check for script type.
		// We want to allow any script to be saved like this if desired
		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_OTHER;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		ScriptManager scriptWriter = new ScriptManager(script);
		long scriptId = scriptWriter.writeScript(connection);

		PreparedStatement insertStatement = connection.getPreparedStatement(insertQuery);
		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, scriptSize);
			insertStatement.setLong(4, scriptId);

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to insert the output script of type other for output #"
					+ tx_index
					+ " for transaction with id "
					+ tx_id, e);
			System.exit(1);
		}
	}

}
