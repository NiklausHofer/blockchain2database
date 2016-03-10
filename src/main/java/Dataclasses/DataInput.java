package Dataclasses;

public class DataInput {

	private long tx_id;

	private boolean isCoinBase = false;

	private long prev_out_index;
	private long prev_tx_id;
	private long prev_out_id;
	private long amount;

	private OutputUpdate outputUpdate;

	public DataInput(long prev_out_index) {
		this.prev_out_index = prev_out_index;
	}

	public DataInput() {
		isCoinBase = true;
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

	public long getPrev_tx_id() {
		return prev_out_id;
	}

}
