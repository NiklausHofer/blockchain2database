package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2RawPubKeyInputscript implements InputScript {

	private static final Logger logger = LogManager.getLogger("P2RawPubKeyInputscript");

	private final String INPUT_QUERY = "INSERT INTO unlock_script_p2raw_pub_key(tx_id, tx_index, script_size, signature_id) VALUES ( ?, ?, ? ,? );;";

	private final static int MAX_SIG_LENGTH = 73;

	private long tx_id;
	private int tx_index;
	private Script script;
	private int script_size;
	
	private byte[] sigBytes;

	public P2RawPubKeyInputscript(long tx_id, int tx_index, Script script, int script_size) throws IllegalArgumentException{
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.script = script;
		this.script_size = script_size;
		
		parse();
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2RAWPUBKEY;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		SigManager sigma = SigManager.getInstance();

		long sigId = sigma.saveAndGetSigId(connection, sigBytes);

		PreparedStatement insertStatement = connection.getPreparedStatement(INPUT_QUERY);

		try {
			insertStatement.setLong(1, tx_id);
			insertStatement.setInt(2, tx_index);
			insertStatement.setInt(3, script_size);
			insertStatement.setLong(4, sigId);

			insertStatement.executeUpdate();

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal(
					"Unable to insert p2raw pub key input script for input #" + tx_index + " of transaction " + tx_id,
					e);
			System.exit(1);
		}
	}
	
	private void parse() throws IllegalArgumentException{
		if( script.getChunks().size() != 1)
			throw new IllegalArgumentException("Pay to Raw Pub Key unlock script must consist of exactly one pushdata operation.");
		if(script.getChunks().get(0).isPushData())
			throw new IllegalArgumentException("Pay to Raw Pub Key unlock script must consist of exactly one pushdata operation.");
		if(script.getChunks().get(0).data == null)
			throw new IllegalArgumentException("OP_N operation cannot possibly push a valid signature for anything.");
		if(script.getChunks().get(0).data.length > MAX_SIG_LENGTH)
			throw new IllegalArgumentException("Data too long to be a real signature");
		
		sigBytes = script.getChunks().get(0).data;
	}

}
