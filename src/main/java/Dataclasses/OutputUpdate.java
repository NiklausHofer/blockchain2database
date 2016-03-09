package Dataclasses;

public class OutputUpdate {

    private long output_id;
    private long spent_by_out_id;
    private long spent_by_tx_id;
    private long spent_at;

    public void update() {
	String query = "UPDATE output SET spent=true," + "spent_by_input=\"" + spent_by_out_id + "\""
		+ ", spent_in_tx=\"" + spent_by_tx_id + "\"" + ", spent_at=\"" + spent_at + "\";";
    }

}
