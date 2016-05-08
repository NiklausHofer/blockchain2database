package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class OtherInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("OtherInputScript");

	private final String INPUT_QUERY = "INSERT INTO unlock_script_other(tx_id, tx_index, script_size, script_id) VALUES( ?, ?, ?, ? );";

	private long tx_id;
	private int tx_index;
	private Script script;
	private int script_size;
	private ScriptType type;

	public OtherInputScript(long tx_id, int tx_index, Script script, int script_size, ScriptType type) {
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.script = script;
		this.script_size = script_size;
		this.type = type;
	}

	public OtherInputScript(long tx_id, int tx_index, Script script, int script_size) {
		this(tx_id, tx_index, script, script_size, ScriptType.IN_OTHER);
	}

	@Override
	public ScriptType getType() {
		return type;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		ScriptManager scima = new ScriptManager();
		long script_id = scima.writeScript(connection, script);

		PreparedStatement insertStatement = connection.getPreparedStatement(INPUT_QUERY);

		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, script_size);
			insertStatement.setLong(4, script_id);

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal(
					"Unable to insert input script of type other for input #" + tx_index + " of transaction " + tx_id,
					e);
			System.exit(1);
		}

	}

}
