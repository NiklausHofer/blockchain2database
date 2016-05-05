package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public interface OutputScript {

	public ScriptType getType();

	public void writeOutputScript(DatabaseConnection connection);
}
