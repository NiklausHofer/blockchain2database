package ch.bfh.blk2.bitcoin.blockchain2database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBInitialisator {

	private static final String
	BLOCK= "CREATE TABLE IF NOT EXISTS block("
			+"blk_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
			+"difficulty BIGINT,"
			+"hash VARCHAR(64),"
			+"prev_blk_id BIGINT,"
			+"mrkl_root VARCHAR(64),"
			+"time TIMESTAMP DEFAULT 0,"
			+"tx_count BIGINT,"
			+"height BIGINT,"
			+"version BIGINT,"
			+"nonce BIGINT,"
			+"output_amount BIGINT,"
			+"input_amount BIGINT"
			+")ENGINE = MEMORY;",
			TRANSACTION="CREATE TABLE IF NOT EXISTS transaction("
					+"tx_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
					+"version BIGINT,"
					+"lock_time TIMESTAMP DEFAULT 0,"
					+"blk_time TIMESTAMP DEFAULT 0,"
					+"input_count BIGINT,"
					+"output_count BIGINT,"
					+"output_amount BIGINT,"
					+"input_amount BIGINT,"
					+"coinbase BOOL,"
					+"blk_id BIGINT,"
					+"tx_hash VARCHAR(64),"
					+"FOREIGN KEY(blk_id) REFERENCES block(blk_id)"
					+")ENGINE = MEMORY;",
					OUTPUT="CREATE TABLE IF NOT EXISTS output("
							+"output_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
							+"amount BIGINT,"
							+"tx_id BIGINT,"
							+"tx_index BIGINT,"
							+"spent BOOL,"
							+"spent_by_input BIGINT,"
							+"spent_in_tx BIGINT,"
							+"spent_at TIMESTAMP DEFAULT 0,"
							+"address_id BIGINT,"
							+"FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)"
							+")ENGINE = MEMORY;",

							INPUT="CREATE TABLE IF NOT EXISTS input("
									+"input_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
									+"prev_output_id BIGINT,"
									+"tx_id BIGINT,"
									+"prev_tx_id BIGINT,"
									+"prev_output_index BIGINT,"
									+"sequence_number BIGINT,"
									+"amount BIGINT,"
									+"FOREIGN KEY(prev_output_id) REFERENCES output(output_id),"
									+"FOREIGN KEY(tx_id) REFERENCES transaction(tx_id)"
									+")ENGINE = MEMORY;",

									SCRIPT="CREATE TABLE IF NOT EXISTS script("
											+"script_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
											+"script_length BIGINT,"
											+"script_code BLOB,"
											+"input_id BIGINT,"
											+"outpu_id BIGINT"
											+")ENGINE = INNODB;",

											ADDRESS="CREATE TABLE IF NOT EXISTS address("
													+"addr_id  BIGINT AUTO_INCREMENT PRIMARY KEY,"
													+"public_key BIGINT,"
													+"addr_hash VARCHAR(35)"
													+")ENGINE = MEMORY;"
							  ;
	
	
	public void initializeDB(){
		
		DatabaseConnection dbconn = new DatabaseConnection();
		
		try {
			PreparedStatement ps;
			
			ps= dbconn.getPreparedStatement(BLOCK);
			ps.execute();
			ps.close();
			
			ps= dbconn.getPreparedStatement(TRANSACTION);
			ps.execute();
			ps.close();
			
			ps= dbconn.getPreparedStatement(OUTPUT);
			ps.execute();
			ps.close();
			
			ps= dbconn.getPreparedStatement(INPUT);
			ps.execute();
			ps.close();
			
			ps= dbconn.getPreparedStatement(SCRIPT);
			ps.execute();
			ps.close();
			
			ps= dbconn.getPreparedStatement(ADDRESS);
			ps.execute();
			ps.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			dbconn.closeConnection();
		}
	}


}
