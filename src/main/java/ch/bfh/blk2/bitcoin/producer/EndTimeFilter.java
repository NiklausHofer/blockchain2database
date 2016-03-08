package ch.bfh.blk2.bitcoin.producer;

import java.util.Date;

import org.bitcoinj.core.Block;

/**
 * Filters out blocks that have a timestamp later that a certain date
 *
 * @author niklaus
 *
 */
public class EndTimeFilter implements BlockFilter {

	private Date end;

	/**
	 *
	 * @param end
	 *            the end date to filter for. All blocks with a timestamp after
	 *            that will be filtered out.
	 */
	public EndTimeFilter( Date end ) {
		this.end = end;
	}

	/**
	 * Applies the filter to a block
	 *
	 * @param block
	 *            the block to check/filter
	 * @return true if and only if the block was formed (timestamped) before the
	 *         end date
	 */
	@Override
	public boolean filter( Block block ) {
		return block.getTime( ).before( end );
	}
}
