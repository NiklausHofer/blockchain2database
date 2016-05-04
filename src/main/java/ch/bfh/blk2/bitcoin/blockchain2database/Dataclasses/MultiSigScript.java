package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class MultiSigScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OPReturnScript");

	private Script script;
	private long tx_id;
	private int tx_index;
	private int scriptSize;
	private int min = -1;
	private int max = -1;

	List<byte[]> publickeys;

	public MultiSigScript(Script script, int scriptSize, long tx_id, int tx_index) {
		if (!script.isSentToMultiSig())
			throw new IllegalArgumentException("Script needs to be of type Bare Multisig");

		this.script = script;
		this.tx_id = tx_id;
		this.tx_index = tx_index;
		this.scriptSize = scriptSize;

		publickeys = new ArrayList<>();
	}

	@Override
	public OutputType getType() {
		return OutputType.MULTISIG;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		parse();
		// TODO Auto-generated method stub
	}

	private void parse() {
		List<ScriptChunk> chunks = script.getChunks();
		int predictedNumberOfPubKeys = chunks.size() - 3;

		// The minimum amount of signatures needet
		int firstOpCode = chunks.get(0).opcode;
		if (firstOpCode < 81 || firstOpCode > 96)
			failParse();
		min = firstOpCode - 80;

		// Assume all the upcoming chunks are pubkeys, until the next value appears
		int i = 1;
		while (i < chunks.size() && chunks.get(i).opcode < 76)
			publickeys.add(chunks.get(i++).data);
		if (i >= chunks.size())
			failParse();
		int nextOpCode = chunks.get(i).opcode;
		if (nextOpCode < 81 || nextOpCode > 96)
			failParse();
		max = nextOpCode - 80;

		if (predictedNumberOfPubKeys != publickeys.size()) {
			logger.warn("Multisig script for output #"
					+ tx_index
					+ " of transaction "
					+ tx_id
					+ " is parsable, but of an unexpected format.");
			logger.warn("We expected there to be "
					+ predictedNumberOfPubKeys
					+ " pubkeys, but we have found "
					+ publickeys.size()
					+ " of them");
			logger.warn(script.toString());
		}

	}

	private void failParse() {
		logger.fatal(
				"Multisig Script for output #" + tx_index + " of transaction " + tx_id + " is of an unexpected format");
		logger.fatal(script.toString());
		System.exit(1);
	}

}
