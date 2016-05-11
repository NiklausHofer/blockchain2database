import org.bitcoinj.core.Address;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;


public class WARGH {
	
	public static void main(String[] args) {
		byte[] sixteen = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		byte[] eight = {0, 0, 0, 0, 0, 0, 0, 0};
		byte[] four = {0, 0, 0, 0};
		byte[] zero = {};
		
		System.out.println("sixteen: " + Utils.HEX.encode(sixteen).toString());
		System.out.println("sixteen: " + Utils.sha256hash160(sixteen));
		System.out.println("eight: " + Utils.HEX.encode(eight).toString());
		System.out.println("eight: " + Utils.sha256hash160(eight));
		System.out.println("four: " + Utils.HEX.encode(four).toString());
		System.out.println("four: " + Utils.sha256hash160(four));
		System.out.println("zero: " + Utils.HEX.encode(zero).toString());
		System.out.println("zero: " + Utils.sha256hash160(zero));
		
		Address addr = new Address(new TestNet3Params(), Utils.sha256hash160(zero));
		
		System.out.println("Address: " + addr.toString());
	}
	

}
