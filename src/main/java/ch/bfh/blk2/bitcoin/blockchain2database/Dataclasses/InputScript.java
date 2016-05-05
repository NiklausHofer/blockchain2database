package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.core.TransactionInput;

public interface InputScript {
	
	public void parseScript(TransactionInput in);
	
	public ScriptType getType();
	
	
}
