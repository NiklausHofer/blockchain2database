package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

public class OuputScriptCreator {

	public static OutputScript parseScript(TransactionOutput out, long txId, int txIndex) {
		try {
			byte[] outputBytes = out.getScriptBytes();
			int scriptSize = outputBytes.length;
			Script script = new Script(outputBytes);

			if (script.isSentToAddress())
				return new P2PKHashScript(script, scriptSize, txId, txIndex);

			if (script.isPayToScriptHash())
				return new P2SHScript(script, scriptSize, txId, txIndex);

			if (script.isSentToRawPubKey())
				return new P2RawPubKeyScript(script,scriptSize,txId,txIndex);

			if (script.isSentToMultiSig())
				return new MultiSigScript(script, scriptSize, txId, txIndex);

			if (script.isOpReturn())
				return new OPReturnScript(script, scriptSize, txId, txIndex);

			return new OtherScript(script, scriptSize, txId, txIndex);
		} catch (ScriptException e) {
			return new InvalidScript();
		}
	}

}
