package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

public class OutputUpdate {

	private long output_id;
	private long spent_by_input_id;
	private long spent_by_tx_id;
	private long spent_at;

	public OutputUpdate(long outputId) {
		this.output_id = outputId;
	}

	public void update() {
		// String query = "UPDATE output SET spent=true," + "spent_by_input=\""
		// + spent_by_out_id + "\""
		// + ", spent_in_tx=\"" + spent_by_tx_id + "\"" + ", spent_at=\"" +
		// spent_at + "\";";
	}

}
