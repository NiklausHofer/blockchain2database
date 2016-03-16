package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Address;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class AddressUpdater {

	private static final Logger logger = LogManager.getLogger("AddressUpdater");

	private static final String ADDR_ID = "addr_id", GET_ADDR_ID = "SELECT addr_id FROM address WHERE addr_hash = ?",
			INSERT_ADDR = "INSERT INTO address (public_key,addr_hash) VALUES(?,?)";

	private String addressHash;
	private byte[] publicKey;
	private long id = -1;

	public AddressUpdater(Address address) {
		this.publicKey = address.getHash160();
		this.addressHash = address.toString();
	}

	/**
	 *
	 * @param dbconnection
	 * @return returns the primary key of the address (addr_id)
	 */
	public long update(DatabaseConnection dbconnection) {
		PreparedStatement statement0, statement1;
		ResultSet result0, result1;

		try {
			statement0 = dbconnection.getPreparedStatement(GET_ADDR_ID);
			statement0.setString(1, addressHash);
			result0 = statement0.executeQuery();

			if (result0.next())
				id = result0.getLong(ADDR_ID);
			else {
				statement1 = dbconnection.getPreparedStatement(INSERT_ADDR);
				statement1.setBytes(1, publicKey);
				statement1.setString(2, addressHash);
				statement1.executeUpdate();
				result1 = statement1.getGeneratedKeys();
				if (result1.next())
					id = result1.getLong(1);
				result1.close();
				statement1.close();
			}

			result0.close();
			statement0.close();

		} catch (SQLException e) {
			logger.fatal("Failed to update/insert Address " + addressHash);
			logger.fatal(e);
			System.exit(1);
		}

		return id;
	}

}
