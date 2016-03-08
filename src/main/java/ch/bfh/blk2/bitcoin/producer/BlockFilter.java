package ch.bfh.blk2.bitcoin.producer;

import org.bitcoinj.core.Block;

/**
 * The producers use the strategy pattern to filter blocks out of the
 * blockchain. Use this interface for your strategy implementations. The filters
 * will be and-connected, meaning that only blocks will pass, that are approved
 * by all the filters. If you need more complex combinations of rules, you have
 * to implement the logic within a filter.
 *
 *
 * @author niklaus
 *
 */
public interface BlockFilter {

	/**
	 *
	 * @param block
	 *            The block that should be checked
	 * @return true if and only if the block is to be approved
	 */
	public boolean filter( Block block );

}
