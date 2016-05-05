package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ECKey;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class PubKeyManager {

	private static final Logger logger = LogManager.getLogger("PubKeyManager");

	private static final String INSERT_PUBKEY = "INSERT INTO public_key (pubkey_hash,pubkey) Values (?,?)",

	GET_PK_ID = "SELECT id FROM public_key WHERE pubkey_hash = ?",

	UPDATE_PK = "UPDATE public_key" + " SET pubkey = ?" + " WHERE id = ?",

	INSERT_IGNORE_KEY_HASH = "INSERT IGNORE INTO public_key (pubkey_hash) VALUES(?)"

	;

	private long getPKIdFromPubKeyHash(DatabaseConnection connection, String pkHash) {

		long pkId = -1;

		try {

			PreparedStatement getPKId = connection.getPreparedStatement(GET_PK_ID);
			getPKId.setString(1, pkHash);
			ResultSet resultPkId = getPKId.executeQuery();
			if (resultPkId.next())
				pkId = resultPkId.getLong(1);

			resultPkId.close();
			getPKId.close();

		} catch (SQLException e) {
			logger.fatal("Failed to query adresse [" + pkHash + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

		return pkId;
	}

	public long insertRawPK(DatabaseConnection connection, byte[] pubKey) {

		String pkHash = ECKey.fromPublicOnly(pubKey).toAddress(Utility.PARAMS).toString();

		long pkId = -1;

		try {

			pkId = getPKIdFromPubKeyHash(connection, pkHash);

			if (pkId != -1) {

				PreparedStatement updatePK = connection.getPreparedStatement(UPDATE_PK);
				updatePK.setBytes(1, pubKey);
				updatePK.setLong(2, pkId);
				updatePK.executeUpdate();
				updatePK.close();
			} else {
				PreparedStatement insertPK = connection.getPreparedStatement(INSERT_PUBKEY);
				insertPK.setString(1, pkHash);
				insertPK.setBytes(2, pubKey);
				insertPK.executeUpdate();

				ResultSet generatedKeys = insertPK.getGeneratedKeys();
				if (generatedKeys.next())
					pkId = generatedKeys.getLong(1);
				else {
					logger.fatal("Bad generatedKeySet from Address [" + pkHash + "]");
					connection.commit();
					connection.closeConnection();
					System.exit(1);
				}

				generatedKeys.close();
				insertPK.close();
			}

		} catch (SQLException e) {
			logger.fatal("Failed to Insert pubkey and adresse [" + pkHash + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		return pkId;
	}

	public long insertPubkeyHash(DatabaseConnection connection, String pkHash) {

		long id = -1;

		try {

			PreparedStatement insertAddr = connection.getPreparedStatement(INSERT_IGNORE_KEY_HASH);
			insertAddr.setString(1, pkHash);
			insertAddr.executeUpdate();
			ResultSet generatedKeys = insertAddr.getGeneratedKeys();

			if (generatedKeys.next())
				id = generatedKeys.getLong(1);
			else {
				logger.debug("Adress does exist in DB try to querry its ID: [" + pkHash + "]");

				id = getPKIdFromPubKeyHash(connection, pkHash);

				if (id == -1) {
					logger.fatal("Bad generatedKeySet from Address [" + pkHash + "]");
					connection.commit();
					connection.closeConnection();
					System.exit(1);
				}
			}

			generatedKeys.close();
			insertAddr.close();

		} catch (SQLException e) {
			logger.fatal("Failed to Insert adresse [" + pkHash + "]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}

		return id;
	}
}
