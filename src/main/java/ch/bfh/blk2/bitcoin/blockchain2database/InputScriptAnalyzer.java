package ch.bfh.blk2.bitcoin.blockchain2database;

import java.util.List;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

public class InputScriptAnalyzer {
	
	private static Context context;
	
	public static void main(String[] args) {
		context = new Context(new MainNetParams());
		BlockFileLoader bfl = new BlockFileLoader(new MainNetParams(), BlockFileLoader.getReferenceClientBlockFileList());
		
		int fakeCounter = 0;
		int realCounter = 0;
		int myCounter = 0;
		
		for( Block block: bfl)
			for( Transaction transaction: block.getTransactions())
				for( TransactionInput input: transaction.getInputs()){
					try{
						byte[] scriptBytes = input.getScriptBytes();
						Script script = new Script( scriptBytes );
						if( script.isSentToMultiSig() )
							fakeCounter++;
						for( ScriptChunk scriptChunk: script.getChunks()){
							if( scriptChunk.isOpCode())
								if( scriptChunk.equalsOpCode(174) || scriptChunk.equals(175)){
									realCounter++;
									continue;
								}
						}
						try{
							List<ScriptChunk> chunks = script.getChunks();
							Script redeemscript = new Script(chunks.get(chunks.size()-1).data);
							if( redeemscript.isSentToMultiSig()){
								myCounter++;
							}
						} catch (Exception e ){
							
						}
					} catch (Exception e ){
						
					}

				}
		
		System.out.println("fakeCounter " + fakeCounter );
		System.out.println("realCounter " + realCounter );
		System.out.println("myCounter " + myCounter );
	}

}
