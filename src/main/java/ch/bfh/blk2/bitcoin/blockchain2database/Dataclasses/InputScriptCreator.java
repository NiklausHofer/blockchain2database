package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

public class InputScriptCreator {

	private static final Logger logger = LogManager.getLogger("InputScriptCreator");

	public static InputScript parseScript(TransactionInput in, long txId, int txIndex, ScriptType prefOutType,
			long prevTxId, int prevTxIndex) {

		byte[] inputBytes = in.getScriptBytes();
		int scriptSize = inputBytes.length;

		if (in.isCoinBase())
			return new CoinbaseInputScript(inputBytes, txId, txIndex, scriptSize);

		Script script = new Script(inputBytes);

		if (prefOutType == ScriptType.OUT_MULTISIG)
			return new MultisigInputScript(txId, txIndex, script, scriptSize);
		if (prefOutType == ScriptType.OUT_P2PKHASH)
			if( script.getChunks().size() == 2 )
				return new P2PKHInputScript(script, txId, txIndex, scriptSize);
			else{
				logger.debug("Non standard Pay to public key hash input script looking like so: " + script.toString());
				return new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_P2PKH_SPEC);
			}
		if (prefOutType == ScriptType.OUT_P2RAWPUBKEY)
			if( script.getChunks().size() == 1 )
				return new P2RawPubKeyInputscript(txId, txIndex, script, scriptSize);
			else{
				logger.debug("Non standard Pay to raw public key input script looking like so: " + script.toString());
				return new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_P2RAWPUBKEY_SPEC);
			}
				
		if (prefOutType == ScriptType.OUT_P2SH)
			if (isP2SHMultisig(script))
				return new P2SHMultisigInputScript(txId, txIndex, script, scriptSize);
			else
				return new P2SHOtherInputScript(script, txId, txIndex, scriptSize);
		if (prefOutType == ScriptType.OUT_OTHER)
			return new OtherInputScript(txId, txIndex, script, scriptSize);

		// input script must be one of these types
		// input script can't be invalid

		logger.fatal("Infalid Inputscript");
		System.exit(1);
		return null;
	}

	private static boolean isP2SHMultisig(Script script) {

		ScriptChunk lastChunk = script.getChunks().get(script.getChunks().size() - 1);

		if (lastChunk.data != null)
			try {
				Script reedemScript = new Script(lastChunk.data);
				return reedemScript.isSentToMultiSig();
			} catch (ScriptException e) {
				logger.debug("invalid reedem Script or data");
				logger.debug("cant parse to script");
				return false;
			}
		else
			return false;
	}
}
