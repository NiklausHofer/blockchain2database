package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

/**
 * Use this class to wrap transaction Input Scripts into the correct InputScript type to then be written to the database
 * 
 * @author niklaus
 */
public class InputScriptCreator {

	private static final Logger logger = LogManager.getLogger("InputScriptCreator");
	
	/*
	// Lengths of keys and signatures according to
	// https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm
	private final static int MAX_KEY_LENGTH = 65;
	private final static int MAX_SIG_LENGTH = 73;
	*/

	/**
	 * Given a Bitcoinj Script, this will output the correct InputScript, which can then be used to write the input
	 * into the database. The type of the input script is determined from the prevOutType. Depending on that, some further
	 * checks are conducted on the script. If it meets all of them, an InputScript of the correct script type will be 
	 * returned. If these additional tests fail, the script will instead be wrapped into a more generic OtherInputScript.
	 * No matter what type of Script is returned, you can check the type by calling the getType() method.
	 * If the prevOutType is an unknown type, then the application will stall.
	 * 
	 * @param in The transaction Input which's script is to be used
	 * @param txId The corresponding transaction's database ID
	 * @param txIndex The corresponding transaction's Index (within the block and the database)
	 * @param prevOutType The previous output's (script) type
	 * @return the InputScript representing the input script, used to write it into the database
	 */
	public static InputScript parseScript(TransactionInput in, long txId, int txIndex, ScriptType prevOutType){

		byte[] inputBytes = in.getScriptBytes();
		int scriptSize = inputBytes.length;

		// COINBASE
		if (in.isCoinBase())
			return new CoinbaseInputScript(inputBytes, txId, txIndex, scriptSize);

		Script script = new Script(inputBytes);

		// P2PKH
		if (prevOutType == ScriptType.OUT_P2PKHASH){
			InputScript inputScript;
			try{
				inputScript =  new P2PKHInputScript(script, txId, txIndex, scriptSize);
			} catch (IllegalArgumentException e){
				logger.debug("Non standard Pay to public key hash input script: " + e.toString() );
				logger.debug("The script looks like so: " + script.toString());
				inputScript =  new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_P2PKH_SPEC);
			}
			
			return inputScript;
		}

		// MULTISIG
		if (prevOutType == ScriptType.OUT_MULTISIG || prevOutType == ScriptType.OUT_MULTISIG_SPEC){
			InputScript inputScript;
			try{
				inputScript =  new MultisigInputScript(txId, txIndex, script, scriptSize);
			} catch (IllegalArgumentException e){
				logger.debug("Non standard Multisig input script: " + e.toString() );
				logger.debug("The script looks like so: " + script.toString());
				inputScript =  new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_MLUTISIG_SPEC);
			}
			
			return inputScript;
		}
		
		// RAW PUB KEY
		if (prevOutType == ScriptType.OUT_P2RAWPUBKEY || prevOutType == ScriptType.OUT_RAWPUBKEY_SPEC){
			InputScript inputScript;
			try{
				inputScript= new P2RawPubKeyInputscript(txId, txIndex, script, scriptSize);
			} catch( IllegalArgumentException e){
				logger.debug("Non standard RAW PUB KEY input script: " + e.toString() );
				logger.debug("The script looks like so: " + script.toString());
				inputScript =  new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_P2RAWPUBKEY_SPEC);
			}
			
			return inputScript;
		}
				
		// P2SH
		if (prevOutType == ScriptType.OUT_P2SH){
			InputScript inputScript;
			if (isP2SHMultisig(script))
				try{
					inputScript = new P2SHMultisigInputScript(txId, txIndex, script, scriptSize);
				} catch (IllegalArgumentException e){
					logger.debug("P2SH Multisig Input with weird Input script: " + e.toString() );
					logger.debug("The script looks like so: " + script.toString());
					inputScript =  new OtherInputScript(txId, txIndex, script, scriptSize, ScriptType.IN_P2SH_MULTISIG_SPEC);
				}
			else
				inputScript =  new P2SHOtherInputScript(script, txId, txIndex, scriptSize);
			
			return inputScript;
		}

		// OTHER
		if (prevOutType == ScriptType.OUT_OTHER)
			return new OtherInputScript(txId, txIndex, script, scriptSize);

		// input script must be one of these types input script can't be invalid
		logger.fatal("Invalid Inputscript of type " + prevOutType + " : " + script.toString());
		System.exit(1);
		return null;
	}
	
	private static boolean isP2SHMultisig(Script script) {
		List<ScriptChunk> scriptChunks = script.getChunks();
		ScriptChunk lastChunk = scriptChunks.get(script.getChunks().size() - 1);
		
		if( lastChunk.data == null)
			return false;

		try {
			Script redeemScript = new Script(lastChunk.data);
			if(redeemScript.isSentToMultiSig())
				return true;
			
			return false;
		} catch (ScriptException | IllegalArgumentException e) {
			logger.debug("invalid redeem Script or data. Can't parse the script");
			return false;
		}
	}
}
