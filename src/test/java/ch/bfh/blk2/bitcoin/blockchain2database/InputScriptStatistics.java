package ch.bfh.blk2.bitcoin.blockchain2database;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.util.BlockFileList;
import ch.bfh.blk2.bitcoin.util.PropertiesLoader;
import ch.bfh.blk2.bitcoin.util.Utility;

public class InputScriptStatistics {
	
	private static final String PROP_TESTNET = "testnet",
			
			// entity count
			
			KEY_BLK = "blk",
			KEY_TX = "tx",
			KEY_IN = "in",
			KEY_P2SCRIPT = "p2script",
			KEY_OP_RETURN = "opreturn",
			KEY_UNKNOWN_SCRIPT = "unknown script",
			KEY_NULL_SCRIPT = "null script",
			KEY_ILLEGAL_ARGUMENT_SCRIPT = "Illegal argument",
			
			// script analysis
			KEY_JUNK = "junk",
			KEY_OP ="OP count",
			KEY_DATA="total data";
	
	private Map<String, Long> blkEntCount = new HashMap<>(),
			p2ScriptCount = new HashMap<>(),
			opReturnScript = new HashMap<>(),
			unknownScript = new HashMap<>();;
	
	public static void main(String [] args){
		InputScriptStatistics outStat = new InputScriptStatistics();
		outStat.run();
	}
	
	public void run(){
		
		BlockFileLoader bfl = setUpBlockFileLoader();
		
		for(Block blk : bfl){
						
			addToCount(blkEntCount,KEY_BLK, 1);
			addToCount(blkEntCount,KEY_TX, blk.getTransactions().size());
			
			
			//if(blkEntCount.get(KEY_BLK)>100) break;
			
			if(blkEntCount.get(KEY_BLK) % 10000 == 0)
				System.out.println("block :" + blkEntCount.get(KEY_BLK));
			
			
			for(Transaction tx : blk.getTransactions()){
				addToCount(blkEntCount,KEY_IN, tx.getInputs().size());
				
				for(TransactionInput in : tx.getInputs()){
					Script s = null;
					try{
						if(!in.isCoinBase())
						s = in.getScriptSig();
						
					}catch (ScriptException e){e.printStackTrace();}
					
					try{
					if(s != null){
						if( s.isSentToMultiSig()){
							addToCount(blkEntCount,KEY_P2SCRIPT, 1);
							alalizeScript(p2ScriptCount,s);
						}else if(s.isOpReturn()){
							addToCount(blkEntCount, KEY_OP_RETURN, 1);
							alalizeScript(opReturnScript,s);
						}
						else if(
								!s.isSentToAddress() &&
								!s.isSentToMultiSig() &&
								!s.isSentToRawPubKey()
						){
							addToCount(blkEntCount, KEY_UNKNOWN_SCRIPT, 1);
							alalizeScript(unknownScript,s);
						}
					}else{
						addToCount(blkEntCount,KEY_NULL_SCRIPT, 1);
					}
					}catch(IllegalArgumentException e){
						addToCount(blkEntCount,KEY_ILLEGAL_ARGUMENT_SCRIPT, 1);
					}
				}
			}
		}
		
		System.out.println("--- Blockchain entity count");
		printCounter(blkEntCount);
		System.out.println("\n\r--- P2ScriptHasch analysis");
		printCounter(p2ScriptCount);
		System.out.println("\n\r--- Op Return analysis");
		printCounter(opReturnScript);
		System.out.println("\n\r--- Unknown analysis");
		printCounter(unknownScript);
	}
	
	private void alalizeScript(Map<String, Long> map,Script s){
		
		addToCount(map,KEY_JUNK, s.getChunks().size());
		
		for (ScriptChunk sc : s.getChunks()){
			
			addToCount(map, "op_"+sc.opcode, 1);
			addToCount(map, KEY_OP, 1);

			if (sc.data != null){
				addToCount(map, "dat_"+sc.opcode, sc.data.length);
				addToCount(map, KEY_DATA, sc.data.length);
			}else{
				addToCount(map, "op_no_data_"+sc.opcode, 1);
			}
		}
	}

	private void printCounter(Map<String, Long> map){
		
		for(Entry<String, Long> e : map.entrySet()){
			System.out.printf("%-17s%20d", e.getKey(),e.getValue());
			System.out.println();
		}
	}

	private void addToCount(Map<String, Long> map,String key,long count){
		
		if(!map.containsKey(key))
			map.put(key,(long) 0);
		
		long val = map.get(key);
		map.put(key, val+count);
	}
	
	private BlockFileLoader setUpBlockFileLoader(){
		
		NetworkParameters params;
		if (Boolean.parseBoolean(PropertiesLoader.getInstance().getProperty(PROP_TESTNET)))
			params = new TestNet3Params();
		else
			params = new MainNetParams();
		Utility.setParams(params);
		BlockFileList blockChainFiles = new BlockFileList();
		Context context = Context.getOrCreate(Utility.PARAMS);
		BlockFileLoader bfl = new BlockFileLoader(Utility.PARAMS, blockChainFiles.getFileList());
		return bfl;
	}
	
}
