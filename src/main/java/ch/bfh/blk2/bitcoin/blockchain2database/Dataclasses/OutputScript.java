package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

enum OutputType {
	P2PKHASH(0), P2RAWPUBKEY(1), MULTISIG(2), P2SH(3), OP_RETURN(4), OTHER(5), INVALID(6);
	private int value;

	private OutputType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

interface OutputScript {

	public OutputType getType();

	public void writeOutputScript(DatabaseConnection connection);
}
