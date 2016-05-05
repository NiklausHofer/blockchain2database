package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Actually, we figured that the probability of two signatures within the
 * blockchain being idential is so low, that we don't check whether an entry for
 * the same signature exists already. The trade off between speed and duplicated
 * entries in the database then is so that we have decided to risk a duplicate
 * entry for what is probably every few hundret signatures.
 *
 * @author niklaus
 *
 */
public class SigManager {

	private static final Logger logger = LogManager.getLogger("SigManager");

	// Not needet any more...
	//private final String SEARCH_SIGNATURE = "SELECT id FROM signature WHERE signature = ?";

	private final String INSERT_SIGNATURE_WITH_PUBKEY_CONNECTION = "INSERT INTO signature(signature, pubkey_id) VALUES( ?, ? );";

	/**
	 * @deprecated In future releases, all signatures will have to be connected
	 *             to a pubkey
	 *
	 *             Save the signature to the database and return the signature's
	 *             id.
	 *
	 * @param connection
	 *            the DatabaseConnection to be used
	 * @param signature
	 *            the signature to be saved and which's id is to be returned
	 * @return the database id of the signature
	 */
	@Deprecated
	public long saveAndGetSigId(DatabaseConnection connection, byte[] signature) {
		long signatureId = -1;

		PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_SIGNATURE_WITH_PUBKEY_CONNECTION);

		try {
			insertStatement.setBytes(1, signature);
			insertStatement.setNull(2, java.sql.Types.BIGINT);

			insertStatement.execute();

			ResultSet resultSet = insertStatement.getGeneratedKeys();
			if (resultSet.next())
				signatureId = resultSet.getLong(1);
			else
				throw new SQLException("No id for a newly inserted row was returned");

			resultSet.close();
			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to insert signature " + new String(signature), e);
			System.exit(1);
		}

		return signatureId;
	}

	/**
	 * Save the signature to the database and return the signature's id.
	 *
	 * The signature entry in the database will also get connected to the
	 * provided public key.
	 *
	 * @param connection
	 *            the DatabaseConnection to be used
	 * @param signature
	 *            the signature to be saved and which's id is to be returned
	 * @param pubkey
	 *            the pubkey which the signature is connected with
	 * @return the database id of the signature
	 */
	public long saveAndGetSigId(DatabaseConnection connection, byte[] signature, byte[] pubkey) {

		PubKeyManager pkm = new PubKeyManager();
		long pubkeyId = pkm.insertRawPK(connection, pubkey);

		return saveAndGetSigId(connection, signature, pubkeyId);
	}

	/**
	 * Save the signature to the database and return the signature's id.
	 *
	 * The signature entry in the database will also get connected to the
	 * provided public key.
	 *
	 * @param connection
	 *            the DatabaseConnection to be used
	 * @param signature
	 *            the signature to be saved and which's id is to be returned
	 * @param pubkey
	 *            the pubkey's ID which the signature is connected with
	 * @return the database id of the signature
	 */
	public long saveAndGetSigId(DatabaseConnection connection, byte[] signature, long pubkeyId) {
		long signatureId = -1;

		PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_SIGNATURE_WITH_PUBKEY_CONNECTION);

		try {
			insertStatement.setBytes(1, signature);
			insertStatement.setLong(2, pubkeyId);

			insertStatement.execute();

			ResultSet resultSet = insertStatement.getGeneratedKeys();
			if (resultSet.next())
				signatureId = resultSet.getLong(1);
			else
				throw new SQLException("No id for a newly inserted row was returned");

			insertStatement.close();
		} catch (SQLException e) {
			logger.fatal("Unable to insert signature " + new String(signature), e);
			System.exit(1);
		}

		return signatureId;
	}

}
