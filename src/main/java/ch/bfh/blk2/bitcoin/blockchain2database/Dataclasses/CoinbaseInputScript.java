package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class CoinbaseInputScript implements InputScript {

	private static final Logger logger = LogManager.getLogger("CoinbaseInputScript");

	private final static String INSERT_COINBASE_SCRIPT = "INSERT INTO unlock_script_coinbase (tx_id,tx_index,script_size,information) VALUES (?,?,?,?)";

	private int txIndex, scriptSize;
	private long txId;
	private byte[] script;

	public CoinbaseInputScript(byte[] script, long txId, int txIndex, int scriptSize) {

		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
		this.script = script;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_COINBASE;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		try {

			PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_COINBASE_SCRIPT);
			insertStatement.setLong(1, txId);
			insertStatement.setInt(2, txIndex);
			insertStatement.setInt(3, scriptSize);
			insertStatement.setBytes(4, script);

			insertStatement.executeUpdate();
			insertStatement.close();

		} catch (SQLException e) {
			logger.fatal("faild to insert Input script of type Coinbase", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
