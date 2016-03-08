package ch.bfh.blk2.bitcoin.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.bitcoinj.core.Sha256Hash;

public class Sha256HashComparator implements Comparator<Sha256Hash>,
		Serializable {

	@Override
	public int compare( Sha256Hash o1, Sha256Hash o2 ) {
		byte[] b1 = o1.getBytes( );
		byte[] b2 = o2.getBytes( );

		if (b1.length > b2.length)
			return 1;

		if (b1.length < b2.length)
			return -1;

		for (int i = b1.length - 1; i >= 0; i--) {

			if (b1[i] < b2[i])
				return -1;
			if (b1[i] > b2[i])
				return 1;
		}

		return 0;
	}

}
