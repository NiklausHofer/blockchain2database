package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBTest {


	/**
	 * test this table
	 * 
	 * CREATE TABLE foo(
	 *   bar INT AUTO_INCREMENT PRIMARY KEY,
	 *   baz VARCHAR(25) 
	 * );
	 */
	@Test
	public void someTest(){

		System.out.println("\n//// TEST 1");
		
		DatabaseConnection dbconnection = new DatabaseConnection();
		
		try{
			
		String sql="CREATE TABLE IF NOT EXISTS foo("
				+ "bar INT AUTO_INCREMENT PRIMARY KEY,baz VARCHAR(25))";
		
		dbconnection.getPreparedStatement(sql).executeUpdate();
		
		sql="INSERT INTO foo (baz) values ('foo'),('bar'),('baz'),('hello'),('lol')";
		
		dbconnection.getPreparedStatement(sql).executeUpdate();
		
		sql="SELECT * FROM foo";

		ResultSet result;

		
			result = dbconnection.getPreparedStatement(sql).executeQuery();

			while(result.next()){
				int bar  = result.getInt("bar");
				String baz = result.getString("baz");

				System.out.print("bar: " + bar);
				System.out.println(", baz: " + baz );
			}
			
			
			sql="DROP TABLE IF EXISTS foo";
			dbconnection.getPreparedStatement(sql).executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

	@Test
	public void initTest(){

		System.out.println("\n//// TEST 2");
		
		DatabaseConnection dbconnection = new DatabaseConnection();

		String sql="";
		DBInitialisator init = new DBInitialisator();
		init.initializeDB();

		ResultSet result;

		sql="show tables";

		try{
			result = dbconnection.getPreparedStatement(sql).executeQuery();

			while(result.next()){
				String table = result.getString("Tables_in_test");
				System.out.println(table);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cleanUP();

	}

	
	@Test
	public void insertDuplicateTest(){
		System.out.println("\n//// TEST 3");
		
		String sql = "";
		DatabaseConnection dbconnection = new DatabaseConnection();
		ResultSet rs;

		PreparedStatement ps;
		sql="";
		try {
			sql="CREATE TABLE IF NOT EXISTS insert_test(id BIGINT AUTO_INCREMENT PRIMARY KEY, val VARCHAR(25) UNIQUE)";
			dbconnection.getPreparedStatement(sql).executeUpdate();
			
			sql="INSERT IGNORE INTO insert_test (val) VALUES(?)";
			
			ps =dbconnection.getPreparedStatement(sql);
			ps.setString(1, "AAA");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) System.out.println("Generated Key:"+ rs.getLong(1));

			ps.setString(1, "BBB");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) System.out.println("Generated Key:"+ rs.getLong(1));
			
			ps.setString(1, "CCC");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) System.out.println("Generated Key:"+ rs.getLong(1));
			
			ps.setString(1, "AAA");
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) System.out.println("Generated Key:"+ rs.getLong(1));
			
			sql="DROP TABLE IF EXISTS insert_test";
			dbconnection.getPreparedStatement(sql).executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void inserBinary(){
		System.out.println("\n//// TEST 4");
		
		String sql = "";
		DatabaseConnection dbconnection = new DatabaseConnection();
		ResultSet rs;

		PreparedStatement ps;
		
		byte[] b = new String("foobarbaz hello").getBytes();
		
		sql="";
		try {
			sql="CREATE TABLE IF NOT EXISTS insert_binary("
					+ "id BIGINT AUTO_INCREMENT PRIMARY KEY, val VARBINARY(25) UNIQUE)"
					+ "ENGINE = MEMORY";
			dbconnection.getPreparedStatement(sql).executeUpdate();
			
			sql="INSERT INTO insert_binary (val) VALUES(?)";
			
			ps =dbconnection.getPreparedStatement(sql);
			ps.setBytes(1, b);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) System.out.println("Generated Key:"+ rs.getLong(1));
			
			
			sql= "SELECT val FROM insert_binary";
			ps =dbconnection.getPreparedStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) System.out.println(new String(rs.getBytes("val")));
			
			
			
			sql="DROP TABLE IF EXISTS insert_binary";
			dbconnection.getPreparedStatement(sql).executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void cleanUP(){
		
		String sql="";

		DatabaseConnection dbconnection = new DatabaseConnection();

		try {
			sql="DROP TABLE IF EXISTS block,transaction,output,input,script,address,block_transaction";
			dbconnection.getPreparedStatement(sql).executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
