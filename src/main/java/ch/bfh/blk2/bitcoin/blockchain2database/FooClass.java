package ch.bfh.blk2.bitcoin.blockchain2database;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.asn1.dvcs.Data;

import Dataclasses.DataBlock;
import Dataclasses.DataTransaction;
import ch.bfh.blk2.bitcoin.producer.BlockProducer;
import ch.bfh.blk2.bitcoin.util.Utility;

public class FooClass {

	private BlockProducer blockProducer;
	private Context context;
	private NetworkParameters params;

	public static void main(String[] args) {

		FooClass foo = new FooClass();
		foo.generateDatabase();
	}

	public FooClass() {

		// Init BitcoinJ
		params = new MainNetParams();
		context = Context.getOrCreate(params);

		blockProducer = new BlockProducer(Utility.getDefaultFileList(), 1);
	}

	private void generateDatabase() {

		for (Block block : blockProducer)
			writeBlock(block);
	}

	private void writeBlock(Block block) {
		int totalIn = 0;
		int totalOut = 0;

		DataBlock dataBlock = new DataBlock(block, params);
		dataBlock.writeBlock();

		for (Transaction transaction : block.getTransactions()) DataTransaction dataTransaction = new DataTransaction( transaction, dataBlock.getId());

		// right at the end of the loop...
		dataBlock.updateAmounts(totalIn, totalOut);
	}

}
