package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class MultisigInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("MultisigInputScript");

	// Not doing this in this version
	//private final String GET_PUBKEYS = "SELECT public_key.pubkey FROM multisig_pubkeys LEFT JOIN public_key ON multisig_pubkeys.public_key_id = public_key.id WHERE multisig_pubkeys.tx_id = ? AND multisig_pubkeys.tx_index = ?;";
	private final String INSERT_QUERY = "INSERT INTO unlock_script_multisig(tx_id, tx_index, script_size) VALUES( ?, ?, ? );";
	private final String CONNECTION_QUERY = "INSERT INTO multisig_signatures(tx_id, tx_index, signature_id, idx) VALUES( ?, ?, ?, ? );";

	private long tx_id;
	private int tx_index;
	private Script script;
	private int script_size;
	private List<byte[]> signatures;

	private final static int MAX_SIG_LENGTH = 73;

	public MultisigInputScript(long tx_id, int tx_index, Script script, int script_size) throws IllegalArgumentException {
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.script = script;
		this.script_size = script_size;
		
		signatures = new ArrayList<>();
		
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_MULTISIG;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
		PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_QUERY);

		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, script_size);

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to write input script for input #" + tx_index + " of transaction with id " + tx_id, e);
			System.exit(1);
		}

		SigManager sima = SigManager.getInstance();

		int index = 0;
		for (byte[] pkBytes: signatures ){
			long sigId = sima.saveAndGetSigId(connection, pkBytes);

			PreparedStatement connectionStatement = connection.getPreparedStatement(CONNECTION_QUERY);

			try {
				connectionStatement.setLong(1, tx_id);
				connectionStatement.setInt(2, tx_index);
				connectionStatement.setLong(3, sigId);
				connectionStatement.setInt(4, index);

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
			index++;
		}

	}

	private void parse() throws IllegalArgumentException{
		for( ScriptChunk scriptChunk: script.getChunks()){
			if( ! scriptChunk.isPushData())
				throw new IllegalArgumentException("Multisig Input Scripts must consist of push operations only");

			if( scriptChunk.data == null ){ // OP_N
				byte[] arr = new byte[1];
				arr[0] = (byte) scriptChunk.decodeOpN();
				signatures.add(arr);
				continue;
			}
			
			if( scriptChunk.data.length > MAX_SIG_LENGTH)
				throw new IllegalArgumentException("Data too long to be a real signature");
			
			signatures.add(scriptChunk.data);
		}
	}
}
