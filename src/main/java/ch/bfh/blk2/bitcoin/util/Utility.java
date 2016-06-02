package ch.bfh.blk2.bitcoin.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;

/**
 *
 * helper class with static methods for often used tasks
 *
 */
public class Utility {
	private static final Logger logger = LogManager.getLogger("Utility");

	public static NetworkParameters PARAMS = new TestNet3Params();
	private static final String PROPERTIES_FILE = "src/resources/blockchain.properties", DIRECTORY = "directory";

	public static void setParams(NetworkParameters params) {
		PARAMS = params;
	}

	/**
	 *
	 * gets the Address from an Output
	 *
	 * @param output The output which's address you seek
	 * @param params The network parameters
	 * @return an valid bitcoinAddress from pay to key or pay to script
	 * @throws ScriptException  if output has an invalid Address
	 */
	public static Address getAddressFromOutput(TransactionOutput output) throws ScriptException {
		try {
			return output.getScriptPubKey().getToAddress(PARAMS, true);
		} catch (IllegalArgumentException e) {
			throw new ScriptException("Unable to get the address");
		}
	}

	/**
	 * Allows to save a generated chain into a file. It will stored the ordered number of
	 * Sha256Hashes into a text file
	 * 
	 * @param chain ordered list of Sha256hashes of blocks
	 * @param fileName name of the file where the list should be written to
	 */
	public static void saveChain(List<Sha256Hash> chain, String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			logger.info("Found an already existing version of the chain file. Will replace it");
			file.delete();
		}

		try {
			file.createNewFile();
			PrintWriter pw = new PrintWriter(file);

			for (Sha256Hash hash : chain)
				pw.println(hash.toString());

			pw.flush();
			pw.close();

		} catch (IOException e) {
			logger.error("Can't create the chain file. Will continue nonetheless.");
		}
	}

}
