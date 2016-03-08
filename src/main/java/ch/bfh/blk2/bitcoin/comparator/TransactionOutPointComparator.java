package ch.bfh.blk2.bitcoin.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.bitcoinj.core.TransactionOutPoint;

public class TransactionOutPointComparator implements
		Comparator<TransactionOutPoint>, Serializable {

	Sha256HashComparator sha256HashComparator;

	public TransactionOutPointComparator( ) {
		sha256HashComparator = new Sha256HashComparator( );
	}

	@Override
	public int compare( TransactionOutPoint to1, TransactionOutPoint to2 ) {
		int val = sha256HashComparator
				.compare( to1.getHash( ), to2.getHash( ) );

		if (val != 0)
			return val;

		return Long.compare( to1.getIndex( ), to2.getIndex( ) );
	}

}
