package ch.bfh.blk2.bitcoin.blockchain2database;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.bfh.blk2.bitcoin.util.DBKeyStore;

public class DBTest {

	/**
	 * test this table
	 *
	 * CREATE TABLE foo( bar INT AUTO_INCREMENT PRIMARY KEY, baz VARCHAR(25) );
	 */
	@Test
	public void someTest() {

		System.out.println("\n//// TEST 1");

		DatabaseConnection dbconnection = new DatabaseConnection();

		try {

			String sql = "CREATE TABLE IF NOT EXISTS foo(" + "bar INT AUTO_INCREMENT PRIMARY KEY,baz VARCHAR(25))";

			dbconnection.getPreparedStatement(sql).executeUpdate();

			sql = "INSERT INTO foo (baz) values ('foo'),('bar'),('baz'),('hello'),('lol')";

			dbconnection.getPreparedStatement(sql).executeUpdate();

			sql = "SELECT * FROM foo";

			ResultSet result;

			result = dbconnection.getPreparedStatement(sql).executeQuery();

			while (result.next()) {
				int bar = result.getInt("bar");
				String baz = result.getString("baz");

				System.out.print("bar: " + bar);
				System.out.println(", baz: " + baz);
			}

			sql = "DROP TABLE IF EXISTS foo";
			dbconnection.getPreparedStatement(sql).executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void initTest() {

		System.out.println("\n//// TEST 2");

		cleanUP();

		DatabaseConnection dbconnection = new DatabaseConnection();

		String sql = "";
		//DBInitialisator init = new DBInitialisator();
		//init.initializeDB();

		System.err.println("----foo");

		ResultSet result;

		sql = "show tables";

		try {
			result = dbconnection.getPreparedStatement(sql).executeQuery();

			while (result.next()) {
				String table = result.getString("Tables_in_test");
				System.out.println(table);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DBKeyStore keyStore = new DBKeyStore();
		String value = "true";

		assertNotNull(keyStore.getParameter(dbconnection, DBKeyStore.DYRTY));
		assertTrue(value.equals(keyStore.getParameter(dbconnection, DBKeyStore.DYRTY)));

		value = "foobar";

		keyStore.setParameter(dbconnection, DBKeyStore.DYRTY, value);
		assertNotNull(keyStore.getParameter(dbconnection, DBKeyStore.DYRTY));
		assertTrue(value.equals(keyStore.getParameter(dbconnection, DBKeyStore.DYRTY)));

		dbconnection.closeConnection();
		cleanUP();

	}

	@Test
	public void insertDuplicateTest() {
		System.out.println("\n//// TEST 3");

		String sql = "";
		DatabaseConnection dbconnection = new DatabaseConnection();
		ResultSet rs;

		PreparedStatement ps;
		sql = "";
		try {
			sql = "CREATE TABLE IF NOT EXISTS insert_test(id BIGINT AUTO_INCREMENT PRIMARY KEY, val VARCHAR(25) UNIQUE)";
			dbconnection.getPreparedStatement(sql).executeUpdate();

			sql = "INSERT IGNORE INTO insert_test (val) VALUES(?)";

			ps = dbconnection.getPreparedStatement(sql);
			ps.setString(1, "AAA");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				System.out.println("Generated Key:" + rs.getLong(1));

			ps.setString(1, "BBB");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				System.out.println("Generated Key:" + rs.getLong(1));

			ps.setString(1, "CCC");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				System.out.println("Generated Key:" + rs.getLong(1));

			ps.setString(1, "AAA");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				System.out.println("Generated Key:" + rs.getLong(1));

			sql = "DROP TABLE IF EXISTS insert_test";
			dbconnection.getPreparedStatement(sql).executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void inserBinary() {
		System.out.println("\n//// TEST 4");

		String sql = "";
		DatabaseConnection dbconnection = new DatabaseConnection();
		ResultSet rs;

		PreparedStatement ps;

		byte[] b = new String("foobarbaz hello").getBytes();

		sql = "";
		try {
			sql = "CREATE TABLE IF NOT EXISTS insert_binary("
					+ "id BIGINT AUTO_INCREMENT PRIMARY KEY, val VARBINARY(25) UNIQUE)"
					+ "ENGINE = MEMORY";
			dbconnection.getPreparedStatement(sql).executeUpdate();

			sql = "INSERT INTO insert_binary (val) VALUES(?)";

			ps = dbconnection.getPreparedStatement(sql);
			ps.setBytes(1, b);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				System.out.println("Generated Key:" + rs.getLong(1));

			sql = "SELECT val FROM insert_binary";
			ps = dbconnection.getPreparedStatement(sql);
			rs = ps.executeQuery();
			if (rs.next())
				System.out.println(new String(rs.getBytes("val")));

			sql = "DROP TABLE IF EXISTS insert_binary";
			dbconnection.getPreparedStatement(sql).executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void multySelect() {
		try {

			System.out.println("\n//// TEST 5");

			DatabaseConnection dbconnection = new DatabaseConnection();
			String sql = "CREATE TABLE IF NOT EXISTS abc("
					+ " a INT AUTO_INCREMENT PRIMARY KEY,"
					+ " b VARCHAR(25),"
					+ " c INT )";
			dbconnection.getPreparedStatement(sql).executeUpdate();

			sql = "INSERT INTO abc (b,c) VALUES"
					+ "('a',1),('b',1),('c',1),('d',1),('e',1),('f',1),"
					+ "('g',2),('h',2),('i',2),('j',2),('k',2),('l',2),"
					+ "('m',3),('n',3),('o',3),('p',3),('q',3),('r',3),"
					+ "('s',1),('t',1),('u',1),('v',1),('w',1),('x',1),"
					+ "('y',1),('z',1)";

			dbconnection.getPreparedStatement(sql).executeUpdate();

			//*****************************

			sql = "SELECT a,b,c FROM abc";

			List<Integer> list = new ArrayList<>();
			list.add(3);
			list.add(2);

			StringBuilder sb = new StringBuilder();

			if (!list.isEmpty()) {
				sb.append(sql);
				sb.append(" WHERE c IN (?");

				for (int i = 1; i < list.size(); i++)
					sb.append(",?");

				sb.append(")");

				System.out.println(sb.toString());

				PreparedStatement statement = dbconnection.getPreparedStatement(sb.toString());

				for (int i = 0; i < list.size(); i++)
					statement.setObject(i + 1, list.get(i));

				ResultSet result = statement.executeQuery();

				while (result.next()) {

					System.out.print(result.getInt("a") + " : ");
					System.out.print(result.getString("b") + " : ");
					System.out.println(result.getInt("c"));
				}

			}

			sql = "DROP TABLE IF EXISTS abc";
			dbconnection.getPreparedStatement(sql).executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void cleanUP() {

		String sql = "";

		DatabaseConnection dbconnection = new DatabaseConnection();

		try {
			sql = "DROP TABLE IF EXISTS block,transaction,output,input,"
					+ "large_out_script,large_in_script,small_out_script,small_in_script,"
					+ "parameter";
			dbconnection.getPreparedStatement(sql).executeUpdate();
			dbconnection.closeConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
