package ch.bfh.blk2.bitcoin.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

/**
 *
 * helper class with static methods for often used tasks
 *
 */
public class Utility {

    // public static MainNetParams PARAMS = new MainNetParams();
    public static TestNet3Params PARAMS = new TestNet3Params();

    /**
     * search the bitcoinclients default folder for all blockchain files
     *
     * @return List a list of all blockchain files in your clients directory
     */
    public static List<File> getDefaultFileList() {
	String homedir = System.getProperty("user.home");
	String blockChainPath = homedir + "/.bitcoin/testnet3/blocks";

	File dir = new File(blockChainPath);
	File[] files = dir.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return name.matches("blk\\d{5}.dat");
	    }
	});

	List<File> blockChainFiles = new ArrayList<>(Arrays.asList(files));

	Collections.sort(blockChainFiles);

	return blockChainFiles;
    }

    /**
     *
     * gets the Address from an Output
     *
     * @param output
     *            The output which's address you seek
     * @param params
     *            The network parameters
     * @return an valid bitcoinAddress from pay to key or pay to script
     * @throws ScriptException
     *             if output has an invalid Address
     */
    public static Address getAddressFromOutput(TransactionOutput output, MainNetParams params) throws ScriptException {
	try {
	    return output.getScriptPubKey().getToAddress(params, true);
	} catch (IllegalArgumentException e) {
	    throw new ScriptException("Unable to get the address");
	}
    }

}
