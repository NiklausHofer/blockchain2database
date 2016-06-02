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
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Represents and output script of type Multisig, also known as m of n Multisig or bare Multisig. 
 * 
 * @author niklaus
 */
public class MultiSigScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OPReturnScript");

	private final static int MAX_KEY_LENGTH = 65;

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

	/**
	 * Create a representation of a Multisig output script, aka m-of-n multisig or bare multisig.
	 * If the passed script is not of type Multisig, or if it contains non-pushdata instructions in the list of public keys
	 * or if any of the public keys are of an invalid length, an IllegalArgumentException will be thrown.
	 * 
	 * @param script The output script. Must be of type bare multisig
	 * @param scriptSize The size (in Byte) of the script
	 * @param tx_id The database Id of the transaction which the script is part of
	 * @param tx_index The index of the transaction which the script is part of within the block (and the database)
	 * @throws IllegalArgumentException If the passed script is not of type bare-multisig or if it contains 'public keys' that are too long
	 */
	public MultiSigScript(Script script, int scriptSize, long tx_id, int tx_index) throws IllegalArgumentException{

		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;

		publicKeys = new ArrayList<>();
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_MULTISIG;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {

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

			PubKeyManager pkm = PubKeyManager.getInstance();
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
		if (!script.isSentToMultiSig())
			throw new IllegalArgumentException("Script needs to be of type Bare Multisig");

		try {
			int scriptLenght = script.getChunks().size();
			int expectedNumOfPubKeys = scriptLenght - 3;
				
			for( int i=1; i <= expectedNumOfPubKeys; i++){
				ScriptChunk pkChunk = script.getChunks().get(i);
				if( ! pkChunk.isPushData())
					throw new IllegalArgumentException("Multisig Script contains non-pushdata operations in invalid positions!");
				if( pkChunk.data == null ){ // OP_N
					byte[] arr = new byte[1];
					arr[0] = (byte) pkChunk.decodeOpN();
					publicKeys.add(arr);
					continue;
				}
				if( pkChunk.data.length > MAX_KEY_LENGTH)
					throw new IllegalArgumentException("Multisig Script contains data that is clearly too long to be a public key.");

				publicKeys.add(pkChunk.data);
			}

			max = publicKeys.size();
            min = script.getChunks().get(0).decodeOpN();
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Pay to Multisig output Script is of unexpected/ UNPARSABLE format!");
		}
	}

}
