package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.*;

import static org.junit.Assert.*;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.OuputScriptCreator;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.P2PKHInputScript;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.P2PKHashScript;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.ScriptType;
import ch.bfh.blk2.bitcoin.util.Utility;

public class P2PKHashScriptTest {
	
	private static DBManager dbManager;
	private DatabaseConnection connection;

	@BeforeClass
	public static void prepare() {
		dbManager = new DBManager();
		dbManager.initDB();
	}

	@AfterClass
	public static void cleanup() {
		dbManager.cleanDB();
	}

	@Before
	public void initDB() {
		dbManager.resetDB();
		connection = new DatabaseConnection("src/resources/test_db.properties");
	}

	@After
	public void closeConnection() {
		connection.commit();
		connection.closeConnection();
	}
	
	
	private Script createScript(byte[] pkHash){
		
		ScriptBuilder sb = new ScriptBuilder();
		sb.addChunk(new ScriptChunk(ScriptOpCodes.OP_DUP, null));
		sb.addChunk(new ScriptChunk(ScriptOpCodes.OP_HASH160, null));
		sb.addChunk(new ScriptChunk(pkHash.length, pkHash));
		sb.addChunk(new ScriptChunk(ScriptOpCodes.OP_EQUALVERIFY, null));
		sb.addChunk(new ScriptChunk(ScriptOpCodes.OP_CHECKSIG, null));
		
		return sb.build();

	}
	
	@Test
	public void writeP2PKHashScript() throws AddressFormatException, SQLException{
		
		//construct a P2PKHash output script
		
		Address addr = new Address(new TestNet3Params(), "mixzB3ZsBeHFTPkpoYVocFbwE1NBYmkksb");
		
		byte [] pkHash = addr.getHash160();
		int scriptSize = pkHash.length + 4; 

		Script script = createScript(pkHash);
		
		// construct dataclass
		P2PKHashScript p2pkh = new P2PKHashScript(script, scriptSize , 1, 1);
		assertEquals(ScriptType.OUT_P2PKHASH, p2pkh.getType());
		
		//save it
		p2pkh.writeOutputScript(connection);
		
		//get Database record
		
		String query = "SELECT * FROM out_script_p2pkh WHERE tx_id = 1 AND tx_index = 1";
		ResultSet result = DBHelper.runQuery(query, connection);
		
		//test reult
		assertTrue(result.next());
		assertEquals(1,result.getLong("tx_id"));
		assertEquals(1,result.getInt("tx_index"));
		assertEquals(scriptSize,result.getLong("script_size"));

		//get Public key hash;
		long pkId = result.getLong("public_key_id");
		query = "SELECT * FROM public_key WHERE id = "+pkId;
		
		result.close();		
		result = DBHelper.runQuery(query, connection);

		assertTrue(result.next());
		assertEquals(addr.toString(),result.getString("pubkey_hash"));
		assertNull(result.getString("pubkey"));
		assertTrue(result.getBoolean("valid_pubkey"));
		
	}
	
	@Test
	public void writeP2PKHdubPKH(){
		
		
		
	}
	

}
