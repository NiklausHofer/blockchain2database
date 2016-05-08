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

			if (script.isSentToAddress())
				if( script.getChunks().size() == 5 )
					return new P2PKHashScript(script, scriptSize, txId, txIndex);
				else{
					logger.debug( "Non standard Pay to address script looking like so: " + script.toString());
					return new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_P2PKHASH_SPEC);
				}

			if (script.isPayToScriptHash())
				if( script.getChunks().size() == 3)
					return new P2SHScript(script, scriptSize, txId, txIndex);
				else{
					logger.debug( "Non standard P2SH script looking like so: " + script.toString());
					return new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_P2SH_SPEC);
				}

			if (script.isSentToRawPubKey())
				if( script.getChunks().size() == 2 )
					return new P2RawPubKeyScript(script,scriptSize,txId,txIndex);
				else{
					logger.debug( "Non standard Pay to raw public key script looking like so: " + script.toString());
					return new OtherScript(script, scriptSize, txId, txIndex, ScriptType.OUT_P2RAWPUBKEY_SPEC);
				}

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
