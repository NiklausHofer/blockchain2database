package ch.bfh.blk2.bitcoin.producer;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.comparator.Sha256HashComparator;
import ch.bfh.blk2.bitcoin.util.Utility;

/**
 * This class allows to iterate over the blockchain. It uses the BlockSorter to
 * retain the correct order of blocks to work through and offers an iterator
 * over all the transactions in the blockchain.
 *
 * All blocks will be returned to the caller in a logical order. That is to say,
 * if a transaction T_B spends a T_A, then T_A will always be returned before
 * T_B.
 *
 * Please note, that each Iterator can use up to two gigabytes of memory, so be
 * careful when using it.
 *
 * @author niklaus, stefan
 *
 */
public class TransactionProducer implements
		Iterable<Transaction> {

	private static final int DEFAULT_MIN_BLOCK_DEPTH = 6;
	private static final Logger logger = LogManager
			.getLogger( "TransactionProducer" );

	private List<Sha256Hash> orderedBlockHashes;
	private List<File> blockChainFiles;
	// private List<Block> blockBuffer;

	private List<BlockFilter> filters;

	/**
	 * Uses the default minimum block depth 6
	 *
	 * @param blockChainFiles
	 *            a list of unparsed Blockchain Files
	 * @param filters
	 *            a list of block filters
	 */
	public TransactionProducer( List<File> blockChainFiles,
			List<BlockFilter> filters ) {
		this( blockChainFiles, filters, DEFAULT_MIN_BLOCK_DEPTH );
	}

	/**
	 *
	 * @param blockChainFiles
	 *            a list of unparsed Blockchain Files
	 * @param filters
	 *            a list of block filters
	 * @param minBlockDepth
	 *            the number of blocks at the end of the blockchain to be
	 *            ignored a number defining the minimal depth a block must have
	 *            to be accepted
	 */
	public TransactionProducer( List<File> blockChainFiles,
			List<BlockFilter> filters, int minBlockDepth ) {
		this.blockChainFiles = blockChainFiles;
		this.filters = filters;
		BlockSorter bs = new BlockSorter( this.blockChainFiles );
		this.orderedBlockHashes = bs.getLongestBranch( );

		if (orderedBlockHashes.size( ) > minBlockDepth)
			for (int i = 0; i < minBlockDepth; i++)
				orderedBlockHashes.remove( orderedBlockHashes.size( ) - 1 );

		if (this.orderedBlockHashes.size( ) < 1) {
			logger.fatal( "BlockSorter is malfunctioning or an invalid list of files was provided" );
			System.exit( 1 );
		}
		logger.debug( "Got a chain of " + this.orderedBlockHashes.size( )
				+ "blocks" );
	}

	/**
	 * WARNING! Each Iterator can use up to two Gigabytes of memory. So be
	 * careful when using this.
	 */
	@Override
	public Iterator<Transaction> iterator( ) {
		return new TransactionIterator( );
	}

	public class TransactionIterator implements Iterator<Transaction> {
		private Iterator<Sha256Hash> hashIterator;
		private Iterator<File> fileIterator;

		private Iterator<Transaction> transactionIterator;

		private Map<Sha256Hash, Block> blockBuffer;
		private List<Block> validBlocks;
		private int blockCount = 0;

		private TransactionIterator( ) {
			hashIterator = TransactionProducer.this.orderedBlockHashes
					.iterator( );
			fileIterator = TransactionProducer.this.blockChainFiles
					.iterator( );

			blockBuffer = new TreeMap<>( new Sha256HashComparator( ) );
			validBlocks = new LinkedList<>( );

			// Place holder to make recursion work. Will be removed after
			// reading in the actual first blocks
			validBlocks.add( null );

			logger.debug( "Going to get the very first block now" );
			iterateBlock( );
		}

		@Override
		public boolean hasNext( ) {
			if (transactionIterator.hasNext( ))
				return true;
			else
				return validBlocks.size( ) >= 2;
		}

		private void iterateBlock( ) {
			while (validBlocks.size( ) < 3 && hashIterator.hasNext( )) {
				Block blk = getBlock( hashIterator.next( ) );
				if (valid( blk ))
					validBlocks.add( blk );
			}

			validBlocks.remove( 0 );
			if (validBlocks.isEmpty( )) {
				System.err
						.println( "iterateBlock was called, but there are no more blocks to fetch and we are all out!" );
				logger.debug( validBlocks.size( ) );
				System.exit( 1 );
			}
			transactionIterator = validBlocks.get( 0 ).getTransactions( )
					.iterator( );
			logger.trace( "Giving out block #" + blockCount++ );
		}

		@Override
		public Transaction next( ) throws NoSuchElementException {
			if (transactionIterator.hasNext( ))
				return transactionIterator.next( );
			else if (validBlocks.size( ) >= 2) {
				iterateBlock( );
				return transactionIterator.next( );
			} else
				throw new NoSuchElementException( );
		}

		private boolean valid( Block b ) {
			for (BlockFilter filter : TransactionProducer.this.filters)
				if (!filter.filter( b ))
					return false;
			return true;
		}

		private Block getBlock( Sha256Hash blockHash ) {
			logger.trace( "Got asked for block " + blockHash );
			if (blockBuffer.containsKey( blockHash )) {
				logger.trace( "Found the block in the blockBuffer" );
				return blockBuffer.remove( blockHash );
			} else {
				try {
					readNextFile( );
				} catch (NoSuchFileException e) {
					System.err
							.println( "Reached end of list of files while looking for Block "
									+ blockHash );
					System.exit( 1 );
				}
				return getBlock( blockHash );
			}

		}

		private void readNextFile( ) throws NoSuchFileException {
			if (!fileIterator.hasNext( ))
				throw new NoSuchFileException( "End of list of files" );

			List<File> tmpList = new ArrayList<>( 1 );
			tmpList.add( fileIterator.next( ) );
			logger.trace( "Going to read in file" + tmpList.get( 0 ).getName( ) );
			BlockFileLoader bfl = new BlockFileLoader( Utility.PARAMS, tmpList );

			for (Block b : bfl)
				blockBuffer.put( b.getHash( ), b );
		}

	}

	public static void main( String[] args ) {
		List<BlockFilter> filters = new ArrayList<>( );
		TransactionProducer op = new TransactionProducer(
				Utility.getDefaultFileList( ), filters );

		long transactions = 0;

		for (Transaction t : op)
			transactions++;

		System.out.println( "There are " + transactions
				+ " transactions currently in the blockchain" );
	}

}
