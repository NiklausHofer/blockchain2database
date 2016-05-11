package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.security.interfaces.ECPublicKey;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2SHMultisigInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("P2SHMultisigInputScript");

	private final String INPUT_QUERY = "INSERT INTO unlock_script_p2sh_multisig(tx_id, tx_index, script_size, redeem_script_size, min_keys, max_keys) VALUES( ?, ?, ?, ?, ?, ? );";
	private final String CONNECT_PUBKEYS_QUERY = "INSERT INTO p2sh_multisig_pubkeys(tx_id, tx_index, public_key_id, idx) VALUES(?,?,?,?);";
	private final String CONNECTION_SIGNATURES_QUERY = "INSERT INTO p2sh_multisig_signatures(tx_id, tx_index, signature_id, idx) VALUES( ?, ?, ?, ? );";

	private long tx_id;
	private int tx_index;
	private Script script;
	private int script_size;

	private Script redeem_script;
	private int redeem_script_size;

	private int min = -1;
	private int max = -1;

	private List<byte[]> publicKeys;

	public P2SHMultisigInputScript(long tx_id, int tx_index, Script script, int script_size) {
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.script = script;
		this.script_size = script_size;
		
		publicKeys = new ArrayList<byte[]>();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2SH_MULTISIG;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
		parse();

		PreparedStatement insertStatement = connection.getPreparedStatement(INPUT_QUERY);

		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, script_size);
			insertStatement.setInt(4, redeem_script_size);
			insertStatement.setInt(5, min);
			insertStatement.setInt(6, max);

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.error("Unable to write the p2sh multisig input script for inptu #"
					+ tx_index
					+ " of transaction "
					+ tx_id, e);
			System.exit(1);
		}

		connect2pubkeys(connection);
		
		connect2signatures(connection);
	}

	private void connect2signatures(DatabaseConnection connection) {
		List<ScriptChunk> chunks = script.getChunks();
		SigManager sima = new SigManager();

		for (int i = 0; i < chunks.size() - 1; i++) {
			ScriptChunk chunk = chunks.get(i);
			if (chunk.opcode > 75) {
				logger.warn("The script of input #"
						+ tx_index
						+ " of transaction w/ id "
						+ tx_id
						+ " is supposed to be of type multisig. But it looks like this:");
				logger.warn(script.toString());
				logger.warn("Separate last chunk looks like this: " + redeem_script.toString());
			}
			byte[] chunkData = chunk.data;
			if( chunkData == null ){
				chunkData = new byte[0];
				logger.debug("P2SH Multisig unlock script with 0 data push looks like so: " + script.toString());
			}
			long sigId = sima.saveAndGetSigId(connection, chunkData);

			PreparedStatement connectionStatement = connection.getPreparedStatement(CONNECTION_SIGNATURES_QUERY);

			try {
				connectionStatement.setLong(1, tx_id);
				connectionStatement.setInt(2, tx_index);
				connectionStatement.setLong(3, sigId);
				connectionStatement.setInt(4, i);

				connectionStatement.executeUpdate();

				connectionStatement.close();
			} catch (SQLException e) {
				logger.fatal("Unable to connect input #"
						+ tx_index
						+ " of transaction "
						+ tx_id
						+ " with signature "
						+ sigId, e);
				System.exit(1);
			}
		}
	}

	private void connect2pubkeys(DatabaseConnection connection) {
		int index = 0;
		for (byte[] pkBytes : publicKeys) {
			long pubkey_id = -1;

			PubKeyManager pkm = new PubKeyManager();
			pubkey_id = pkm.insertRawPK(connection, pkBytes);

			PreparedStatement insertStatement = connection.getPreparedStatement(CONNECT_PUBKEYS_QUERY);

			try {
				insertStatement.setLong(1, tx_id);
				insertStatement.setInt(2, tx_index);
				insertStatement.setLong(3, pubkey_id);
				insertStatement.setInt(4, index++);

				insertStatement.executeUpdate();

				insertStatement.close();
			} catch (SQLException e) {
				logger.fatal("Unable to connect multisig output #"
						+ tx_index
						+ " of tx "
						+ tx_id
						+ " to publickey w/ id "
						+ pubkey_id, e);
				System.exit(1);
			}
		}
	}

	private void parse() {
		List<ScriptChunk> chunks = script.getChunks();

		try {
			// separate the redeem script
			ScriptChunk redeemScriptChunk = chunks.get(chunks.size() - 1);
			redeem_script_size = redeemScriptChunk.data.length;
			redeem_script = new Script(redeemScriptChunk.data);

			int scriptLenght = redeem_script.getChunks().size();
			// We expect this to be a perfectly normal multisig script
			int expectedNumOfPubKeys = scriptLenght - 3;
				
			for( int i=1; i <= expectedNumOfPubKeys; i++)
				publicKeys.add(redeem_script.getChunks().get(i).data);

			max = publicKeys.size();
			//min = redeem_script.getNumberOfSignaturesRequiredToSpend();
            min = redeem_script.getChunks().get(0).decodeOpN();
		} catch (ScriptException e) {
			logger.fatal("Multisig redeem Script for p2sh input #"
					+ tx_index
					+ " of transaction "
					+ tx_id
					+ " is of an unexpected format", e);
			logger.fatal(redeem_script.toString());
			System.exit(1);
		}
	}

}
