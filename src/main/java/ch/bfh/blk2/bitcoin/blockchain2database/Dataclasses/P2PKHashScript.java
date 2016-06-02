package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

/**
 * This class represents Pay to Public Key Hash output scripts, by far the most common type of output script in Bitcoin.
 * Since the structure of the script is well known, only the Public Key Hash (including the network prefix) aka the Bitcoin
 * Address is stored to the database. The rest of the script is implied by the script type.
 * 
 * @author niklaus
 */
public class P2PKHashScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("P2PKHashScript");

	private Script script;
	private long txId;
	private int txIndex, scriptSize;

	private final static String INSERT_P2PKH_SCRIPT = "INSERT INTO out_script_p2pkh (tx_id,tx_index,script_size,public_key_id) VALUES(?,?,?,?)";

	/**
	 * @param script the output script to be used
	 * @param scriptSize the size of the output script in Byte
	 * @param txId The database id of the transaction which the script is part of
	 * @param txIndex the (database) index within the block of the transaction which the script is part of
	 */
	public P2PKHashScript(Script script, int scriptSize, long txId, int txIndex) {
		if (!script.isSentToAddress())
			throw new IllegalArgumentException("Script must be of type Pay to PubKeyHash");

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_P2PKHASH;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {

		String address = script.getToAddress(Utility.PARAMS, false).toString();

		PubKeyManager pm = PubKeyManager.getInstance();
		long pubkeyId = pm.insertPubkeyHash(connection, address);

		try {

			PreparedStatement insertScriptStatement = connection.getPreparedStatement(INSERT_P2PKH_SCRIPT);
			insertScriptStatement.setLong(1, txId);
			insertScriptStatement.setInt(2, txIndex);
			insertScriptStatement.setInt(3, scriptSize);
			insertScriptStatement.setLong(4, pubkeyId);
			insertScriptStatement.executeUpdate();
			
			insertScriptStatement.close();
		} catch (SQLException e) {
			logger.fatal("Failed to write P2PKH script");
			logger.fatal("in output [tx_id: " + txId + ", tx_index:" + txIndex + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
