package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class P2RawPubKeyScript implements OutputScript {
	
	private static final Logger logger = LogManager.getLogger("P2RawPubKeyScript");
	
	private static final String 
	        INSERT_PUBKEY =
			"INSERT INTO public_key (pubkey_hash,pubkey) Values (?,?)",
			
			GET_PK_ID = 
			"SELECT id FROM public_key WHERE pubkey_hash = ?",
			
			UPDATE_PK =
			"UPDATE public_key"
			+ " SET pubkey = ?"
			+ " WHERE id = ?",
			
			INSERT_P2RAW_PUBKEY_SCRIPT =
			"INSERT INTO out_script_p2raw_pub_key (tx_id,tx_index,script_size,public_key_id) VALUES (?,?,?,?)"
			;
	

	private Script script;

	private int scriptSize,
		txIndex;
	
	private long txId;
	
	public P2RawPubKeyScript(Script script,int scriptSize,long txId,int txIndex) {
		if (!script.isSentToRawPubKey())
			throw new IllegalArgumentException("Script must be of type Pay to RawPybKey");

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
	}

	@Override
	public OutputType getType() {
		return OutputType.P2RAWPUBKEY;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		
		String keyHash = script.getToAddress(Utility.PARAMS, true).toString();
		byte [] pubKey= script.getPubKey();
		
		PreparedStatement getPKId = connection.getPreparedStatement(GET_PK_ID);
		long pkId = -1;
		
		try{
		
		getPKId.setString(1, keyHash);
		ResultSet resultPkId = getPKId.executeQuery();
		
		if(resultPkId.next()){
			
			pkId = resultPkId.getLong(1);
			
			PreparedStatement updatePK = connection.getPreparedStatement(UPDATE_PK);
			updatePK.setBytes(1, pubKey);
			updatePK.setLong(2, pkId);
			updatePK.executeUpdate();
			updatePK.close();
		}else{
			PreparedStatement insertPK = connection.getPreparedStatement(INSERT_PUBKEY);
			insertPK.setString(1, keyHash);
			insertPK.setBytes(2, pubKey);
			insertPK.executeUpdate();
			
			ResultSet generatedKeys = insertPK.getGeneratedKeys();
			if(generatedKeys.next()){
				pkId = generatedKeys.getLong(1);
			}else{
				logger.fatal("Bad generatedKeySet from Address [" + keyHash + "]");
				logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
			
			generatedKeys.close();
			insertPK.close();
		}
		resultPkId.close();
		getPKId.close();
		
		}catch(SQLException e){
			logger.fatal("Failed to Querry/Insert adresse [" + keyHash + "]");
			logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		
		try{
			
			PreparedStatement inserRawPKScript = connection.getPreparedStatement(INSERT_P2RAW_PUBKEY_SCRIPT);
			inserRawPKScript.setLong(1, txId);
			inserRawPKScript.setInt(2, txIndex);
			inserRawPKScript.setInt(3, scriptSize);
			inserRawPKScript.setLong(4, pkId);
			inserRawPKScript.executeUpdate();
			
		}catch(SQLException e){
			logger.fatal("Failed to write P2raw PK script");
			logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
