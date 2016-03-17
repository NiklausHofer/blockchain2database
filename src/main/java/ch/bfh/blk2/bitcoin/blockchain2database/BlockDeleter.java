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

	String GET_BLOCK_ID = "SELECT blk_id FROM block WHERE hash = ?",
			GET_TRANSACTION_ID = "SELECT tx_id FROM transaction WHERE blk_id = ?",
			GET_OUTPUT_ID = "SELECT output_id FROM output WHERE tx_id = ?",
			GET_INPUT_ID = "SELECT input_id FROM input WHERE tx_id= ?",

	REMOVE_OUTPUT_SCRIPT = "DELETE FROM script WHERE output_id = ?",
			REMOVE_INPUT_SCRIPT = "DELETE FROM script WHERE input_id = ?",
			REMOVE_OUTPUT = "DELETE FROM output WHERE tx_id = ?", REMOVE_INPUT = "DELETE FROM input WHERE tx_id = ?",
			REMOVE_BLOCK = "DELETE FROM block WHERE blk_id = ?",
			REMOVE_TRANSACTION = "DELETE FROM transaction WHERE tx_id = ?",

	MARK_AS_UNSPENT = "UPDATE output"
			+ " SET spent = 0,"
			+ " spent_by_input = NULL,"
			+ " spent_in_tx = NULL,"
			+ " spent_at = NULL"
			+ " WHERE spent_in_tx = ?";

	private List<PreparedStatement> statements = new ArrayList<>();

	public void deleteBlock(String blockHash, DatabaseConnection connection) {

		try {

			//get block id
			long blkId = -1;

			PreparedStatement statement = connection.getPreparedStatement(GET_BLOCK_ID);
			statement.setString(1, blockHash);
			ResultSet result = statement.executeQuery();

			if (result.next())
				blkId = result.getLong("blk_id");
			result.close();
			statement.close();

			if (blkId < 0) {
				logger.fatal("block not in database: " + blockHash);
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

			//get transactions of this block

			ResultSet transactions = getFromDB(GET_TRANSACTION_ID, blkId, connection);

			while (transactions.next()) {

				long txId = transactions.getLong("tx_id");

				//get output and input ids and remove scripts
				ResultSet outputs = getFromDB(GET_OUTPUT_ID, txId, connection);
				while (outputs.next()) {
					long outputId = outputs.getLong("output_id");
					removeFromDB(REMOVE_OUTPUT_SCRIPT, outputId, connection);
				}
				outputs.close();

				ResultSet inputs = getFromDB(GET_INPUT_ID, txId, connection);
				while (inputs.next()) {
					long inputId = inputs.getLong("input_id");
					removeFromDB(REMOVE_INPUT_SCRIPT, inputId, connection);
				}
				inputs.close();

				//remove outputs and inputs
				removeFromDB(REMOVE_OUTPUT, txId, connection);
				removeFromDB(REMOVE_INPUT, txId, connection);

				//set outputs to unspent
				PreparedStatement unspentstatement = connection.getPreparedStatement(MARK_AS_UNSPENT);
				unspentstatement.setLong(1, txId);
				unspentstatement.executeUpdate();
				unspentstatement.close();

				//remove transaction
				removeFromDB(REMOVE_TRANSACTION, txId, connection);
			}

			//remove block
			removeFromDB(REMOVE_BLOCK, blkId, connection);
			connection.commit();

		} catch (SQLException e) {
			logger.fatal("Unable to delete Block " + blockHash);
			logger.fatal("failed at", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		} finally {
			closeStatements();
		}

		logger.info("removed block from datbase: " + blockHash);

	}

	private void closeStatements() {
		try {
			for (PreparedStatement statement : statements)
				statement.close();
			statements = new ArrayList<>();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet getFromDB(String sql, long id, DatabaseConnection connection) {

		PreparedStatement statement = connection.getPreparedStatement(sql);
		statements.add(statement);
		ResultSet result = null;
		try {
			statement.setLong(1, id);
			result = statement.executeQuery();
		} catch (SQLException e) {
			logger.fatal("Unable to execute SQL statement " + sql + " with id = " + id);
			logger.fatal("failed at: ", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		return result;
	}

	private void removeFromDB(String sql, long id, DatabaseConnection connection) {
		PreparedStatement statement = connection.getPreparedStatement(sql);
		statements.add(statement);
		try {
			statement.setLong(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.fatal("Unable to execute SQL statement " + sql + " with id = " + id);
			logger.fatal("failed at: ", e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
