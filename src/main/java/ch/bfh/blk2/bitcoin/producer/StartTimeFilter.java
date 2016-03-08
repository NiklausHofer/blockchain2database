package ch.bfh.blk2.bitcoin.producer;

import java.util.Date;

import org.bitcoinj.core.Block;

/**
 * Filters out blocks that have a timestamp before a certain date
 *
 * @author niklaus
 *
 */
public class StartTimeFilter implements BlockFilter {

	private Date start;

	/**
	 *
	 * @param start
	 *            the start date to filter for. All blocks timestamped before
	 *            that will be filtered out.
	 */
	public StartTimeFilter( Date start ) {
		this.start = start;
	}

	/**
	 * Applies the filter to a block
	 *
	 * @param block
	 *            the block to check/filter
	 * @return true if and only if the block was formed (timestamped) after the
	 *         startDate
	 */
	@Override
	public boolean filter( Block block ) {
		return block.getTime( ).after( start );
	}
}
