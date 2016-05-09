package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class MultiSigScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OPReturnScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;
	private int min = -1;
	private int max = -1;

	//List<ECKey> publickeys;
	private List<byte[]> publicKeys;

	private final String insertQuery = "INSERT INTO out_script_multisig(tx_id, tx_index, script_size, min_keys, max_keys) VALUES( ?, ?, ?, ?, ? );";
	private final String insertConnectionQuery = "INSERT INTO multisig_pubkeys(tx_id, tx_index, public_key_id, idx) VALUES(?,?,?,?);";

	public MultiSigScript(Script script, int scriptSize, long tx_id, int tx_index) {
		if (!script.isSentToMultiSig())
			throw new IllegalArgumentException("Script needs to be of type Bare Multisig");

		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;

		publicKeys = new ArrayList<>();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_MULTISIG;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		parse();

		PreparedStatement insertStatement = connection.getPreparedStatement(insertQuery);
		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, scriptSize);
			insertStatement.setInt(4, min);
			insertStatement.setInt(5, max);
			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal(
					"Unable to insert the output (of type multisig) #" + tx_index + " of transaction with id " + tx_id,
					e);
			System.exit(1);
		}

		connect2pubkeys(connection);
	}

	private void connect2pubkeys(DatabaseConnection connection) {
		int index = 0;
		for (byte[] key : publicKeys) {
			//byte[] pubKeyBytes = key.getPubKey();
			long pubkey_id = -1;

			PubKeyManager pkm = new PubKeyManager();
			pubkey_id = pkm.insertRawPK(connection, key);

			PreparedStatement insertStatement = connection.getPreparedStatement(insertConnectionQuery);

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
		try {
			try{
				// see if BitcoinJ/Bouncycastle are able to propperly cast the addresses
				List<ECKey> ecPubKeys = script.getPubKeys();
				for(ECKey ecKey: ecPubKeys)
					publicKeys.add(ecKey.getPubKey());
			} catch (IllegalArgumentException e){
				logger.debug("Unable to decode the public keys for multisig output #" + tx_index + " of transaction " + tx_id + ". The script is: " + script.toString(), e);
				logger.debug("Will attempt to manually extract the byte sequences");
				
				int scriptLenght = script.getChunks().size();
				// We expect this to be a perfectly normal multisig script
				int expectedNumOfPubKeys = scriptLenght - 3;
				
				for( int i=1; i <= expectedNumOfPubKeys; i++)
					publicKeys.add(script.getChunks().get(i).data);
			}
			max = publicKeys.size();
			min = script.getNumberOfSignaturesRequiredToSpend();
		} catch (ScriptException e) {
			logger.fatal("Multisig Script for output #"
					+ tx_index
					+ " of transaction "
					+ tx_id
					+ " is of an unexpected format", e);
			logger.fatal(script.toString());
			System.exit(1);
		} catch (IllegalArgumentException e){
			System.exit(1);
		}
	}

}
