package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2PKHInputScript implements InputScript {

	private final static String INSERT_P2PK_SCRIPT = "INSERT INTO unlock_script_p2pkh(tx_id,tx_index,script_size,pubkey_id,signature_id) VALUES (?,?,?,?,?)";

	private static final Logger logger = LogManager.getLogger("P2PKHInputScript");

	private Script script;
	private int txIndex, scriptSize;
	private long txId;

	public P2PKHInputScript(Script script, long txId, int txIndex, int scriptSize) {

		this.script = script;
		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2PKH;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		/*
		byte[] signature = getSignature();
		byte[] pubkey = getPubkey();
		*/
		byte[] signature = script.getChunks().get(0).data;
		byte[] pubkey = script.getChunks().get(1).data;

		long pubkeyId = -1;
		PubKeyManager pm = PubKeyManager.getInstance();
		try{
			pubkeyId = pm.insertRawPK(connection, pubkey);
		} catch (IllegalArgumentException e){
			logger.fatal(script.toString());
			System.exit(1);
		}

		SigManager sm = SigManager.getInstance();
		long signId = sm.saveAndGetSigId(connection, signature, pubkeyId);

		try {

			PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_P2PK_SCRIPT);
			insertStatement.setLong(1, txId);
			insertStatement.setInt(2, txIndex);
			insertStatement.setInt(3, scriptSize);
			insertStatement.setLong(4, pubkeyId);
			insertStatement.setLong(5, signId);

			insertStatement.executeUpdate();
			insertStatement.close();

		} catch (SQLException e) {
			logger.fatal("faild to insert Input script of type P2PK Hash", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

	}

	/*
	private byte[] getSignature() {
		return script.getChunks().get(0).data;
	}

	private byte[] getPubkey() {
		return script.getChunks().get(1).data;
	}
	*/
}
