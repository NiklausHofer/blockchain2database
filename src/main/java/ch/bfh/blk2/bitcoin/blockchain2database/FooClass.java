package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import com.mysql.jdbc.PreparedStatement;

import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataBlock;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataTransaction;
import ch.bfh.blk2.bitcoin.producer.BlockProducer;
import ch.bfh.blk2.bitcoin.producer.BlockSorter;
import ch.bfh.blk2.bitcoin.util.BlockFileList;
import ch.bfh.blk2.bitcoin.util.DBKeyStore;
import ch.bfh.blk2.bitcoin.util.PropertiesLoader;
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

	private final static String GETBLOCKIDQUERY = "SELECT blk_id FROM block WHERE hash = ?;";

	public static void main(String[] args) {

		FooClass foo = new FooClass();
		foo.start();
	}

	public FooClass() {

		// Init BitcoinJ

		if (Boolean.parseBoolean(PropertiesLoader.getInstance().getProperty("testnet"))) {
			params = new TestNet3Params();
			logger.info("Operating on Testnet3");
		} else {
			params = new MainNetParams();
			logger.info("Operating on MainNet");
		}
		Utility.setParams(params);
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
					BlockSorter blockSorter = new BlockSorter(new BlockFileList());
					this.insertChain(-1, blockSorter.getLongestBranch());
				} else {
					// Stuff is already in the database
					logger.info("Found Blocks in the database. Will attempt to continue inserting new blocks");
					continueInsert();
				}
			} else {
				logger.fatal("Unable to query databae");
				connection.closeConnection();
				System.exit(1);
			}

			rs.close();
			statement.close();

		} catch (SQLException e) {
			e.printStackTrace();
			connection.closeConnection();
			System.exit(1);
		}

	}

	private void continueInsert() {
		// Get the height of the chain in our database
		int dbMaxHeight = getChainHeight();
		Sha256Hash blkHash = getBlockHashAtHeight(dbMaxHeight);

		// Get the hash of 30 Blocks back...
		Sha256Hash saveHash = getBlockHashAtHeight(dbMaxHeight - 30);

		// Generate the chain from that block on
		BlockFileList bflist = new BlockFileList(dbMaxHeight - 30, saveHash);
		BlockSorter blockSorter = new BlockSorter(bflist);
		List<Sha256Hash> validChain = blockSorter.getLongestBranch();

		logger.debug("dbMaxheight:\t" + dbMaxHeight);
		logger.debug("dbMaxHeight - 30:\t" + (dbMaxHeight - 30));
		logger.debug("validChain size\t:" + validChain.size());

		if (dbMaxHeight - 30 + validChain.size() < dbMaxHeight) {
			logger.fatal(
					"The chain in the database is longer than the one on disk. Something is very likely wrong. Aborting");
			System.exit(1);
		}
		if (dbMaxHeight - 30 + validChain.size() == dbMaxHeight) {
			logger.info("Chain in the database appears to be in order and up to date. Nothing to do for now");
			System.exit(0);
		}

		// Check if there are Orphan blocks in the database
		List<Sha256Hash> orphanBlocks = new ArrayList<>();

		while (!validChain.contains(blkHash)) {
			logger.warn("Found an orphan block in the database");
			orphanBlocks.add(blkHash);
			dbMaxHeight--;
			blkHash = getBlockHashAtHeight(dbMaxHeight);
		}

		if (orphanBlocks.isEmpty()) {
			// If no orphan Blocks have been detected
			logger.info("No orphan Blocks found. Will attempt to update the database now");
			dbMaxHeight = checkOnDirtyFlag(dbMaxHeight);
		} else {
			// If orphan Blocks have been detected

			if (orphanBlocks.size() > 2) {
				// In case a suspiciously large amount of orphan blocks have been detected, ask user
				// for consent before deleting them
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

			// Delete the orphan blocks
			BlockDeleter blockDeleter = new BlockDeleter();
			for (Sha256Hash hash : orphanBlocks)
				blockDeleter.deleteBlock(hash.toString(), connection);

			//simpleUpdateDatabase(dbMaxHeight, validChain);
		}

		// Remove the duplicate block hashes from the list
		Sha256Hash lastBlkHash = getBlockHashAtHeight(dbMaxHeight);
		int index = validChain.indexOf(lastBlkHash);
		if (index < 0) {
			logger.fatal("An unexpected error occured. The authors of this software were morons!");
			System.exit(1);
		}
		for (int i = 0; i <= index; i++)
			validChain.remove(0);

		insertChain(dbMaxHeight, validChain);
	}

	private int checkOnDirtyFlag(int currentHeight) {
		//check if DB is in a dirty condition
		// last block might not be inserted completly

		DBKeyStore keyStore = new DBKeyStore();
		boolean dirty = Boolean.parseBoolean(keyStore.getParameter(connection, DBKeyStore.DYRTY));

		if (dirty) {
			// delete last block which might be incomplete

			logger.debug("DIRTY flag is set in DB");
			Sha256Hash lastBlkHash = getBlockHashAtHeight(currentHeight);

			logger.debug("Will try to delete block at height "
					+ currentHeight
					+ " which we found to be Block "
					+ lastBlkHash);

			//set flag as soon as DB is manipulated
			keyStore = new DBKeyStore();
			keyStore.setParameter(connection, DBKeyStore.DYRTY, "true");

			BlockDeleter blockDeleter = new BlockDeleter();
			blockDeleter.deleteBlock(lastBlkHash.toString(), connection);

			currentHeight--;
		} else {
			logger.debug("DIRTY flag not set in DB");
			logger.debug("last Block was inserted completly and will not be deleted");
		}

		// resume inserting
		return currentHeight;
	}

	private void insertChain(int currentHeight, List<Sha256Hash> validChain) {
		// set dirty flag while DB will be manipulated
		DBKeyStore keyStore = new DBKeyStore();
		keyStore.setParameter(connection, DBKeyStore.DYRTY, "true");

		// Prepare the blockproducer
		BlockFileList bflist = new BlockFileList(currentHeight, validChain.get(0));
		blockProducer = new BlockProducer(bflist, validChain, 1);

		long prevId = -1;
		if (currentHeight >= 0)
			prevId = getBlockId(validChain.get(0));

		currentHeight++;

		logger.info("All ready to start inserting");
		logger.info("\tStarting at Height " + currentHeight + " Block " + validChain.get(0));
		logger.debug("\tprevId: " + prevId);
		if (logger.getLevel().isMoreSpecificThan(Level.INFO))
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// Don't do anything
			}

		int numOfTransactions = 0;
		long startTime = System.currentTimeMillis();

		for (Block block : blockProducer) {
			prevId = writeBlock(block, currentHeight++, prevId);
			connection.commit();
			numOfTransactions += block.getTransactions().size();
			if (currentHeight % 10000 == 0) {
				double duration = (System.currentTimeMillis() - startTime) / 1000.0;
				logger.info("Inserted "
						+ numOfTransactions
						+ " transactions in "
						+ duration
						+ " seconds. That's about "
						+ (duration / numOfTransactions)
						+ " seconds per transaction.\n"
						+ "\tInserting 1M transactions takes approx "
						+ (duration / numOfTransactions * 1000000.0)
						+ " seconds");
				startTime = System.currentTimeMillis();
				numOfTransactions = 0;
			}
		}

		// stop manipulation on DB, leaving in clean state
		keyStore = new DBKeyStore();
		keyStore.setParameter(connection, DBKeyStore.DYRTY, "false");

		logger.info("Finished inserting Blocks");
		logger.debug("Database in clean state");

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
			logger.fatal("failed at", e);
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
					logger.fatal("failed at", e);
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
			logger.fatal("failed at", e);
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
			logger.fatal("failed at", e);
			System.exit(1);
		}

		return height;
	}

	private long writeBlock(Block block, int height, long prevId) {

		long startTime = System.currentTimeMillis();

		DataBlock dataBlock = new DataBlock(block, params, connection, height, prevId);
		dataBlock.writeBlock();

		for (Transaction transaction : block.getTransactions()) {
			DataTransaction dataTransaction = new DataTransaction(transaction, dataBlock.getId(), connection,
					block.getTime());
			dataTransaction.writeTransaction();
		}

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
