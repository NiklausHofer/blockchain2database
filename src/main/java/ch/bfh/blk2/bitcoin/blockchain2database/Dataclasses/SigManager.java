package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Utils;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Manages the signatures in the database. For now it does not do a whole lot,
 * other than just writing signatures into a table. It does not even deduplicate
 * them because we figured that the probability of two signatures within the
 * blockchain being identical is so low, that the additional time needet to
 * search for duplicates is not worth it.
 *
 * @author niklaus
 *
 */
public class SigManager {

	private static final Logger logger = LogManager.getLogger("SigManager");
	
	private static SigManager instance = null;
	
	private SigManager(){};
	
	public static SigManager getInstance(){
		if( instance == null)
			instance = new SigManager();
		return instance;
	}

	private final String INSERT_SIGNATURE_WITH_PUBKEY_CONNECTION = "INSERT INTO signature(signature) VALUES( ? );";

	/**
	 * Save the signature to the database and return the signature's id.
	 *
	 * @param connection
	 *            the DatabaseConnection to be used
	 * @param signature
	 *            the signature to be saved and which's id is to be returned
	 * @return the database id of the signature
	 */
	public long saveAndGetSigId(DatabaseConnection connection, byte[] signature) {
		long signatureId = -1;

		PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_SIGNATURE_WITH_PUBKEY_CONNECTION);
		
		try {
			insertStatement.setString(1, Utils.HEX.encode(signature));

			insertStatement.execute();

			ResultSet resultSet = insertStatement.getGeneratedKeys();
			if (resultSet.next())
				signatureId = resultSet.getLong(1);
			else
				throw new SQLException("No id for a newly inserted row was returned");

			resultSet.close();
			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to insert signature " + Utils.HEX.encode(signature), e);
			System.exit(1);
		}

		return signatureId;
	}

}
