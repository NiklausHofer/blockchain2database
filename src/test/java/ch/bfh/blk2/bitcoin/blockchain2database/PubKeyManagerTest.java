package ch.bfh.blk2.bitcoin.blockchain2database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.TestNet3Params;
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

	@Test
	public void invalidPubKeyTest() {
		CharSequence hexKey = "9e000000416e6f7468657220746578742077617320656d62656464656420696e746f2074686520626c6f636b20636861696e2e20546865207374616e6461726420";
		byte pkBytes[] = Utils.HEX.decode(hexKey);
		// This is an invalid key, so we can't turn it into an eckey

		PubKeyManager pkManager = PubKeyManager.getInstance();

		long first_pubkey_id = pkManager.insertRawPK(connection, pkBytes);
		long second_pubkey_id = pkManager.insertRawPK(connection, pkBytes);

		assertEquals("The same id should have been returned both times", first_pubkey_id, second_pubkey_id);

		ResultSet rs = DBHelper.runQuery("SELECT * FROM public_key", connection);
		try {
			assertTrue("There should be a result", rs.next());

			assertFalse("The key should be marked as invalid", rs.getBoolean("valid_pubkey"));

			assertFalse("There should be only one result", rs.next());
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}
	}

	@Test
	public void inserRawPubKeyTest() {
		CharSequence hexKey = "048a02cf4770a296da44b6a5ddac5acf2732c0c6d53d47754c7985eaf1baae541e0403c182b3f911444133a1c3871cdea7b47311f4d0452908f1c0e416c3c2f6b1";
		byte pkBytes[] = Utils.HEX.decode(hexKey);
		ECKey eckey = ECKey.fromPublicOnly(pkBytes);

		PubKeyManager pkManager = PubKeyManager.getInstance();

		String address = eckey.toAddress(new TestNet3Params()).toString();

		long pubkey_id = pkManager.insertRawPK(connection, pkBytes);

		ResultSet rs = DBHelper.runQuery("SELECT id FROM public_key WHERE pubkey_hash = '" + address + "';",
				connection);
		try {
			assertTrue("There should be a result", rs.next());
			assertEquals("Addresses should match", pubkey_id, rs.getInt("id"));
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}

		long second_pubkey_id = pkManager.insertRawPK(connection, pkBytes);
		assertEquals("ID should not change", pubkey_id, second_pubkey_id);
		long address_id = pkManager.insertPubkeyHash(connection, address);
		assertEquals("Should be the same entry", pubkey_id, address_id);

		rs = DBHelper.runQuery("SELECT * FROM public_key", connection);

		try {
			assertTrue("There should be a result", rs.next());
			assertFalse("There should be exactly one result", rs.next());
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}
	}

	@Test
	public void propperAddressCompletionTest() {
		CharSequence hexKey = "04e90b54f48c733d31862b048d324fccd0b127702d5e8bd743cdf637605fbe01564d5345faee1f05a25028fc76715bd0bca966200fffa35d4990ffdf239521de0b";
		byte[] pubKey = Utils.HEX.decode(hexKey);
		ECKey eckey = ECKey.fromPublicOnly(pubKey);

		PubKeyManager pkManager = PubKeyManager.getInstance();

		String address = eckey.toAddress(new TestNet3Params()).toString();

		long address_id = pkManager.insertPubkeyHash(connection, address);
		long pubkey_id = pkManager.insertRawPK(connection, pubKey);

		assertEquals("The ids should match", address_id, pubkey_id);

		ResultSet rs = DBHelper.runQuery("SELECT * FROM public_key", connection);

		try {
			assertTrue("There should be at least one result", rs.next());

			String pubkey = rs.getString("pubkey");
			String pubkey_hash = rs.getString("pubkey_hash");

			assertEquals("Addresses should match", address, pubkey_hash);
			assertEquals("Keys should still match", eckey.getPublicKeyAsHex(), pubkey);

			assertFalse("There should be at most one result", rs.next());
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}

		Utils.HEX.encode(pubKey);
	}

	@Test
	public void emptyPubkeyTest() {
		byte[] pubKey = new byte[0];

		PubKeyManager pkManager = PubKeyManager.getInstance();
		pkManager.insertRawPK(connection, pubKey);

		ResultSet rs = DBHelper.runQuery("SELECT pubkey_hash, pubkey FROM public_key WHERE pubkey = '';", connection);

		try {
			assertTrue("There should be at least one result", rs.next());

			String pubkey = rs.getString("pubkey");
			String pubkey_hash = rs.getString("pubkey_hash");

			assertEquals("Empty key should still be emtpy", "", pubkey);
			assertEquals("There is a real hash for the empty key though", "mwy5FX7MVgDutKYbXBxQG5q7EL6pmhHT58",
					pubkey_hash);

			assertFalse("There should be at most one result", rs.next());
		} catch (SQLException e) {
			fail("Unable to read SQL result: " + e.toString());
		}

	}
}
