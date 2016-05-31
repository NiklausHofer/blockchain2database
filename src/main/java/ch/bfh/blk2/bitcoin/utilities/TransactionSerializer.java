package ch.bfh.blk2.bitcoin.utilities;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.OuputScriptCreator;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.OutputScript;

public class TransactionSerializer {

	private static Context context;

	public static void main(String[] args) {
		context = new Context(new MainNetParams());
		BlockFileLoader bfl = new BlockFileLoader(new MainNetParams(),
				BlockFileLoader.getReferenceClientBlockFileList());

		int[] types = { 0, 0, 0, 0, 0, 0 };
		int flip = 0;

		for (Block block : bfl)
			for (Transaction transaction : block.getTransactions()) {
				if (flip == 6)
					return;
				for (TransactionOutput output : transaction.getOutputs()) {
					int type = -1;
					try {
						OutputScript os = OuputScriptCreator.parseScript(output, 0, 0);
						type = os.getType().getValue();
					} catch (NullPointerException e) {
						System.out.println(output);
						continue;
					}
					if (type == 6)
						continue;
					if (types[type] == 0) {
						types[type] = 1;
						flip++;
						serialize(output);
					}
				}
			}

	}

	private static void serialize(TransactionOutput output) {
		byte[] scriptBytes = output.getScriptBytes();
		Script script = new Script(scriptBytes);
		System.out.println("Output #"
				+ output.getIndex()
				+ " of transaction "
				+ output.getParentTransactionHash().toString()
				+ ":");
		System.out.println(script);
		for (ScriptChunk chunk : script.getChunks()) {
			System.out.print("\topcode: " + chunk.opcode);
			System.out.print("\t\t");
			try {
				System.out.println("(isOpCode: " + chunk.isOpCode() + ", isPushData: " + chunk.isPushData() + ")");
			} catch (Exception e) {

			}
			System.out.println("\t\t" + chunk.toString());
		}

	}

}
