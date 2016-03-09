package Dataclasses;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;

public class DataBlock {

    private Block block;
    private String block_hash;
    private String prev_block_hash;
    private int prevBlockId;
    private int blockId;
    NetworkParameters params;

    private String insertBlockQuery = "INSERT INTO block"
	    + " (magic_id, difficulty, hash, prev_blk_id, mkrl_root, time, transaction_count, height, version, nonce)"
	    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private String updateBlockQuery = "UPDATE block" + " SET output_amount = ?, input_amount = ?"
	    + " WHERE blk_id = ?;";

    private String getPrevBlockIdQuery = "SELECT block_id FROM block WHERE block_hash = ?;";

    public DataBlock(Block block, NetworkParameters params) {

	this.block = block;

	block_hash = block.getHash().toString();
	prev_block_hash = block.getPrevBlockHash().toString();

    }

    /*
     * Get the required data from the database
     */
    private void getData() {
	if (block.getPrevBlockHash().equals(params.getGenesisBlock().getHash())) {
	    prevBlockId = -1;
	    return;
	}
	String query = "SELECT block_id FROM blocks WHERE block_hash = \"" + prev_block_hash + "\";";
    }

    /**
     * Inserts the block into the database. Leaves the "amount" Fields empty.
     */
    public void writeBlock() {
	getData();

	if (prevBlockId < 0) {
	    // This is the genesis block. Special case. No prev block Id. Insert
	    // NULL instead.
	}

	String query = "INSERT INTO...";
	// TODO run

	// TODO retrieve the blockID and store it
    }

    public void updateAmounts(int totalIn, int totalOut) {
	String query = "UPDATE blocks SET ... = ... WHERE block_id = " + blockId + ";";
	// TODO run
    }

    public int getId() {
	return blockId;
    }
}
