package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockDeleter {

	private static final Logger logger = LogManager.getLogger("BlockDeleter");

	private static final String GET_BLOCK_ID = "SELECT blk_id FROM block WHERE hash = ?";
	private static final String GET_TRANSACTION_ID = "SELECT tx_id FROM transaction WHERE blk_id = ?";

	// Find the signatures used
	private static final String GET_SIGNATURE_IDS = "SELECT signature_id AS signature_id FROM multisig_signature WHERE tx_id = ?"
			+ " UNION ALL SELECT signature_id AS signature_id FROM p2sh_multisig_signatures WHERE tx_id = ?"
			+ " UNION ALL SELECT signature_id AS signature_id FROM unlock_script_p2pkh WHERE tx_id = ?"
			+ " UNION ALL SELECT signature_id AS signature_id FROM unlock_script_p2raw_pub_key WHERE tx_id = ?;";

	// delete references to the signature table
	private static final String DELETE_MULTISIG_SIGNATURE = "DELETE FROM multisig_signature WHERE tx_id = ?;";
	private static final String DELETE_P2SH_MULTISIG_SIGNATURE = "DELETE FROM p2sh_multisig_signatures WHERE tx_id = ?;";
	// signatures are NOT deduplicated in the current build, so we can just remove them without loosing any information
	private static final String DELETE_SIGNATURES = "DELETE FROM signature WHERE id = ?;";

	// delete references to the public keys. The public keys themselves we don't delete since they are deduplicated with other transactions.
	private static final String DELETE_MULTISIG_PUBKEYS = "DELETE FROM multisig_pubkeys WHERE tx_id = ?;";
	private static final String DELETE_P2SH_MULTISIG_PUBKEYS = "DELETE FROM p2sh_multisig_pubkeys WHERE tx_id = ?;";

	// Find references to scripts
	private static final String FIND_SCRIPTS = "SELECT script_id AS script_id FROM unlock_script_p2sh_other WHERE tx_id = ?"
			+ " UNION ALL SELECT redeem_script_id AS script_id FROM unlock_script_p2sh_other WHERE tx_id = ?"
			+ " UNION ALL SELECT script_id AS script_id FROM unlock_script_other WHERE tx_id = ?"
			+ " UNION ALL SELECT script_id AS script_id FROM out_script_other WHERE tx_id = ?;";

	// Delete the scripts we've just found
	private static final String DELETE_SCRIPT = "DELETE FROM script WHERE script_id = ?";

	private static final String[] DELETE_SCRIPTS = { "DELETE FROM out_script_p2pkh WHERE tx_id = ?;",
			"DELETE FROM out_script_p2sh WHERE tx_id = ?;", "DELETE FROM out_script_p2raw_pub_key WHERE tx_id = ?;",
			"DELETE FROM out_script_multisig WHERE tx_id = ?;", "DELETE FROM out_script_op_return WHERE tx_id = ?;",
			"DELETE FROM out_script_other WHERE tx_id = ?;", "DELETE FROM unlock_script_coinbase WHERE tx_id = ?;",
			"DELETE FROM unlock_script_p2pkh WHERE tx_id = ?;", "DELETE FROM unlock_script_multisig WHERE tx_id = ?;",
			"DELETE FROM unlock_script_p2raw_pub_key WHERE tx_id = ?;",
			"DELETE FROM unlock_script_other WHERE tx_id = ?;",
			"DELETE FROM unlock_script_p2sh_multisig WHERE tx_id = ?;",
			"DELETE FROM unlock_script_p2sh_other WHERE tx_id = ?;" };

	// delete the inputs and outputs
	private static final String DELETE_OUTPUTS = "DELETE FROM output WHERE tx_id = ?;";
	private static final String DELETE_INPUTS = "DELETE FROM input WHERE tx_id = ?;";

	// delete the transactions
	private static final String DELETE_TRANSACTIONS = "DELETE FROM transaction WHERE blk_id = ?;";

	// and now, finally, delete the block
	private static final String DELETE_BLOCK = "DELETE FROM block WHERE blk_id = ?;";

	private List<PreparedStatement> statements = new ArrayList<>();

	public void deleteBlock(String blockHash, DatabaseConnection connection) {

		// First, retrieve the Block ID
		long blk_id = getBlockIdFromHash(blockHash, connection);

		// Using the Block ID, retrieve a list of all Transaction IDs
		List<Long> tx_ids = getTransactionIdsFromBlockId(blk_id, connection);

		// For each transaction id...
		for (long tx_id : tx_ids) {
			// ... find the connected signatures
			// ... delete the connections to them
			// ... delete the signatures
			deleteSignatures(tx_id, connection);

			// ... delete the references to the public keys
			deletePublicKeyReferences(tx_id, connection);

			// ... find the references to the scripts
			// ... delete the scripts themselves
			deleteScripts(tx_id, connection);

			// ... delete all the input- and output scripts
			deleteInputOutputScripts(tx_id, connection);

			// ... delete the outputs, the inputs
			deleteInputsAndOutputsOfTransaction(tx_id, connection);
		}

		// Delete all transactions linked to the block
		deleteTransactions(blk_id, connection);

		// Delete the block itself
		deleteBlock(blk_id, connection);
	}

	private long getBlockIdFromHash(String blockHash, DatabaseConnection connection) {
		long blk_id = -1;

		PreparedStatement statement = connection.getPreparedStatement(GET_BLOCK_ID);
		try {
			statement.setString(1, blockHash);
			statement.executeQuery();

			ResultSet rs = statement.getResultSet();

			if (rs.next())
				blk_id = rs.getInt("blk_id");
			else
				throw new SQLException("Got no result for this block. WTF");
		} catch (SQLException e) {
			logger.fatal("Unable to get blk_id for Block with hash [ " + blockHash + "]", e);
			System.exit(1);
		}

		return blk_id;
	}

	private List<Long> getTransactionIdsFromBlockId(long blk_id, DatabaseConnection connection) {
		List<Long> tx_ids = new ArrayList<>();

		PreparedStatement statement = connection.getPreparedStatement(GET_TRANSACTION_ID);
		try {
			statement.setLong(1, blk_id);
			statement.executeQuery();

			ResultSet rs = statement.getResultSet();

			while (rs.next())
				tx_ids.add(rs.getLong("tx_id"));

		} catch (SQLException e) {
			logger.fatal("Unable to get transaction IDs for Block with id " + blk_id, e);
			System.exit(1);
		}

		return tx_ids;
	}

	private void deleteSignatures(long tx_id, DatabaseConnection connection) {
		List<Long> signature_ids = new ArrayList<Long>();

		// find the connected signatures
		PreparedStatement statement = connection.getPreparedStatement(GET_SIGNATURE_IDS);
		try {
			statement.setLong(1, tx_id);
			statement.setLong(2, tx_id);
			statement.setLong(3, tx_id);
			statement.setLong(4, tx_id);
			statement.executeQuery();

			ResultSet rs = statement.getResultSet();

			while (rs.next())
				signature_ids.add(rs.getLong("signature_id"));
		} catch (SQLException e) {
			logger.fatal("Unable to retrieve any signature IDs", e);
			System.exit(1);
		}

		// delete the links to those signatures
		PreparedStatement deleteStatement;
		try {
			deleteStatement = connection.getPreparedStatement(DELETE_MULTISIG_SIGNATURE);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();

			deleteStatement = connection.getPreparedStatement(DELETE_P2SH_MULTISIG_SIGNATURE);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to delete the connections to the signatures", e);
			System.exit(1);
		}

		// Delete the signatures
		for (long signature_id : signature_ids) {
			deleteStatement = connection.getPreparedStatement(DELETE_SIGNATURES);
			try {
				deleteStatement.setLong(1, signature_id);
				deleteStatement.executeUpdate();
			} catch (SQLException e) {
				logger.fatal("Unable to delete signature #" + signature_id);
				System.exit(1);
			}
		}

	}

	private void deletePublicKeyReferences(long tx_id, DatabaseConnection connection) {
		PreparedStatement deleteStatement;
		try {
			deleteStatement = connection.getPreparedStatement(DELETE_MULTISIG_PUBKEYS);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();

			deleteStatement = connection.getPreparedStatement(DELETE_P2SH_MULTISIG_PUBKEYS);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to delete the references to public keys", e);
			System.exit(1);
		}
	}

	private void deleteScripts(long tx_id, DatabaseConnection connection) {
		PreparedStatement statement;
		PreparedStatement deleteStatement;

		List<Long> script_ids = new ArrayList<Long>();

		try {
			statement = connection.getPreparedStatement(FIND_SCRIPTS);
			statement.setLong(1, tx_id);
			statement.setLong(2, tx_id);
			statement.setLong(3, tx_id);
			statement.setLong(4, tx_id);
			statement.executeQuery();

			ResultSet rs = statement.getResultSet();
			while (rs.next())
				script_ids.add(rs.getLong("script_id"));
		} catch (SQLException e) {
			logger.fatal("Unable to find the scripts for tx_id " + tx_id, e);
			System.exit(1);
		}

		for (long script_id : script_ids)
			try {
				deleteStatement = connection.getPreparedStatement(DELETE_SCRIPT);
				deleteStatement.setLong(1, script_id);
				deleteStatement.executeUpdate();
			} catch (SQLException e) {
				logger.fatal("Unable to delete script #" + script_id, e);
				System.exit(1);
			}
	}

	private void deleteInputOutputScripts(long tx_id, DatabaseConnection connection) {
		PreparedStatement deleteStatement;
		for (String query : DELETE_SCRIPTS) {
			deleteStatement = connection.getPreparedStatement(query);
			try {
				deleteStatement.setLong(1, tx_id);
				deleteStatement.executeUpdate();
			} catch (SQLException e) {
				logger.fatal("Unable to delete Input or Output script for Transaction #" + tx_id, e);
				System.exit(1);
			}

		}
	}

	private void deleteInputsAndOutputsOfTransaction(long tx_id, DatabaseConnection connection) {
		PreparedStatement deleteStatement;
		try {
			deleteStatement = connection.getPreparedStatement(DELETE_INPUTS);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();

			deleteStatement = connection.getPreparedStatement(DELETE_OUTPUTS);
			deleteStatement.setLong(1, tx_id);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to delete either one of either the inputs or outputs for transactoin " + tx_id, e);
			System.exit(1);
		}
	}

	private void deleteTransactions(long blk_id, DatabaseConnection connection) {
		PreparedStatement deleteStatement = connection.getPreparedStatement(DELETE_TRANSACTIONS);
		try {
			deleteStatement.setLong(1, blk_id);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to delete transactions for block " + blk_id, e);
			System.exit(1);
		}
	}

	private void deleteBlock(long blk_id, DatabaseConnection connection) {
		PreparedStatement deleteStatement = connection.getPreparedStatement(DELETE_BLOCK);
		try {
			deleteStatement.setLong(1, blk_id);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to delete block " + blk_id, e);
			System.exit(1);
		}
	}
}
