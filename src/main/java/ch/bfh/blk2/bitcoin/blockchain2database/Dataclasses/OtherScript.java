package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Any script can be represented as an OtherScript or OtherInputScript. However, whenever possible, a more specific
 * script type should be used. Also note that this class is only supposed to represent Output Scripts. For Input
 * Scripts, use the OtherInputScript class instead.
 * 
 * @author niklaus
 */
public class OtherScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OtherScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;
	private ScriptType type;

	private final String insertQuery = "INSERT INTO out_script_other(tx_id, tx_index, script_size, script_id) VALUES(?, ?, ?, ?);";

	/**
	 * 
	 * @param script The output script. Can be of any type
	 * @param scriptSize The size (in Byte) of the script
	 * @param tx_id The database Id of the transaction which the script is part of
	 * @param tx_index The index of the transaction which the script is part of within the block (and the database)
	 * @param type since this class can hold any type of script, you can chose the type yourself
	 */
	public OtherScript(Script script, int scriptSize, long tx_id, int tx_index, ScriptType type) {
		// Don't check for script type.
		// We want to allow any script to be saved like this if desired
		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;
		this.type = type;
	}

	public OtherScript(Script script, int scriptSize, long tx_id, int tx_index){
		this(script, scriptSize, tx_id, tx_index, ScriptType.OUT_OTHER);
	}

	@Override
	public ScriptType getType() {
		return type;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		ScriptManager scriptWriter = new ScriptManager();
		long scriptId = scriptWriter.writeScript(connection, script);

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
