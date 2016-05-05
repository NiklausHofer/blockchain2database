package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class InvalidScript implements OutputScript {

	/**
	 * Invalid script. Don't need a script for that ;)
	 */
	public InvalidScript() {
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_INVALID;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		return;
	}

}
