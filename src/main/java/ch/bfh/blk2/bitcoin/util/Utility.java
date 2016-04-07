package ch.bfh.blk2.bitcoin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;

import ch.bfh.blk2.bitcoin.producer.FileMapSerializer;

/**
 *
 * helper class with static methods for often used tasks
 *
 */
public class Utility {

	public static NetworkParameters PARAMS = new TestNet3Params();
	private static final String PROPERTIES_FILE = "src/resources/blockchain.properties", DIRECTORY = "directory";

	public static void setParams(NetworkParameters params) {
		PARAMS = params;
	}

	/**
	 * search the bitcoinclients default folder for all blockchain files
	 *
	 * @return List a list of all blockchain files in your clients directory
	 */
	public static List<File> getDefaultFileList() {

		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(PROPERTIES_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String blockChainPath = properties.getProperty(DIRECTORY);

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
	 * Returns a list of .dat files containing the blockchain. If a fileMap is
	 * found, only those files will be returned that contain blocks which are
	 * higher or have an equal height to the passed height parameter.
	 *
	 * @param height
	 *            threshhold for file selection
	 * @return List A list of files containing blocks of height larger or equal
	 *         to height. If no fileMap is found, all files will be returned.
	 */
	public static List<File> getDefaultFileList(int height) {
		List<File> compleatList = getDefaultFileList();
		Map<String, Integer> fileMap = FileMapSerializer.read();

		// Abort if no fileMap was found
		if (fileMap == null)
			return compleatList;

		List<File> prunedList = new ArrayList<>(compleatList);
		// Newest file. Since the compleatlist is sorted, this works easy enough
		File newestFile = compleatList.get(compleatList.size() - 1);

		for (File f : compleatList)
			// Don't remove the newest file. It is always needet
			if (fileMap.get(f.getName()) < height && !f.equals(newestFile))
				prunedList.remove(f);

		return prunedList;
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
	public static Address getAddressFromOutput(TransactionOutput output) throws ScriptException {
		try {
			return output.getScriptPubKey().getToAddress(PARAMS, true);
		} catch (IllegalArgumentException e) {
			throw new ScriptException("Unable to get the address");
		}
	}

}
