package ch.bfh.blk2.bitcoin.blockchain2database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.utils.BlockFileLoader;
import org.junit.Test;

import ch.bfh.blk2.bitcoin.util.BlockFileList;
import ch.bfh.blk2.bitcoin.util.Utility;

public class AddressTest {

	private static final String PROPERTIES_FILE = "src/resources/blockchain.properties", DIRECTORY = "directory";

	@Test
	public void someTest() {

		Properties properties = new Properties();
		NetworkParameters params;

		try {
			properties.load(new FileInputStream(PROPERTIES_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Boolean.parseBoolean(properties.getProperty("testnet")))
			params = new TestNet3Params();
		else
			params = new MainNetParams();
		Utility.setParams(params);

		//List<File> blockChainFiles = Utility.getDefaultFileList();
		BlockFileList blockChainFiles = new BlockFileList();
		Context context = Context.getOrCreate(Utility.PARAMS);

		BlockFileLoader bfl = new BlockFileLoader(Utility.PARAMS, blockChainFiles.getFileList());
		int count = 0;
		Transaction t = null;
		TransactionOutput o = null;

		try {

			for (Block blk : bfl)
				for (Transaction tx : blk.getTransactions())
					for (TransactionOutput out : tx.getOutputs()) {

						t = tx;
						o = out;

						Script s = out.getScriptPubKey();

						byte[] bytes = new byte[1];

						if (!s.isSentToAddress() && s.isPayToScriptHash() && !s.isSentToRawPubKey()) {

							count++;
							for (ScriptChunk b : s.getChunks())
								if (b.data != null) {

									int i = 1;
									bytes = b.data;
									i = b.data.length;

									byte[] addressBytes = new byte[1 + i + 4];
									addressBytes[0] = (byte) Utility.PARAMS.getAddressHeader();//version
									System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
									byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, bytes.length + 1);
									System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
									String addr = Base58.encode(addressBytes);
									System.out.println(addr);
									System.out.println(b.opcode);
								} else
									System.out.println("nan  " + s.getChunks().size());

						}

						//
						//						Address a = null;
						//
						//						if (s.isSentToAddress())
						//							a= new Address(Utility.PARAMS, s.getPubKeyHash());
						//						else if (s.isPayToScriptHash())
						//							a= Address.fromP2SHScript(Utility.PARAMS, s);
						//						else if( s.isSentToRawPubKey())
						//							a=ECKey.fromPublicOnly(s.getPubKey()).toAddress(Utility.PARAMS);
						//						else
						//							count ++;
						//
						//						if(a!=null){
						//							System.out.println("2 : "+a.toString());
						//						}

					}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();

		} catch (ScriptException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("unknown keys :" + count);
		System.out.println("crashed in transaction :" + t.getHashAsString());
		System.out.println("num of outputs :" + t.getOutputs().size());
		System.out.println("output index :" + o.getIndex());

	}

}
