package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class PubKeyManager {

	private static final Logger logger = LogManager.getLogger("PubKeyManager");

	private static final String INSERT_PUBKEY = "INSERT INTO public_key (pubkey_hash,pubkey, valid_pubkey) Values (?,?,?)",

	GET_PK_ID = "SELECT id FROM public_key WHERE pubkey_hash = ?",

	UPDATE_PK = "UPDATE public_key" + " SET pubkey = ?" + " WHERE id = ?",

	INSERT_IGNORE_KEY_HASH = "INSERT IGNORE INTO public_key (pubkey_hash, valid_pubkey) VALUES(?,?)"

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

	public long insertRawPK(DatabaseConnection connection, byte[] pkBytes) {

		String pkHash = null;
		String pkHex = null;
		boolean valid = true;

		try {
			ECKey publicKey = ECKey.fromPublicOnly(pkBytes);
			pkHash = publicKey.toAddress(Utility.PARAMS).toString();
			pkHex = publicKey.getPublicKeyAsHex();
		} catch (IllegalArgumentException e) {
			String keyPrint = new String(pkBytes);
			logger.debug("Unable to create an ECKey from public key " + keyPrint, e);

			byte[] pubKeyHash = Utils.sha256hash160(pkBytes);
			pkHash = new Address(Utility.PARAMS, pubKeyHash).toString();
			pkHex = Utils.HEX.encode(pkBytes);

			valid = false;
		}

		if (pkHash == null || pkHex == null) {
			logger.fatal("Despite our best efforts, we were unable to create a public key and address from ["
					+ Utils.HEX.encode(pkBytes)
					+ "]");
			System.exit(1);
		}

		long pkId = -1;

		try {

			pkId = getPKIdFromPubKeyHash(connection, pkHash);

			if (pkId != -1) {

				PreparedStatement updatePK = connection.getPreparedStatement(UPDATE_PK);
				updatePK.setString(1, pkHex);
				updatePK.setLong(2, pkId);
				updatePK.executeUpdate();
				updatePK.close();
			} else {
				PreparedStatement insertPK = connection.getPreparedStatement(INSERT_PUBKEY);
				insertPK.setString(1, pkHash);
				insertPK.setString(2, pkHex);
				insertPK.setBoolean(3, valid);
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
			logger.fatal("Failed to Insert pubkey and adresse [" + pkHash + "]", e);
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
			insertAddr.setBoolean(2, true);
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
