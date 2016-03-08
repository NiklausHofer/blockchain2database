package ch.bfh.blk2.bitcoin.producer;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

import ch.bfh.blk2.bitcoin.comparator.Sha256HashComparator;
import ch.bfh.blk2.bitcoin.util.Utility;

/**
 * This class works much the same as TransactionProducer, except that it gives
 * out blocks in the reverse order.
 *
 * All blocks will be returned in a logical (reverse!) order. That is to say, if
 * a transaction T_A is spent by a transaction T_B, then T_B will always be
 * returned before T_A.
 *
 * Please note, that each Iterator can use up to two gigabytes of memory, so be
 * careful when using it.
 *
 * @author niklaus
 *
 */
public class ReverseTransactionProducer implements
		Iterable<Transaction> {

	private static final int DEFAULT_MIN_BLOCK_DEPTH = 6;
	private static final Logger logger = LogManager
			.getLogger( "ReverseTransactionProducer" );

	private List<Sha256Hash> orderedBlockHashes;
	private List<File> blockChainFiles;
	// private List<Block> blockBuffer;

	private List<BlockFilter> filters;

	private NetworkParameters params = new MainNetParams( );

	/**
	 * Uses the default minimum block depth 6
	 *
	 * @param blockChainFiles
	 *            a list of unparsed Blockchain Files
	 * @param filters
	 *            the filters to be applied to blocks (AND connected)
	 */
	public ReverseTransactionProducer( List<File> blockChainFiles,
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
	public ReverseTransactionProducer( List<File> blockChainFiles,
			List<BlockFilter> filters, int minBlockDepth ) {
		this.blockChainFiles = blockChainFiles;
		this.filters = filters;

		BlockSorter bs = new BlockSorter( this.blockChainFiles );
		this.orderedBlockHashes = bs.getLongestBranch( );

		if (minBlockDepth > orderedBlockHashes.size( ))
			for (int i = 0; i < minBlockDepth; i++)
				orderedBlockHashes.remove( orderedBlockHashes.size( ) - 1 );

		if (this.orderedBlockHashes.size( ) < 1) {
			System.err
					.println( "BlockSorter is malfunctioning or an invalid list of files was provided" );
			System.exit( 1 );
		}

		/* Reverse sort the files */
		this.blockChainFiles.sort( Collections.reverseOrder( ) );
		logger.trace( this.blockChainFiles );

		/* Reverse the list of blocks */
		Collections.reverse( this.orderedBlockHashes );

		logger.debug( "Got a chain of " + this.orderedBlockHashes.size( )
				+ "blocks" );
	}

	/**
	 * WARNING! Each Iterator can use up to two Gigabytes of memory. So be
	 * careful when using this.
	 */
	@Override
	public Iterator<Transaction> iterator( ) {
		return new ReverseTransactionIterator( );
	}

	public class ReverseTransactionIterator implements Iterator<Transaction> {
		private Iterator<Sha256Hash> hashIterator;
		private Iterator<File> fileIterator;

		private Iterator<Transaction> transactionIterator;

		private Map<Sha256Hash, Block> blockBuffer;
		private List<Block> validBlocks;
		private int blockCount = 0;

		private ReverseTransactionIterator( ) {
			hashIterator = ReverseTransactionProducer.this.orderedBlockHashes
					.iterator( );
			fileIterator = ReverseTransactionProducer.this.blockChainFiles
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

			/*
			 * Build a reverse list of Transactions. The list is immutable which
			 * is why we have to copy the elements into a new list before
			 * reversing it.
			 */

			List<Transaction> trs = new ArrayList<>( );
			trs.addAll( validBlocks.get( 0 ).getTransactions( ) );
			Collections.reverse( trs );
			transactionIterator = trs.iterator( );
			logger.debug( "Giving out block #" + blockCount++ );
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
			for (BlockFilter filter : ReverseTransactionProducer.this.filters)
				if (!filter.filter( b ))
					return false;
			return true;
		}

		private Block getBlock( Sha256Hash blockHash ) {
			logger.debug( "Got asked for block " + blockHash );
			if (blockBuffer.containsKey( blockHash )) {
				logger.debug( "Found the block in the blockBuffer" );
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
			logger.debug( "Going to read in file" + tmpList.get( 0 ).getName( ) );
			BlockFileLoader bfl = new BlockFileLoader( params, tmpList );

			for (Block b : bfl)
				blockBuffer.put( b.getHash( ), b );
		}

	}

	public static void main( String[] args ) {
		List<BlockFilter> filters = new ArrayList<>( );
		ReverseTransactionProducer op = new ReverseTransactionProducer(
				Utility.getDefaultFileList( ), filters );

		long transactions = 0;

		for (Transaction t : op)
			transactions++;

		System.out.println( "There are " + transactions
				+ " transactions currently in the blockchain" );
	}

}
