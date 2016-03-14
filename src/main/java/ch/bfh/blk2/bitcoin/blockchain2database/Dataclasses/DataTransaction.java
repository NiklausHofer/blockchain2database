package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class DataTransaction {

	private static final Logger logger = LogManager.getLogger("DataTransaction");

	private long blockId;
	private long outAmount;
	private long inAmount;
	private Transaction transaction;
	private DatabaseConnection connection;
	private Date date;
	private List<DataInput> dataInputs;
	private long tx_id;

	//private String inputAmountQuery_1 = "SELECT tx_id FROM transaction WHERE tx_hash = ?;";
	//private String inputAmountQuery_2 = "SELECT amount, output_id FROM output WHERE tx_id = ? AND output_index = ?;";
	private String inputAmountQuery_3 = "SELECT transaction.tx_id, output.amount, output.output_id FROM transaction RIGHT JOIN output ON transaction.tx_id = output.tx_id WHERE transaction.tx_hash = ? AND output.tx_index = ?;";

	private String transactionInsertQuery = "INSERT INTO transaction"
			+ " (version, lock_time, blk_time, input_count, output_count, output_amount, input_amount, coinbase, blk_id, tx_hash) "
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private String outputInsertQuery = "INSERT INTO output" + " (amount, tx_id, tx_index, spent,addr_id)"
			+ " VALUES( ?, ?, ?, ? , ? );";

	public DataTransaction(Transaction transaction, long blockId, DatabaseConnection connection, Date date) {
		this.transaction = transaction;
		this.blockId = blockId;
		this.connection = connection;
		this.date = date;

		dataInputs = new ArrayList<>();
		inAmount = 0;
		outAmount = 0;
	}

	public void writeTransaction() {
		calcOutAmount();
		calcInAmount();

		tx_id = -1;

		try {
			PreparedStatement transactionInsertStatement = (PreparedStatement) connection
					.getPreparedStatement(transactionInsertQuery);

			transactionInsertStatement.setLong(1, transaction.getVersion());
			transactionInsertStatement.setTimestamp(2, new java.sql.Timestamp(transaction.getLockTime() * 1000 + 1000));
			transactionInsertStatement.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
			transactionInsertStatement.setLong(4, transaction.getInputs().size());
			transactionInsertStatement.setLong(5, transaction.getOutputs().size());
			transactionInsertStatement.setLong(6, outAmount);
			transactionInsertStatement.setLong(7, inAmount);
			transactionInsertStatement.setBoolean(8, transaction.isCoinBase());
			transactionInsertStatement.setLong(9, blockId);
			transactionInsertStatement.setString(10, transaction.getHashAsString());

			transactionInsertStatement.executeUpdate();

			ResultSet rs = transactionInsertStatement.getGeneratedKeys();

			if (rs.next())
				tx_id = rs.getLong(1);
			else
				logger.warn("Bad generatedKeySet from Transaction " + transaction.getHashAsString());

		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (DataInput dataInput : dataInputs) {
			dataInput.setTx_id(tx_id);
			dataInput.writeInput(connection);
		}

		for (TransactionOutput output : transaction.getOutputs())
			writeOutput(output);

		for (DataInput dataInput : dataInputs) {
			dataInput.setDate(date);
			dataInput.updateOutputs(connection);
		}

	}

	private void writeOutput(TransactionOutput output) {

		long addr_id=-1;
		
		try{
			
			//Get Adress ID
			Address address = Utility.getAddressFromOutput(output, new MainNetParams());
			AddressUpdater addressUpdater = new AddressUpdater(address);
			addr_id = addressUpdater.update(connection);
			
		}
		catch(ScriptException e){
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Invalid Address: error" + e.getClass());
		}
		
		try{
			
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(outputInsertQuery);
			
			statement.setLong(1, output.getValue().getValue());
			statement.setLong(2, tx_id);
			statement.setLong(3, output.getIndex());
			statement.setBoolean(4, false);
			statement.setLong(5, addr_id);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void calcOutAmount() {
		for (TransactionOutput output : transaction.getOutputs())
			outAmount += output.getValue().getValue();
	}

	private void calcInAmount() {
		for (TransactionInput input : transaction.getInputs()) {

			if (input.isCoinBase()) {
				// Coinbase Input
				DataInput dataInput = new DataInput();
				dataInputs.add(dataInput);
				// TODO
				return;
			}

			DataInput dataInput = new DataInput(input);
			dataInputs.add(dataInput);

			try {
				PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(inputAmountQuery_3);
				statement.setString(1, input.getOutpoint().getHash().toString());
				statement.setLong(2, input.getOutpoint().getIndex());

				ResultSet rs = statement.executeQuery();

				if (rs.next()) {
					dataInput.setPrev_tx_id(rs.getLong(1));
					dataInput.setAmount(rs.getLong(2));
					dataInput.setPrev_out_id(rs.getLong(3));

					inAmount += rs.getLong(2);
				} else
					logger.error("The transaction/output reffered to by one of " + transaction.getHashAsString()
							+ " Inputs can not be found\n" + "The specific output we were looking for  is: "
							+ input.getOutpoint().getHash().toString() + " index " + input.getOutpoint().getIndex());
				//		PreparedStatement statement_1 = (PreparedStatement) connection.getPreparedStatement(inputAmountQuery_1);
				//
				//		statement_1.setString(1, transaction.getHashAsString());
				//
				//		ResultSet rs_1 = statement_1.executeQuery();
				//
				//		if (rs_1.next())
				//		    dataInput.setPrev_tx_id(rs_1.getLong(1));
				//		else {
				//		    logger.warn("The transaction refered to by " + transaction.getHashAsString() + " Input "
				//			    + input.getSequenceNumber()
				//			    + " Was not Found.\nThere is a good chance, that your dataset is corrupt!");
				//		    return;
				//		}
				//
				//		PreparedStatement statement_2 = (PreparedStatement) connection.getPreparedStatement(inputAmountQuery_2);
				//
				//		statement_2.setLong(1, dataInput.getPrev_tx_id());
				//		statement_2.setLong(2, input.getOutpoint().getIndex());
				//
				//		ResultSet rs_2 = statement_2.executeQuery();
				//
				//		if (rs_2.next()) {
				//		    dataInput.setAmount(rs_2.getLong(1));
				//		    dataInput.setPrev_out_id(rs_2.getLong(2));
				//		} else {
				//		    logger.warn("Could not find the output refered to by" + transaction.getHashAsString() + " Input "
				//			    + input.getSequenceNumber() + ".\nThere's a good chance, that your dataset is corrupt!");
				//		    return;
				//		}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public long getInAmount() {
		return inAmount;
	}

	public long getOutAmount() {
		return outAmount;
	}

}
