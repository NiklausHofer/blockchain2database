package Dataclasses;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class DataBlock {

    private static final Logger logger = LogManager.getLogger("DataBlock");

    private Block block;
    private int height;
    private long prevBlockId = -1;
    private long blockId;
    private NetworkParameters params;
    private DatabaseConnection connection;

    private String insertBlockQuery = "INSERT INTO block"
	    + " (difficulty, hash, prev_blk_id, mkrl_root, time, transaction_count, height, version, nonce)"
	    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private String updateBlockQuery = "UPDATE block" + " SET output_amount = ?, input_amount = ?"
	    + " WHERE blk_id = ?;";

    private String getPrevBlockIdQuery = "SELECT block_id FROM block WHERE block_hash = ?;";

    public DataBlock(Block block, NetworkParameters params, DatabaseConnection connection, int height,
	    long prevBlockId) {

	this.block = block;
	this.connection = connection;
	this.height = height;
	this.prevBlockId = prevBlockId;

    }

    // /*
    // * Get the required data from the database
    // */
    // private void getData() {
    // PreparedStatement statement = (PreparedStatement)
    // connection.getPreparedStatement(getPrevBlockIdQuery);
    //
    // try {
    // // Don't do this for the genesis block
    // if (block.getPrevBlockHash().equals(params.getGenesisBlock().getHash()))
    // return;
    // else
    // statement.setString(1, block.getPrevBlockHash().toString());
    //
    // statement.execute();
    // ResultSet result = statement.getResultSet();
    //
    // prevBlockId = result.getLong(1);
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }

    /**
     * Inserts the block into the database. Leaves the "amount" Fields empty.
     */
    public void writeBlock() {
	// getData();

	try {
	    PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(insertBlockQuery);

	    statement.setLong(1, block.getDifficultyTarget()); // difficulty
	    statement.setString(2, block.getHashAsString()); // hash
	    // special treatment for genesis Block
	    if (prevBlockId >= 0)
		statement.setLong(3, prevBlockId); // prevBlockId
	    else
		statement.setNull(3, java.sql.Types.NULL);
	    statement.setString(4, block.getMerkleRoot().toString()); // mrkl_root
	    statement.setTimestamp(5, new java.sql.Timestamp(block.getTime().getTime())); // time
	    statement.setLong(6, block.getTransactions().size()); // transaction_count
	    statement.setLong(7, height); // height
	    statement.setLong(8, block.getVersion()); // version
	    statement.setLong(9, block.getNonce()); // Nonce

	    statement.executeUpdate();

	    ResultSet rs = statement.getGeneratedKeys();

	    if (rs.next())
		blockId = rs.getLong(1);
	    else {
		blockId = -1;
		logger.warn("Bad generatedKeySet from Block " + block.getHashAsString());
	    }

	    rs.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	logger.info("Writing Block " + block.getHashAsString());

    }

    public void updateAmounts(int totalIn, int totalOut) {
	String query = "UPDATE blocks SET ... = ... WHERE block_id = " + blockId + ";";
	// TODO run
    }

    public long getId() {
	return blockId;
    }
}
