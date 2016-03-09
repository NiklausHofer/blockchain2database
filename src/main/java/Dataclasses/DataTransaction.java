package Dataclasses;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

public class DataTransaction {

    private int blockId;
    private long outAmount;
    private Transaction transaction;

    public DataTransaction(Transaction transaction, int blockId) {
	this.transaction = transaction;
	this.blockId = blockId;
    }

    private void calcOutAmount() {
	outAmount = 0;
	for (TransactionOutput output : transaction.getOutputs())
	    outAmount += output.getValue().getValue();
    }

    private void fetchInputInformation() {
	for (TransactionInput input : transaction.getInputs()) {
	    int prev_out_id;
	    String query_1 = "SELECT tx_id" + " FROM transaction" + " WHERE tx_hash = \""
		    + input.getOutpoint().getHash().toString() + "\";";

	    // TODO run
	    // TODO put value into prev_out_id
	    prev_out_id = 42;

	    String query_2 = "SELECT amount, output_id" + " FROM output" + " WHERE output_id = \"" + prev_out_id + "\""
		    + " AND output_index = \"" + input.getOutpoint().getIndex() + "\"";
	}
    }

}
