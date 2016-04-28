package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.script.Script;

public class P2RawPubKeyScript implements OutputScript {

	private Script script;

	public P2RawPubKeyScript(Script script) {
		if (!script.isSentToRawPubKey())
			throw new IllegalArgumentException("Script must be of type Pay to RawPybKey");

		this.script = script;
	}

	@Override
	public OutputType getType() {
		return OutputType.P2RAWPUBKEY;
	}

	@Override
	public void writeOutputScript() {
		// TODO Auto-generated method stub

	}

}
