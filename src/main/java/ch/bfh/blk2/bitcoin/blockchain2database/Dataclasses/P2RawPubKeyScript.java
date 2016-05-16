package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2RawPubKeyScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("P2RawPubKeyScript");
	private final static int MAX_KEY_LENGTH = 65;

	private static final String INSERT_P2RAW_PUBKEY_SCRIPT = "INSERT INTO out_script_p2raw_pub_key (tx_id,tx_index,script_size,public_key_id) VALUES (?,?,?,?)";

	private Script script;

	private int scriptSize, txIndex;

	private long txId;
	private byte[] pkBytes;

	public P2RawPubKeyScript(Script script, int scriptSize, long txId, int txIndex) throws IllegalArgumentException{

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
		
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_P2RAWPUBKEY;
	}
	
	private void parse() throws IllegalArgumentException{
		if (!script.isSentToRawPubKey())
			throw new IllegalArgumentException("Script must be of type Pay to RawPybKey");

		ScriptChunk pkChunk = script.getChunks().get(0);
		
		if(pkChunk.data.length > MAX_KEY_LENGTH)
			throw new IllegalArgumentException("Data in PAY TO RAW PUB KEY output script too large to be a valid public key");
		
		pkBytes = pkChunk.data;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		PubKeyManager pm = PubKeyManager.getInstance();
		long pkId = pm.insertRawPK(connection, pkBytes);

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
