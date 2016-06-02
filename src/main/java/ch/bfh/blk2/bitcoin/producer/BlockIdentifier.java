package ch.bfh.blk2.bitcoin.producer;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;

/**
 * This class identifies a block in a memory efficient way. It stores a block's
 * hash and the parent's hash, so it can be used to build the blockchain.
 *
 * @author niklaus
 *
 */
public class BlockIdentifier {

	private Sha256Hash blockHash;
	private Sha256Hash parentHash;
	private String file = "";

	private int depth;
	private BlockIdentifier parent;

	/**
	 *
	 * @param blk the Block from which this identifier should be built
	 * @param filename The .blk file where the block is stored
	 */
	public BlockIdentifier(Block blk, String filename) {
		this.blockHash = blk.getHash();
		this.parentHash = blk.getPrevBlockHash();
		this.file = filename;
	}

	/**
	 * Creates a dummy block identifier with no real data associated.
	 * 
	 * @param hash The hash of this fake block
	 */
	public BlockIdentifier(Sha256Hash hash) {
		blockHash = hash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockHash == null) ? 0 : blockHash.hashCode());
		result = prime * result + ((parentHash == null) ? 0 : parentHash.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockIdentifier other = (BlockIdentifier) obj;
		if (blockHash == null) {
			if (other.blockHash != null)
				return false;
		} else if (!blockHash.equals(other.blockHash))
			return false;
		if (parentHash == null) {
			if (other.parentHash != null)
				return false;
		} else if (!parentHash.equals(other.parentHash))
			return false;
		return true;
	}

	/**
	 *
	 * @return the hash of this block
	 */
	public Sha256Hash getBlockHash() {
		return blockHash;
	}

	/**
	 *
	 * @return the hash of this block's parent
	 */
	public Sha256Hash getParentHash() {
		return parentHash;
	}

	/**
	 * We use the depth of a block in the BlockSorter to measure the length of
	 * the blockchain. That way, we find the longest 'branch' of the block
	 * chain, in case it has orphan blocks.
	 *
	 * @param d
	 *            The depth of this block
	 */
	public void setDepth(int d) {
		depth = d;
	}

	/**
	 * Manually set the parent of a block
	 *
	 * @param p
	 *            the parent of this block
	 */
	public void setParent(BlockIdentifier p) {
		parent = p;
	}

	/**
	 * Use this to go back through the blockchain
	 * (getParent().getParent().getParent()...)
	 *
	 * @return the block's parent
	 */
	public BlockIdentifier getParent() {
		return parent;
	}

	/**
	 *
	 * @return The depth of the block (the number of the block in the
	 *         blockchain)
	 */
	public int getDepth() {
		return depth;
	}

	public String getFilename() {
		return file;
	}

}
