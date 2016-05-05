package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;

public class InputScriptCreator {
		
	private static final Logger logger = LogManager.getLogger("InputScriptCreator");

	public static InputScript parseScript(TransactionInput in, long txId, int txIndex,ScriptType prefOutType){
		
		byte [] inputBytes = in.getScriptBytes();
		int scriptSize = inputBytes.length;
		Script script = new Script(inputBytes);
		
		if(prefOutType == ScriptType.OUT_MULTISIG){
			return new MultisigInputScript();
		}
		if(prefOutType == ScriptType.OUT_P2PKHASH){
			return new P2PKHInputScript();
		}
		if(prefOutType == ScriptType.OUT_P2RAWPUBKEY){
			return new P2RawPubKeyInputscript();
		}
		if(prefOutType == ScriptType.OUT_P2SH){
		
			if(isP2SHMultisig(script)){
				return new P2SHMultisigInputScript();
			}else{
				return new P2SHOtherInputScript();
			}
		}
		if(prefOutType == ScriptType.OUT_OTHER){
			return new OtherInputScript();
		}
		if(prefOutType == ScriptType.NO_PREV_OUT){
			return new CoinbaseInputScript();
		}
		
		// input script must be one of these types
		// input script can't be invalid
		
		logger.fatal("Infalid Inputscript");
		System.exit(1);
	}
	
	
	private static boolean isP2SHMultisig(Script script){
		//TODO --analize script
		// is multisig?
		return false;
	}
}