package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.core.TransactionInput;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class CoinbaseInputScript implements InputScript{
	
	
	private final static String INSERT_COINBASE_SCRIPT;
	
	private int txIndex,scriptSize;
	private long txId;
	private byte[] information;

	public CoinbaseInputScript(long txId,int txIndex,int scriptSize) {
		
		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
		this.information = information;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_COINBASE;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
		
		
	}

}
