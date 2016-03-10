package Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.bitcoinj.core.TransactionInput;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataInput {

	private long tx_id;

	private boolean isCoinBase = false;

	private long prev_tx_id;
	private long prev_out_id;
	private long amount;
	private TransactionInput input;
	private long input_id = -1;
	private Date date;

	private OutputUpdate outputUpdate;

	String dataInputQuery = "INSERT INTO input "
			+ " (prev_output_id, tx_id, prev_tx_id, prev_output_index, sequenze_number, amount)"
			+ " VALUES( ?, ?, ?, ?, ?, ?);";

	String outputUpdateQuery = "UPDATE output" + " SET spent = ?, spent_by_input = ?, spent_in_tx = ?, spent_at = ?"
			+ " WHERE output_id = ?;";

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

			statement.setLong(1, prev_out_id);
			statement.setLong(2, tx_id);
			statement.setLong(3, prev_tx_id);
			statement.setLong(4, input.getOutpoint().getIndex());
			statement.setLong(5, input.getSequenceNumber());
			statement.setLong(6, amount);

			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();

			if (rs.next())
				input_id = rs.getLong(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateOutputs(DatabaseConnection connection) {
		if (isCoinBase)
			return;
		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(outputUpdateQuery);

			statement.setBoolean(1, true);
			statement.setLong(2, input_id);
			statement.setLong(3, tx_id);
			statement.setTimestamp(4, new Timestamp(date.getTime()));
			statement.setLong(5, prev_out_id);

			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
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

	public void setPrev_out_id(long prev_out_id) {
		this.prev_out_id = prev_out_id;
	}

	public void setTx_id(long tx_id) {
		this.tx_id = tx_id;
	}

	public long getPrev_tx_id() {
		return prev_out_id;
	}

}
