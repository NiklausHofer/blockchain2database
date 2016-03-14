package ch.bfh.blk2.bitcoin.blockchain2database;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.TestNet3Params;

import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataBlock;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.DataTransaction;
import ch.bfh.blk2.bitcoin.producer.BlockProducer;
import ch.bfh.blk2.bitcoin.util.Utility;

public class FooClass {

	private BlockProducer blockProducer;
	private Context context;
	private NetworkParameters params;
	private DatabaseConnection connection;

	public static void main(String[] args) {

		FooClass foo = new FooClass();
		foo.generateDatabase();
	}

	public FooClass() {

		// Init BitcoinJ
		// params = new MainNetParams();
		params = new TestNet3Params();
		context = Context.getOrCreate(params);

		// database connection
		connection = new DatabaseConnection();

		blockProducer = new BlockProducer(Utility.getDefaultFileList(), 1);
	}

	private void generateDatabase() {

		int height = 0;
		long prevId = -1;
		for (Block block : blockProducer) {
			prevId = writeBlock(block, height++, prevId);
			connection.commit();
		}

	}

	private long writeBlock(Block block, int height, long prevId) {
		long totalIn = 0;
		long totalOut = 0;

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

		return dataBlock.getId();

		// right at the end of the loop...
		// dataBlock.updateAmounts(totalIn, totalOut);
	}

}
