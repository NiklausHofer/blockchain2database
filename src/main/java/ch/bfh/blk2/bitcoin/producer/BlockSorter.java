package ch.bfh.blk2.bitcoin.producer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.comparator.Sha256HashComparator;
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

	private BlockIdentifier deepestBlock;
	private Map<Sha256Hash, BlockIdentifier> blockMap;
	private BlockFileLoader bfl;
	private List<BlockIdentifier> unsortedBlocks;

	/**
	 * @param blockChainFiles
	 *            the files containing the unparsed blockchain
	 */
	public BlockSorter( List<File> blockChainFiles ) {
		Collections.sort( blockChainFiles );

		blockMap = new TreeMap<>( new Sha256HashComparator( ) );
		unsortedBlocks = new LinkedList<>( );

		bfl = new BlockFileLoader( Utility.PARAMS, blockChainFiles );

		sort( );
	}

	private void insertBlock( BlockIdentifier bi ) {
		BlockIdentifier parent = blockMap.get( bi.getParentHash( ) );
		bi.setParent( parent );
		bi.setDepth( parent.getDepth( ) + 1 );
		blockMap.put( bi.getBlockHash( ), bi );
		if (bi.getDepth( ) > deepestBlock.getDepth( ))
			deepestBlock = bi;

		for (BlockIdentifier bli : unsortedBlocks)
			if (blockMap.containsKey( bli.getParentHash( ) )) {
				unsortedBlocks.remove( bli );
				insertBlock( bli );
				break;
			}
	}

	private void sort( ) {
		/*
		 * Insert a virtual block that is parent of gensis, allowing us to
		 * handle Genesis Block in the same way as normal blocks.
		 */
		BlockIdentifier virtualBlock = new BlockIdentifier(
				Sha256Hash.ZERO_HASH );
		virtualBlock.setDepth( -1 );
		deepestBlock = virtualBlock;
		blockMap.put( virtualBlock.getBlockHash( ), virtualBlock );

		for (Block blk : bfl) {
			BlockIdentifier bi = new BlockIdentifier( blk );
			if (blockMap.containsKey( bi.getParentHash( ) ))
				insertBlock( bi );
			else
				unsortedBlocks.add( bi );
		}
	}

	/**
	 *
	 * @return a list of hashes representing blocks in the order that is
	 *         considered the current state of the blockchain
	 */
	public List<Sha256Hash> getLongestBranch( ) {
		List<Sha256Hash> branch = new LinkedList<>( );

		BlockIdentifier current = deepestBlock;

		while (current.getDepth( ) >= 0) {
			branch.add( 0, current.getBlockHash( ) );
			current = current.getParent( );
		}

		return branch;
	}

	// main method for testing
	public static void main( String[] args ) {
		String homedir = System.getProperty( "user.home" );
		String blockChainPath = homedir + "/.bitcoin/blocks";

		File dir = new File( blockChainPath );
		File[] files = dir.listFiles( new FilenameFilter( ) {
			@Override
			public boolean accept( File dir, String name ) {
				return name.matches( "blk\\d{5}.dat" );
			}
		} );

		List<File> blockChainFiles = new ArrayList<>( Arrays.asList( files ) );

		BlockSorter sorter = new BlockSorter( blockChainFiles );

		System.out.println( "================================" );
		System.out.println( "Highest block: "
				+ (sorter.getLongestBranch( ).size( ) + 1) );
		System.out.println( "================================" );
	}
}
