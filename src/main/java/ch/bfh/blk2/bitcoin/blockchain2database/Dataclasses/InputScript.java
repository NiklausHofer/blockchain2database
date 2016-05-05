package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public interface InputScript {
		
	public ScriptType getType();
	
	public void writeInput(DatabaseConnection connection);
	
}
