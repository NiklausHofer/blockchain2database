package ch.bfh.blk2.bitcoin.blockchain2database;

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

		DatabaseConnection dbconnection = new DatabaseConnection();

		String sql="SELECT * FROM foo";

		ResultSet result;

		try{
			result = dbconnection.getPreparedStatement(sql).executeQuery(sql);
    
			while(result.next()){
				int bar  = result.getInt("bar");
				String baz = result.getString("baz");

				System.out.print("bar: " + bar);
				System.out.println(", baz: " + baz );
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}