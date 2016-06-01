package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.*;

import static org.junit.Assert.*;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.MultiSigScript;
import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.ScriptType;
import ch.bfh.blk2.bitcoin.util.PropertiesLoader;

public class MultiSigScriptTest {

	
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
		PropertiesLoader.getInstance().loadFromFile("test_db.properties");
		dbManager.resetDB();
		connection = new DatabaseConnection();
	}

	@After
	public void closeConnection() {
		connection.commit();
		connection.closeConnection();
	}
	
	private Script createMultisigOutputScript(int min,List<String> pks){
		
		int op_2 = ScriptOpCodes.OP_2,
			op_n = op_2 - 2 + min,
			op_m = op_2 - 2 + pks.size();
		
		
		ScriptBuilder sb = new ScriptBuilder();
		sb.addChunk(new ScriptChunk(op_n, null));
		for(String pkstr: pks){
			byte[] pkb = Utils.HEX.decode(pkstr);
			sb.addChunk(new ScriptChunk(pkb.length,pkb));
		}
		sb.addChunk(new ScriptChunk(op_m, null));
		sb.addChunk(new ScriptChunk(ScriptOpCodes.OP_CHECKMULTISIG, null));
		
		return sb.build();
	}
	
	@Test
	public void writeMultisigOutputScript() throws SQLException{

		int min = 3,
			scriptSize = 20;
		
		String 
			pk0 ="04c98749a4415e6c8dfd5559d424e371e5ff233c32f9de342846b0204b8e215e9ab3b30013bb15221fbeb73b5219ff6897f73b67bb39947e5801a647b3b8e28c02",
			pk1 ="04ef36bf3e13ea3e4faa1f7428eaef483327fc013db1fbffae01ff88cf183fc5e53ea53d002ab40184b1c2ff136bfd324d9a15a1343e9ab674237d6c4acb635760",
			pk2 ="04bb6b667eb637f74eca54ee900d73b694ee966804a7a576d4a2c56b40c08a9536c7a5902649a0c6fb7c5ee92e522d1fb981bd46e2a4b3ec2e6788e2655bdd2eb6",
			pk3 ="041423a83a797484879395e4e2516604a579941d6b499cd8aea2a8b23ff2426225beff9e36c9a646b481aacb1bf1d965ac83451f26c3e6e076d62e6b1fa999433f",
			pk4 ="04b69287e516e9cbb4b4cb26af6d72d4759d3d98771a281067e2779178228fb65cbf9a5ccd2da2a740468a43d2e73c7a15c40e4b04649baa1140bf8de9943aa2a4";
		
		List<String> pks = new ArrayList<>();
		pks.add(pk0);
		pks.add(pk1);
		pks.add(pk2);
		pks.add(pk3);
		pks.add(pk4);
		
		Script script = createMultisigOutputScript(min, pks);
		MultiSigScript multiSig = new MultiSigScript(script, scriptSize, 1, 1);
		
		assertEquals(ScriptType.OUT_MULTISIG, multiSig.getType());
		
		multiSig.writeOutputScript(connection);
		String query = "SELECT * FROM out_script_multisig WHERE tx_index = 1 AND tx_id = 1";
		ResultSet result = DBHelper.runQuery(query, connection);
		
		assertTrue(result.next());
		assertEquals(1 ,result.getLong("tx_id"));
		assertEquals(1 ,result.getInt("tx_index"));
		assertEquals(scriptSize ,result.getLong("script_size"));
		assertEquals(min ,result.getLong("min_keys"));
		assertEquals(pks.size() ,result.getLong("max_keys"));
		result.close();
		
		
		query = "Select * from multisig_pubkeys WHERE tx_id = 1 AND tx_index = 1";
		result = DBHelper.runQuery(query, connection);
		
		Map<Integer,Long> pkIds= new HashMap<>();
		while(result.next())
			pkIds.put(result.getInt("idx"),result.getLong("public_key_id"));
		
		result.close();
		
		assertEquals(pks.size(), pkIds.size());
		
		query = "SELECT * FROM public_key WHERE id = ";
		
		for(Entry<Integer, Long> e : pkIds.entrySet()){
			int idx = e.getKey();
			long id = e.getValue();
			
			result = DBHelper.runQuery(query+id, connection);
			
			String pkRef = pks.get(idx);
			ECKey publicKey = ECKey.fromPublicOnly(Utils.HEX.decode(pkRef));
			String pkHash = publicKey.toAddress(new TestNet3Params()).toString();

			assertTrue(result.next());
			assertEquals(pkRef, result.getString("pubkey"));
			assertEquals(pkHash,result.getString("pubkey_hash"));
			assertTrue(result.getBoolean("valid_pubkey"));
			
			result.close();
		}
	}
}
