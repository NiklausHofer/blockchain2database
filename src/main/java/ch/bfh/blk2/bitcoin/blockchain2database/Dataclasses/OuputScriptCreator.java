package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

public class OuputScriptCreator {

	public static OutputScript parseScript(TransactionOutput out) {
		try {
			byte[] outputBytes = out.getScriptBytes();
			Script script = new Script(outputBytes);

			if (script.isSentToAddress())
				return new P2PKHashScript(script);

			if (script.isPayToScriptHash())
				return new P2SHScript(script);

			if (script.isSentToRawPubKey())
				return new P2RawPubKeyScript(script);

			if (script.isSentToMultiSig())
				return new MultiSigScript(script);

			if (script.isOpReturn())
				return new OPReturnScript(script);

			return new OtherScript(script);
		} catch (ScriptException e) {
			return new InvalidScript();
		}
	}

}
