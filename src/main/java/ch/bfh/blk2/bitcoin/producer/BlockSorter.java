package ch.bfh.blk2.bitcoin.producer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.comparator.Sha256HashComparator;
import ch.bfh.blk2.bitcoin.util.BlockFileList;
import ch.bfh.blk2.bitcoin.util.Utility;

/**
 * The blockchain can contain orphan blocks. Whenever it does, one could imagine
 * the blockchain like a tree, from which the orphan blocks branch out (in tiny
 * branches, most often). We need to find the way to the top of the tree without
 * getting lost in the branches. This class does exactly this.
 *
 * fetch an ordered List of blockhashes skip orphan blocks
 *
 */
public class BlockSorter {
	private static final Logger logger = LogManager.getLogger("BlockSorter");

	private BlockIdentifier deepestBlock;
	private Map<Sha256Hash, BlockIdentifier> blockMap;
	private List<BlockIdentifier> unsortedBlocks;
	//private List<File> files;
	private BlockFileList fileList;

	/* Default values for when we start at the bottom of the chain */
	private Sha256Hash rootHash = Sha256Hash.ZERO_HASH;
	private int virtBlockHeight = -1;

	/**
	 * @param bflist the files containing the unparsed blockchain
	 */
	public BlockSorter(BlockFileList bflist) {
		//Collections.sort(blockChainFiles);

		blockMap = new TreeMap<>(new Sha256HashComparator());
		unsortedBlocks = new LinkedList<>();

		virtBlockHeight = bflist.getStartHeight();
		this.rootHash = bflist.getRootHash();

		logger.info("Will continue from Block #" + virtBlockHeight + " : " + rootHash);

		fileList = bflist;

		//bfl = new BlockFileLoader(Utility.PARAMS, blockChainFiles);

		sort();
		Map<String, Integer> fileMap = FileMapSerializer.read();
		if (fileMap == null)
			FileMapSerializer.write(this.extractFileInformation());
		else
			FileMapSerializer.write(this.extractFileInformation(fileMap));
	}

	private void insertBlock(BlockIdentifier bi) {
		logger.trace("Inserting yet another block: " + bi.getBlockHash().toString());
		BlockIdentifier parent = blockMap.get(bi.getParentHash());
		bi.setParent(parent);
		bi.setDepth(parent.getDepth() + 1);
		blockMap.put(bi.getBlockHash(), bi);
		if (bi.getDepth() > deepestBlock.getDepth())
			deepestBlock = bi;

		// Makes the program faster whenever chains are found after an insert
		for (BlockIdentifier bli : unsortedBlocks)
			if (bli.getParentHash().equals(bi.getBlockHash())) {
				unsortedBlocks.remove(bli);
				insertBlock(bli);
				return;
			}

		for (BlockIdentifier bli : unsortedBlocks)
			if (blockMap.containsKey(bli.getParentHash())) {
				unsortedBlocks.remove(bli);
				insertBlock(bli);
				break;
			}
	}

	private void sort() {
		/*
		 * Insert a virtual block that is parent of gensis, allowing us to
		 * handle Genesis Block in the same way as normal blocks.
		 */
		BlockIdentifier virtualBlock = new BlockIdentifier(rootHash);
		virtualBlock.setDepth(virtBlockHeight);
		deepestBlock = virtualBlock;
		blockMap.put(virtualBlock.getBlockHash(), virtualBlock);

		logger.debug("start sorting");

		//logger.debug(bfl);
		for (File file : fileList) {

			List<File> currentFile = new ArrayList<>(1);
			currentFile.add(file);
			BlockFileLoader bfl = new BlockFileLoader(Utility.PARAMS, currentFile);
			for (Block blk : bfl) {
				logger.trace("block: " + blk.getHashAsString());
				BlockIdentifier bi = new BlockIdentifier(blk, file.getName());
				if (blockMap.containsKey(bi.getParentHash()))
					insertBlock(bi);
				else
					unsortedBlocks.add(bi);
			}
		}
	}

	public Map<String, Integer> extractFileInformation() {
		return extractFileInformation(new TreeMap<String, Integer>());
	}

	/**
	 * Stores a list of files and the highest block found in each file into the map.
	 * If the map already had entries, it will be updated.
	 * 
	 * @param fileMap an older map already containing values
	 * @return fileMap a map of all files and the height of the highest block inside that file
	 */
	public Map<String, Integer> extractFileInformation(Map<String, Integer> fileMap) {

		for (BlockIdentifier bi : blockMap.values()) {
			String filename = bi.getFilename();
			if (!fileMap.containsKey(filename) || fileMap.get(filename) < bi.getDepth())
				fileMap.put(filename, bi.getDepth());
		}

		logger.debug("Highest block in each .dat file:");
		for (Entry<String, Integer> e : fileMap.entrySet())
			logger.debug("\t" + e.getKey() + "\t" + e.getValue());

		logger.trace("The following blocks are not part of _any_ chain:");
		for (BlockIdentifier bi : unsortedBlocks)
			logger.trace("\t"
					+ bi.getBlockHash().toString()
					+ " ==> "
					+ bi.getParentHash().toString()
					+ " ("
					+ bi.getFilename()
					+ ")");

		return fileMap;
	}

	/**
	 *
	 * @return a list of hashes representing blocks in the order that is
	 *         considered the current state of the blockchain
	 */
	public List<Sha256Hash> getLongestBranch() {
		List<Sha256Hash> branch = new LinkedList<>();

		BlockIdentifier current = deepestBlock;

		while (current.getDepth() > virtBlockHeight) {
			branch.add(0, current.getBlockHash());
			current = current.getParent();
		}

		return branch;
	}

	/**
	 * Main method. Used for testing only.
	 * 
	 * @param args arguments / parameters
	 */
	public static void main(String[] args) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("target/classes/blockchain.properties"));
		} catch (Exception e) {
			logger.fatal("Unable to read the properties file");
			logger.fatal("failed at", e);
			System.exit(1);
		}

		NetworkParameters params = null;

		if (Boolean.parseBoolean(properties.getProperty("testnet"))) {
			params = new TestNet3Params();
			logger.info("Operating on Testnet3");
		} else {
			params = new MainNetParams();
			logger.info("Operating on MainNet");
		}
		Utility.setParams(params);
		Context context = Context.getOrCreate(params);

		// Check if a chain file is present already...
		File file = new File("./chain");
		if (file.exists() && !file.isDirectory() && file.canRead()) {
			logger.info("Found a chain File. Will attempt to continue from there");

			try {
				// Read in the old chain
				BufferedReader reader = new BufferedReader(new FileReader(file));
				List<String> hashes = new LinkedList<>();
				String line = null;
				int lineNr = 0;
				while ((line = reader.readLine()) != null) {
					lineNr++;
					hashes.add(line);
					if (hashes.size() > 100)
						hashes.remove(0);
				}
				logger.info("Highest block in chian file:\n\theight: "
						+ (lineNr - 1)
						+ "\tblock: "
						+ hashes.get(hashes.size() - 1));
				logger.info("Block #" + (lineNr - 100) + " : " + hashes.get(0));

				BlockFileList bflist = new BlockFileList(lineNr - 100, hashes.get(0));

				logger.debug("These files will be searched: ");

				for (File f : bflist)
					logger.debug("\t" + f.getName());

				BlockSorter sorter = new BlockSorter(bflist);
				Utility.saveChain(sorter.getLongestBranch(), "./chain2");
				logger.info("done!");
			} catch (IOException e) {
				logger.info("Unable to process the chain file.", e);
				System.exit(1);
			}
			return;
		}

		logger.info("We could not find (or not read) an old chainfile. Will start from scratch");

		BlockFileList bflist = new BlockFileList();
		BlockSorter sorter = new BlockSorter(bflist);
		Utility.saveChain(sorter.getLongestBranch(), "./chain");
		long startTime = System.currentTimeMillis();
		Map<String, Integer> fileMap = sorter.extractFileInformation();
		FileMapSerializer.write(fileMap);
		logger.info((System.currentTimeMillis() - startTime) / 1000.0);

		System.out.println("================================");
		System.out.println("Highest block: " + (sorter.getLongestBranch().size() - 1));
		System.out.println("================================");
	}
}
