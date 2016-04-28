package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.script.Script;

public class MultiSigScript implements OutputScript {

	private Script script;

	public MultiSigScript(Script script) {
		if (!script.isSentToMultiSig())
			throw new IllegalArgumentException("Script needs to be of type Bare Multisig");
		this.script = script;
	}

	@Override
	public OutputType getType() {
		return OutputType.MULTISIG;
	}

	@Override
	public void writeOutputScript() {
		// TODO Auto-generated method stub

	}

}
