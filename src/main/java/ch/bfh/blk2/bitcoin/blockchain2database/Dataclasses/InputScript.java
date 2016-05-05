package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.core.TransactionInput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public interface InputScript {
	
	public void parseScript(TransactionInput in);
	
	public ScriptType getType();
	
	public void writeInput(DatabaseConnection connection);
	
}
