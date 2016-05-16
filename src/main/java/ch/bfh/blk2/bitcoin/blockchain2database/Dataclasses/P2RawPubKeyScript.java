package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2RawPubKeyScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("P2RawPubKeyScript");

	private static final String INSERT_P2RAW_PUBKEY_SCRIPT = "INSERT INTO out_script_p2raw_pub_key (tx_id,tx_index,script_size,public_key_id) VALUES (?,?,?,?)";

	private Script script;

	private int scriptSize, txIndex;

	private long txId;

	public P2RawPubKeyScript(Script script, int scriptSize, long txId, int txIndex) {
		if (!script.isSentToRawPubKey())
			throw new IllegalArgumentException("Script must be of type Pay to RawPybKey");

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_P2RAWPUBKEY;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {

		byte[] pubKey = script.getPubKey();

		PubKeyManager pm = PubKeyManager.getInstance();
		long pkId = pm.insertRawPK(connection, pubKey);

		try {

			PreparedStatement inserRawPKScript = connection.getPreparedStatement(INSERT_P2RAW_PUBKEY_SCRIPT);
			inserRawPKScript.setLong(1, txId);
			inserRawPKScript.setInt(2, txIndex);
			inserRawPKScript.setInt(3, scriptSize);
			inserRawPKScript.setLong(4, pkId);
			inserRawPKScript.executeUpdate();
			
			inserRawPKScript.close();

		} catch (SQLException e) {
			logger.fatal("Failed to write P2raw PK script");
			logger.fatal("in output [tx_id: " + txId + ", tx_index:" + txIndex + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
