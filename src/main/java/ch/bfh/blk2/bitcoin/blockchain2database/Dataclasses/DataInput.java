package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.TransactionInput;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataInput {

	private static final Logger logger = LogManager.getLogger("DataInput");

	private long tx_id;

	private boolean isCoinBase = false;

	private long prev_tx_id;
	private long amount;
	private long tx_index;
	private TransactionInput input;
	private long input_id = -1;
	private Date date;

	String dataInputQuery = "INSERT INTO input "
			+ " (tx_id,tx_index,prev_tx_id,prev_output_index,sequence_number,amount)"
			+ " VALUES( ?, ?, ?, ?, ?, ?);";

	String outputUpdateQuery = "UPDATE output"
			+ " SET spent_by_tx = ?, spent_by_index = ?, spent_at = ?"
			+ " WHERE tx_id = ?"
			+ " AND tx_index = ?;";

	public DataInput(TransactionInput input) {
		this.input = input;
		isCoinBase = false;
	}

	public DataInput() {
		isCoinBase = true;
	}

	public void writeInput(DatabaseConnection connection) {
		if (isCoinBase)
			return;

		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(dataInputQuery);

			statement.setLong(1, tx_id);
			statement.setLong(2, tx_index);
			statement.setLong(3, prev_tx_id);
			statement.setLong(4, input.getOutpoint().getIndex());
			statement.setLong(5, input.getSequenceNumber());
			statement.setLong(6, amount);

			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();

			if (rs.next())
				input_id = rs.getLong(1);
			else {
				logger.fatal("Malformed response from Database when reading ID for a new Input");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}

			rs.close();
			statement.close();

		} catch (SQLException e) {
			logger.fatal("Failed to write Input #" + input_id + " on Transaction #" + tx_id);
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

	public void updateOutputs(DatabaseConnection connection) {
		if (isCoinBase)
			return;
		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(outputUpdateQuery);

			statement.setBoolean(1, true);
			statement.setLong(2, tx_id);
			statement.setLong(3, tx_index);
			statement.setTimestamp(4, new Timestamp(date.getTime()));
			statement.setLong(5, tx_id);
			statement.setLong(6, input.getOutpoint().getIndex());

			statement.executeUpdate();

			statement.close();
		} catch (SQLException e) {
			logger.fatal("Failed to update Output [tx: "+prev_tx_id+" , # "+input.getOutpoint().getIndex());
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setPrev_tx_id(long prev_tx_id) {
		this.prev_tx_id = prev_tx_id;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void setTx_id(long tx_id) {
		this.tx_id = tx_id;
	}

	public void setTxIndex(long tx_index){
		this.tx_index = tx_index;
	}
	
	public long getTxIndex(){
		return tx_index;
	}
}
