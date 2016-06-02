package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * This class represents a very specific type of input script. The input which the script is part of, must refer a 
 * previous output of type Pay to Script Hash. The script itself must thus contain a redeem script. We require this
 * redeem script to be a script of type Multisig.
 * 
 * @author niklaus
 *
 */
public class P2SHMultisigInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("P2SHMultisigInputScript");

	private final static int MAX_KEY_LENGTH = 65;
	private final static int MAX_SIG_LENGTH = 73;

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
	private List<byte[]> signatures;

	/**
	 * The script needs to be of a very specific format. Here it is:
	 * 
	 * <ul>
	 *   <li> The last script chunk itself must form a new script (called the redeem script)
	 *   <ul>
	 *     <li> That script must be of type Multisig </li>
	 *     <li> All the keys pushed by the Multisig redeem script must be of plausible lenght </li>
	 *     <li> No OP_N operations are permitted in the redeem script, since OP_N cannot possibly push a valid public key </li>
	 *   </ul>
	 *   </li>
	 *   <li> The signatures pushed by the unlock script must be of plausible length </li>
	 * </ul>
	 * 
	 * If any of these criteria is not matched, an IllegalArgumentException will be thrown.
	 * 
	 * @param tx_id The database id of the transaction which the script is part of
	 * @param tx_index the index within the block of the transaction which the script is part of
	 * @param script The input script. Must be of type Pay to script hash with a redeem script of type multisig
	 * @param script_size The size of the script in Byte
	 * @throws IllegalArgumentException If the script is not of the right format
	 */
	public P2SHMultisigInputScript(long tx_id, int tx_index, Script script, int script_size) throws IllegalArgumentException{
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.script = script;
		this.script_size = script_size;
		
		publicKeys = new ArrayList<byte[]>();
		signatures = new ArrayList<byte[]>();
		
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2SH_MULTISIG;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
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
		SigManager sima = SigManager.getInstance();

		int i = 0;
		for( byte[] signature: signatures ){
			long sigId = sima.saveAndGetSigId(connection, signature);

			PreparedStatement connectionStatement = connection.getPreparedStatement(CONNECTION_SIGNATURES_QUERY);

			try {
				connectionStatement.setLong(1, tx_id);
				connectionStatement.setInt(2, tx_index);
				connectionStatement.setLong(3, sigId);
				connectionStatement.setInt(4, i++);

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

			PubKeyManager pkm = PubKeyManager.getInstance();
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

			parseRedeemScript();
		} catch (ScriptException e) {
			logger.fatal("Multisig redeem Script for p2sh input #"
					+ tx_index
					+ " of transaction "
					+ tx_id
					+ " is of an unexpected format", e);
			logger.fatal(redeem_script.toString());
			System.exit(1);
		}
		
		// parse the unlock script
		for(int i=0; i< chunks.size()-1; i++){
			ScriptChunk scriptChunk = chunks.get(i);

			if( ! scriptChunk.isPushData())
				throw new IllegalArgumentException("P2SH Multisig Unlock Scripts must consist of push data operations only");

			if( scriptChunk.data == null ){ // OP_N
				byte[] arr = new byte[1];
				arr[0] = (byte) scriptChunk.decodeOpN();
				signatures.add(arr);
				continue;
			}
			
			if( scriptChunk.data.length > MAX_SIG_LENGTH)
				throw new IllegalArgumentException("P2SH Multisig Unlock Script: Data too long to be a real signature");
			
			signatures.add(scriptChunk.data);
		}
	}
	
	private void parseRedeemScript() throws IllegalArgumentException{
		if(!redeem_script.isSentToMultiSig())
			throw new IllegalArgumentException("Redeem script was not of script Type SENT TO MULTISIG");

		int redeemScriptLenght = redeem_script.getChunks().size();
		// We expect this to be a perfectly normal multisig script
		int expectedNumOfPubKeys = redeemScriptLenght - 3;
			
		for( int i=1; i <= expectedNumOfPubKeys; i++){
			ScriptChunk pkChunk = redeem_script.getChunks().get(i);
			if( ! pkChunk.isPushData())
				throw new IllegalArgumentException("Multisig Redeem Script contains non-pushdata operations in invalid positions!");
			if( pkChunk.data == null ){ // OP_N
				byte[] arr = new byte[1];
				arr[0] = (byte) pkChunk.decodeOpN();
				publicKeys.add(arr);
				continue;
			}
			if( pkChunk.data.length > MAX_KEY_LENGTH)
				throw new IllegalArgumentException("Multisig Redeem Script contains data that is clearly too long to be a public key.");

			publicKeys.add(pkChunk.data);
		}

		max = publicKeys.size();
        min = redeem_script.getChunks().get(0).decodeOpN();
	}

}
