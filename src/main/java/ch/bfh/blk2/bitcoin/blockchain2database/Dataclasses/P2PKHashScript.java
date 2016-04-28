package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.script.Script;

public class P2PKHashScript implements OutputScript {

	private Script script;

	public P2PKHashScript(Script script) {
		if (!script.isSentToAddress())
			throw new IllegalArgumentException("Script must be of type Pay to PubKeyHash");

		this.script = script;
	}

	@Override
	public OutputType getType() {
		return OutputType.P2PKHASH;
	}

	@Override
	public void writeOutputScript() {
		// TODO Auto-generated method stub

	}

}
