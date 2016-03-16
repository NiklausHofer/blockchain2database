package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.TestNet3Params;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataBlock;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataTransaction;
import ch.bfh.blk2.bitcoin.producer.BlockProducer;
import ch.bfh.blk2.bitcoin.producer.BlockSorter;
import ch.bfh.blk2.bitcoin.util.Utility;

public class FooClass {
	private static final Logger logger = LogManager.getLogger("FooClass");

	private BlockProducer blockProducer;
	private Context context;
	private NetworkParameters params;
	private DatabaseConnection connection;

	private final static String COUNTQUERY = "SELECT COUNT(*) FROM block;";

	private final static String HIGHESTBLOCKQUERY = "SELECT MAX(height) FROM block;";

	private final static String GETBLKHASHFROMHEIGHTQUERY = "SELECT hash FROM block WHERE height = ?;";

	private final static String GETBLOCKIDQUERY = "SELECT id FROM block WHERE hash = ?;";

	public static void main(String[] args) {

		FooClass foo = new FooClass();
		foo.start();
	}

	public FooClass() {

		// Init BitcoinJ
		// params = new MainNetParams();
		params = new TestNet3Params();
		context = Context.getOrCreate(params);

		// database connection
		connection = new DatabaseConnection();

		//blockProducer = new BlockProducer(Utility.getDefaultFileList(), 1);
	}

	public void start() {
		// Find out if any blocks are in the database at all
		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(COUNTQUERY);
			statement.executeQuery();
			ResultSet rs = statement.getResultSet();

			if (rs.next()) {
				if (rs.getInt(1) < 1) {
					// Database still empty. Let's fill it up
					logger.info("No blocks in Database yet. Will now start inserting");
					this.generateDatabase();
				} else {
					// Stuff is already in the database
					logger.info("Found Blocks in the database. Will attempt to continue inserting new blocks");
					continueInsert();
				}
			} else {
				logger.error("Unable to query databae");
				System.exit(1);
			}

			rs.close();
			statement.close();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void continueInsert() {
		// Get a list of all valid blocks
		BlockSorter blockSorter = new BlockSorter(Utility.getDefaultFileList());
		List<Sha256Hash> validChain = blockSorter.getLongestBranch();

		// Get the height of the chain in our database
		int dbMaxHeight = getChainHeight();

		if (validChain.size() < dbMaxHeight + 1) {
			logger.fatal(
					"The chain in the database is longer than the one on disk. Something is very likely wrong. Aborting");
			System.exit(1);
		}

		// Check if there are Orphan blocks in the database
		List<Sha256Hash> orphanBlocks = new ArrayList<>();
		Sha256Hash blkHash;
		blkHash = getBlockHashAtHeight(dbMaxHeight);

		while (!validChain.contains(blkHash)) {
			logger.warn("Found an orphan block in the database");
			orphanBlocks.add(blkHash);
			dbMaxHeight--;
			blkHash = getBlockHashAtHeight(dbMaxHeight);
		}

		// If no orphan Blocks have been detected
		if (orphanBlocks.isEmpty()) {
			if (validChain.size() == dbMaxHeight + 1) {
				logger.info("Chain in the database appears to be in order and up to date. Nothing to do for now");
				System.exit(0);
			}
			logger.info("No orphan Blocks found. Will attempt to update the database now");
			updateDatabase();

			return;
		}

		// If orphan Blocks have been detected
		if (orphanBlocks.size() > 2) {
			logger.warn("More than two orphan Blocks have been found. The user will be informed about this");
			System.out.println("More than two (2) orphan Blocks have been found on the database.\n"
					+ "This is suspicious. They will be deleted. To continue, type YES");

			Scanner scanner = new Scanner(System.in);
			String unserInput = scanner.next();
			if (!unserInput.contains("YES")) {
				logger.info("The user chose to abort, rather than delete a long chain of orphan blocks");
				System.out.println("Goodbye!");
				System.exit(0);
			}
		}

		for (Sha256Hash hash : orphanBlocks) {
			// TODO delete
		}

		simpleUpdateDatabase(dbMaxHeight, validChain);

	}

	private void updateDatabase() {
		// TODO
	}

	private void simpleUpdateDatabase(int currentHeight, List<Sha256Hash> validChain) {
		Sha256Hash lastBlkHash = getBlockHashAtHeight(currentHeight);
		long prevId = getBlockId(lastBlkHash);

		blockProducer = new BlockProducer(Utility.getDefaultFileList(), validChain, 1);

		Iterator<Block> blockIterator = blockProducer.iterator();

		while (blockIterator.hasNext() && !blockIterator.next().getPrevBlockHash().equals(lastBlkHash)) {
		}

		logger.info("After deleting orphan Blocks, we are now ready again to continue inserting Blocks.\n"
				+ "We will continue from Block "
				+ lastBlkHash.toString());

		while (blockIterator.hasNext()) {
			currentHeight++;
			Block blk = blockIterator.next();
			prevId = writeBlock(blk, currentHeight, prevId);
			connection.commit();
		}
	}

	private long getBlockId(Sha256Hash blkHash) {
		long id = -1;
		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(GETBLOCKIDQUERY);
			statement.setString(1, blkHash.toString());
			statement.executeQuery();
			ResultSet rs = statement.getResultSet();
			if (rs.next())
				id = rs.getLong(1);
			else {
				logger.fatal("Unable to interprete result from DB Query");
				System.exit(1);
			}

			rs.close();
			statement.close();
		} catch (SQLException e) {
			logger.fatal("Error while trying to get blk_id from blkHash");
			logger.fatal(e);
			System.exit(1);
		}

		return id;
	}

	private Sha256Hash getBlockHashAtHeight(long height) {
		Sha256Hash hash = null;

		try {
			PreparedStatement statement = (PreparedStatement) connection
					.getPreparedStatement(GETBLKHASHFROMHEIGHTQUERY);
			statement.setLong(1, height);
			statement.executeQuery();
			ResultSet rs = statement.getResultSet();

			if (rs.next())
				try {
					hash = Sha256Hash.wrap(rs.getString(1));
				} catch (IllegalArgumentException e) {
					logger.fatal("The block in the database appears to have an invalid Hash. Height: "
							+ height
							+ " Hash: "
							+ rs.getString(1));
					logger.fatal(e);
					System.exit(1);
				}
			else {
				logger.fatal("Unable to interprete result from Database");
				System.exit(1);
			}

			rs.close();
			statement.close();
		} catch (SQLException e) {
			logger.fatal("Error while trying to get Blkhash from Blkheight");
			logger.fatal(e);
			System.exit(1);
		}
		return hash;
	}

	private int getChainHeight() {
		int height = -1;

		try {
			PreparedStatement statement = (PreparedStatement) connection.getPreparedStatement(HIGHESTBLOCKQUERY);
			statement.executeQuery();
			ResultSet rs = statement.getResultSet();
			if (rs.next())
				height = rs.getInt(1);
			else {
				logger.fatal("Unable to interprete result from Database");
				System.exit(1);
			}

			rs.close();
			statement.close();
		} catch (SQLException e) {
			logger.fatal("Error while trying to get maximum block height");
			logger.fatal(e);
			System.exit(1);
		}

		return height;
	}

	private void generateDatabase() {

		int height = 0;
		long prevId = -1;

		blockProducer = new BlockProducer(Utility.getDefaultFileList(), 1);

		long startTime = System.currentTimeMillis();
		long numOfTransactions = 0;

		for (Block block : blockProducer) {
			prevId = writeBlock(block, height++, prevId);
			connection.commit();
			numOfTransactions += block.getTransactions().size();
			if (height % 100 == 0) {
				double duration = (System.currentTimeMillis() - startTime) / 1000.0;
				logger.info("Inserted "
						+ numOfTransactions
						+ " in "
						+ duration
						+ " seconds. That's about "
						+ (duration / numOfTransactions)
						+ " seconds per transaction.\n"
						+ "\tInserting 100K transactions takes approx "
						+ (duration / numOfTransactions * 100000.0)
						+ " seconds");
				startTime = System.currentTimeMillis();
			}
		}

	}

	private long writeBlock(Block block, int height, long prevId) {
		long totalIn = 0;
		long totalOut = 0;
		long startTime = System.currentTimeMillis();

		DataBlock dataBlock = new DataBlock(block, params, connection, height, prevId);
		dataBlock.writeBlock();

		for (Transaction transaction : block.getTransactions()) {
			DataTransaction dataTransaction = new DataTransaction(transaction, dataBlock.getId(), connection,
					block.getTime());
			dataTransaction.writeTransaction();

			totalIn += dataTransaction.getInAmount();
			totalOut += dataTransaction.getOutAmount();
		}

		dataBlock.updateAmounts(totalIn, totalOut);

		logger.info("Inserted Block "
				+ height
				+ " with Hash "
				+ block.getHashAsString()
				+ ". Inserting "
				+ block.getTransactions().size()
				+ " transactions. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0)
				+ " Seconds.");

		return dataBlock.getId();

		// right at the end of the loop...
		// dataBlock.updateAmounts(totalIn, totalOut);
	}

}
