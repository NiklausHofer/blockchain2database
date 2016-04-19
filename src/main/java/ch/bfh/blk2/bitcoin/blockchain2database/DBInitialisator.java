package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.util.PropertiesLoader;

public class DBInitialisator {

	private final static int LARGE_INPUT_SCRIPT = 9216;
	private final static int LARGE_OUTPUT_SCRIPT = 4096;

	private static final String MAX_INMEMORY_OUTPUT_SCRIPT = "max_inmemory_output_script",
			MAX_INMEMORY_INPUT_SCRIPT = "max_inmemory_input_script",

	BLOCK = "CREATE TABLE IF NOT EXISTS block("
			+ "blk_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
			+ "difficulty BIGINT,"
			+ "hash VARCHAR(64),"
			+ "prev_blk_id BIGINT,"
			+ "mrkl_root VARCHAR(64),"
			+ "time TIMESTAMP DEFAULT 0,"
			+ "height BIGINT,"
			+ "version BIGINT,"
			+ "nonce BIGINT"
			+ ")ENGINE = MEMORY;",

	TRANSACTION = "CREATE TABLE IF NOT EXISTS transaction("
			+ "tx_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
			+ "version BIGINT,"
			+ "lock_time DATETIME DEFAULT 0,"
			+ "blk_time TIMESTAMP DEFAULT 0,"
			+ "blk_id BIGINT,"
			+ "tx_hash VARCHAR(64),"
			+ "blk_index BIGINT,"
			+ "FOREIGN KEY(blk_id) REFERENCES block(blk_id)"
			+ ")ENGINE = MEMORY;",

	OUTPUT = "CREATE TABLE IF NOT EXISTS output("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "amount BIGINT,"
			+ "address VARCHAR(64),"
			+ "largescript BOOLEAN,"
			+ "spent_by_index BIGINT,"
			+ "spent_by_tx BIGINT,"
			+ "spent_at TIMESTAMP NULL,"
			+ "PRIMARY KEY(tx_id,tx_index),"
			+ "FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)"
			+ ")ENGINE = MEMORY;",

	INPUT = "CREATE TABLE IF NOT EXISTS input("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "prev_tx_id BIGINT,"
			+ "prev_output_index BIGINT,"
			+ "largescript BOOLEAN,"
			+ "sequence_number BIGINT,"
			+ "amount BIGINT,"
			+ "PRIMARY KEY(tx_id,tx_index)"
			+ ")ENGINE = MEMORY;",

	SMALL_OUT_SCRIPT_SCRIPT = "CREATE TABLE IF NOT EXISTS small_out_script("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "script_size BIGINT,"
			+ "script VARBINARY(?),"
			+ "isOpReturn BOOLEAN,"
			+ "isPayToScriptHash BOOLEAN,"
			+ "isSentToAddress BOOLEAN,"
			+ "isSentoToMultiSig BOOLEAN,"
			+ "isSentToRawPubKey BOOLEAN,"
			+ "PRIMARY KEY(tx_id,tx_index)"
			+ ")ENGINE = MEMORY;",

	LARGE_OUT_SCRIPT_SCRIPT = "CREATE TABLE IF NOT EXISTS large_out_script("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "script_size BIGINT,"
			+ "script VARBINARY(?),"
			+ "isOpReturn BOOLEAN,"
			+ "isPayToScriptHash BOOLEAN,"
			+ "isSentToAddress BOOLEAN,"
			+ "isSentoToMultiSig BOOLEAN,"
			+ "isSentToRawPubKey BOOLEAN,"
			+ "PRIMARY KEY(tx_id,tx_index)"
			+ ")ENGINE = INNODB;",

	SMALL_IN_SCRIPT_SCRIPT = "CREATE TABLE IF NOT EXISTS small_in_script("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "script_size BIGINT,"
			+ "script VARBINARY(?),"
			+ "PRIMARY KEY(tx_id,tx_index)"
			+ ")ENGINE = MEMORY;",

	LARGE_IN_SCRIPT_SCRIPT = "CREATE TABLE IF NOT EXISTS large_in_script("
			+ "tx_id BIGINT,"
			+ "tx_index BIGINT,"
			+ "script_size BIGINT,"
			+ "script VARBINARY(?),"
			+ "PRIMARY KEY(tx_id,tx_index)"
			+ ")ENGINE = INNODB;",

	PARAMETER = "CREATE TABLE IF NOT EXISTS parameter("
			+ " p_key VARCHAR(256),"
			+ " p_value VARCHAR(256) NOT NULL,"
			+ " PRIMARY KEY(p_key)"
			+ " ) ENGINE = MEMORY",

	INIT_PARAMETER = "INSERT INTO parameter (p_key,p_value)" + " VALUES ('DIRTY','true')";

	private final String INDEX_1 = "CREATE INDEX transaction_hash USING BTREE ON transaction (tx_hash);";

	private final String INDEX_2 = " CREATE INDEX output_tx_index USING BTREE ON output (tx_index);";

	private static final Logger logger = LogManager.getLogger("DBInitialisator");

	public void initializeDB() {

		DatabaseConnection dbconn = new DatabaseConnection();

		int smallInputScriptSize = 0, smallOutputScriptSize = 0;

		try {

			PropertiesLoader properties = PropertiesLoader.getInstance();
			smallInputScriptSize = Integer.parseInt(properties.getProperty(MAX_INMEMORY_INPUT_SCRIPT));
			smallOutputScriptSize = Integer.parseInt(properties.getProperty(MAX_INMEMORY_OUTPUT_SCRIPT));

			PreparedStatement ps;

			ps = dbconn.getPreparedStatement(BLOCK);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(TRANSACTION);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(OUTPUT);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(INPUT);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(SMALL_OUT_SCRIPT_SCRIPT);
			ps.setInt(1, smallOutputScriptSize);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(LARGE_OUT_SCRIPT_SCRIPT);
			ps.setInt(1, LARGE_OUTPUT_SCRIPT);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(SMALL_IN_SCRIPT_SCRIPT);
			ps.setInt(1, smallInputScriptSize);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(LARGE_IN_SCRIPT_SCRIPT);
			ps.setInt(1, LARGE_INPUT_SCRIPT);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(PARAMETER);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(INIT_PARAMETER);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(INDEX_1);
			ps.execute();
			ps.close();

			ps = dbconn.getPreparedStatement(INDEX_2);
			ps.execute();
			ps.close();

			dbconn.closeConnection();

		} catch (SQLException e) {
			e.printStackTrace();
			dbconn.closeConnection();
		}
	}

	public static void main(String[] args) {
		DBInitialisator dbi = new DBInitialisator();
		dbi.initializeDB();
	}

}
