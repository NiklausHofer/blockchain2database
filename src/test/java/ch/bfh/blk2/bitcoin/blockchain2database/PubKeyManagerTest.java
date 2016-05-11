package ch.bfh.blk2.bitcoin.blockchain2database;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses.PubKeyManager;

public class PubKeyManagerTest {

	private static DBManager dbManager;
	private DatabaseConnection connection;
	
	@BeforeClass
	public static void prepare(){
		dbManager = new DBManager();
		dbManager.initDB();
	}
	
	@AfterClass
	public static void cleanup(){
		dbManager.cleanDB();
	}
	
	@Before
	public void initDB(){
		dbManager.resetDB();
		connection = new DatabaseConnection("src/resources/test_db.properties");
	}
	
	@After
	public void closeConnection(){
		connection.commit();
		connection.closeConnection();
	}
	
	@Test
	public void fooTest(){
		assertTrue(true);
	}
	
	@Test
	public void emptyPubkeyTest(){
		byte[] pubKey = new byte[0];
		
		PubKeyManager pkManager = new PubKeyManager();
		pkManager.insertRawPK(connection, pubKey);
		
		ResultSet rs = DBHelper.runQuery("SELECT pubkey_hash, pubkey FROM public_key WHERE pubkey = '';", connection);
		
		try {
			assertTrue("There should be at least one result", rs.next());

			String pubkey = rs.getString("pubkey");
			String pubkey_hash = rs.getString("pubkey_hash");
			
			assertEquals("Empty key should still be emtpy", "", pubkey);
			assertEquals("There is a real hash for the empty key though", "mwy5FX7MVgDutKYbXBxQG5q7EL6pmhHT58", pubkey_hash);
			
			assertFalse("There should be at most one result", rs.next());
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}
		
	}
}
