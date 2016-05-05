package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

public enum ScriptType {
	OUT_P2PKHASH(0), OUT_P2RAWPUBKEY(1), OUT_MULTISIG(2), OUT_P2SH(3), OUT_OP_RETURN(4), OUT_OTHER(5), OUT_INVALID(
			6), IN_COINBASE(7), IN_P2PKH(8), IN_P2RAWPUBKEY(9), IN_MULTISIG(10), IN_P2SH_MULTISIG(11), IN_P2SH_OTHER(
					12), IN_OTHER(13), IN_INVALID(14);
	private int value;

	private ScriptType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}