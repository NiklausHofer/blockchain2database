package ch.bfh.blk2.bitcoin.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.bitcoinj.core.Address;

public class AddressComparator implements Comparator<Address>, Serializable {

	@Override
	public int compare( Address o1, Address o2 ) {

		if (o1.getVersion( ) > o2.getVersion( ))
			return 1;

		if (o1.getVersion( ) < o2.getVersion( ))
			return -1;

		byte[] b1 = o1.getHash160( );
		byte[] b2 = o2.getHash160( );

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
