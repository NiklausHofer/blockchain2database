package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;

public class OuputScriptCreator {

	private static final Logger logger = LogManager.getLogger("OutputScriptCreator");

	public static OutputScript parseScript(TransactionOutput out, long txId, int txIndex) {
		try {
			byte[] outputBytes = out.getScriptBytes();
			int scriptSize = outputBytes.length;
			Script script = new Script(outputBytes);

			try {
				if (script.isSentToAddress())
					return new P2PKHashScript(script, scriptSize, txId, txIndex);

				if (script.isPayToScriptHash())
					return new P2SHScript(script, scriptSize, txId, txIndex);

				if (script.isSentToRawPubKey())
					return new P2RawPubKeyScript(script, scriptSize, txId, txIndex);

				if (script.isSentToMultiSig())
					return new MultiSigScript(script, scriptSize, txId, txIndex);

				// OP_RETURN w/ data >80 Byte is not relayed by core client, thus not a standard transaction...
				if (script.isOpReturn() && script.getChunks().get(1).data != null && script.getChunks().get(1).data.length <= 80 )
					return new OPReturnScript(script, scriptSize, txId, txIndex);

			} catch (IllegalArgumentException e) {
				logger.debug("There was an error when trying to detect the script type for the following scrip: "
						+ script.toString()
						+ " It will be saved as OtherScript");
				return new OtherScript(script, scriptSize, txId, txIndex);
			}

			return new OtherScript(script, scriptSize, txId, txIndex);

		} catch (ScriptException e) {
			return new InvalidScript();
		}
	}

}
