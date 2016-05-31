package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2SHOtherInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("P2SHOtherInputScript");

	private final static String INSERT_P2SH_OTHER_SCRIPT = "INSERT INTO unlock_script_p2sh_other"
			+ " (tx_id,tx_index,script_size,script_id,redeem_script_id,redeem_script_size)"
			+ " VALUES (?,?,?,?,?,?)";

	private int txIndex, scriptSize;
	private long txId;
	private Script script;

	public P2SHOtherInputScript(Script script, long txId, int txIndex, int scriptSize) {

		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
		this.script = script;

	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2SH_OTHER;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		byte[] redeemScript = getRedeemScript();

		//TODO check if this works
		// remove redeem script
		Script.removeAllInstancesOf(script.getProgram(), redeemScript);

		ScriptManager sc = new ScriptManager();
		long scriptId = sc.writeScript(connection, script);
		long redeemId = sc.writeScript(connection, new Script(redeemScript));

		try {

			PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_P2SH_OTHER_SCRIPT);
			insertStatement.setLong(1, txId);
			insertStatement.setInt(2, txIndex);
			insertStatement.setInt(3, scriptSize - redeemScript.length);
			insertStatement.setLong(4, scriptId);
			insertStatement.setLong(5, redeemId);
			insertStatement.setInt(6, redeemScript.length);

			insertStatement.executeUpdate();
			insertStatement.close();

		} catch (SQLException e) {
			logger.fatal("faild to insert Input script of type Coinbase", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

	}

	public byte[] getRedeemScript() {
		return script.getChunks().get(script.getChunks().size() - 1).data;
	}
}