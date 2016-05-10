package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

public enum ScriptType {
	OUT_P2PKHASH(0), OUT_P2RAWPUBKEY(1), OUT_MULTISIG(2), OUT_P2SH(3), OUT_OP_RETURN(4), OUT_OTHER(5), OUT_INVALID(
			6), IN_COINBASE(7), IN_P2PKH(8), IN_P2RAWPUBKEY(9), IN_MULTISIG(10), IN_P2SH_MULTISIG(11), IN_P2SH_OTHER(
					12), IN_OTHER(13), IN_INVALID(14), NO_PREV_OUT(15), IN_P2PKH_SPEC(16), IN_P2RAWPUBKEY_SPEC(
							17), IN_MLUTISIG_SPEC(18), IN_P2SH_OTHER_REDEEM(19);
	private int value;

	private ScriptType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}