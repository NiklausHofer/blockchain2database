package ch.bfh.blk2.bitcoin.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.bitcoinj.core.Sha256Hash;

/**
 * Compare two Sha256Hash instances. Other than the compare method they provide themselves,
 * which operates on the 32bit hashCode() generated from the 256bit Value, this actually
 * compares the entire values, byte per byte.
 * 
 * @author niklaus
 */
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
