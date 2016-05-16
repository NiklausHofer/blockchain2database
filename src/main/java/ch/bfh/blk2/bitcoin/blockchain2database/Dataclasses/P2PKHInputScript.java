package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2PKHInputScript implements InputScript {

	private final static String INSERT_P2PK_SCRIPT = "INSERT INTO unlock_script_p2pkh(tx_id,tx_index,script_size,pubkey_id,signature_id) VALUES (?,?,?,?,?)";

	private final static int MAX_KEY_LENGTH = 65;
	private final static int MAX_SIG_LENGTH = 73;

	private static final Logger logger = LogManager.getLogger("P2PKHInputScript");

	private Script script;
	private int txIndex, scriptSize;
	private long txId;
	private byte[] pkBytes;
	private byte[] sigBytes;

	public P2PKHInputScript(Script script, long txId, int txIndex, int scriptSize) throws IllegalArgumentException {

		this.script = script;
		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
		
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2PKH;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
		long pubkeyId = -1;
		PubKeyManager pm = PubKeyManager.getInstance();
		try{
			pubkeyId = pm.insertRawPK(connection, pkBytes);
		} catch (IllegalArgumentException e){
			logger.fatal(script.toString());
			System.exit(1);
		}

		SigManager sm = SigManager.getInstance();
		long signId = sm.saveAndGetSigId(connection, sigBytes);

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
	
	private void parse() throws IllegalArgumentException {
		if(script.getChunks().size() != 2)
			throw new IllegalArgumentException("Script has wrong number of chunks to be a perfectly normal P2PKH Input script");
		
		ScriptChunk chunk;
		chunk = script.getChunks().get(0);
		
		if( (!chunk.isPushData()) || chunk.data == null || chunk.data.length < 1 || chunk.data.length > MAX_SIG_LENGTH)
			throw new IllegalArgumentException("First script chunk was not a propper signature");
		sigBytes = chunk.data;

		chunk = script.getChunks().get(1);
		if( (!chunk.isPushData()) || chunk.data == null || chunk.data.length < 1 || chunk.data.length > MAX_KEY_LENGTH)
			throw new IllegalArgumentException("Second script chunk was not a propper public key");
		pkBytes = chunk.data;
	}
}
