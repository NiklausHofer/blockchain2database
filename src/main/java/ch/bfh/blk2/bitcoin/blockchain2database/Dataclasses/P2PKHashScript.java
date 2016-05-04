package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class P2PKHashScript implements OutputScript {
	
	private static final Logger logger = LogManager.getLogger("P2PKHashScript");

	private Script script;
	private long txId;
	private int txIndex,
		scriptSize;

	private final static String GET_PUBKEY_ID = "SELECT id FROM public_key WHERE pubkey_hash = ?",
			INSERT_IGNORE_KEY_HASH ="INSERT IGNORE INTO public_key (pubkey_hash) VALUES(?)",
			INSERT_P2PKH_SCRIPT = "INSERT INTO out_script_p2pkh (tx_id,tx_index,script_size,public_key_id) VALUES(?,?,?,?)";
	
	public P2PKHashScript(Script script,int scriptSize,long TxId,int TxIndex) {
		if (!script.isSentToAddress())
			throw new IllegalArgumentException("Script must be of type Pay to PubKeyHash");

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
	}
	

	@Override
	public OutputType getType() {
		return OutputType.P2PKHASH;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {

		try{		
		
		long pubkeyId = getPubkeyId(connection);
		
		PreparedStatement insertScriptStatement = connection.getPreparedStatement(INSERT_P2PKH_SCRIPT);
		insertScriptStatement.setLong(1, txId);
		insertScriptStatement.setInt(2, txIndex);
		insertScriptStatement.setInt(3, scriptSize);
		insertScriptStatement.setLong(4, pubkeyId);
		insertScriptStatement.executeLargeUpdate();
		}catch(SQLException e){
			logger.fatal("Failed to write P2PKH script");
			logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}
	
	
	private long getPubkeyId(DatabaseConnection connection) throws SQLException{
		
		String address = script.getToAddress(Utility.PARAMS, false).toString();
		long id = -1;
		
		PreparedStatement insertAddr = connection.getPreparedStatement(INSERT_IGNORE_KEY_HASH);
		insertAddr.setString(1,address);
		insertAddr.executeUpdate();
		ResultSet generatedKeys = insertAddr.getGeneratedKeys();
		
		if(generatedKeys.next()){
			id = generatedKeys.getLong(1);
		}else{
			logger.debug("Adress does exist in DB try to querry its ID: ["+address+"]");
			PreparedStatement getAddrId = connection.getPreparedStatement(GET_PUBKEY_ID);
			getAddrId.setString(1, address);
			getAddrId.executeQuery();
			ResultSet result = getAddrId.getResultSet();
			if(result.next()){
				id = result.getLong(1);
			}else{
				logger.fatal("Bad generatedKeySet from Address [" + address + "]");
				logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
			result.close();
			insertAddr.close();
		}
		
		generatedKeys.close();
		insertAddr.close();
		return id;
	}

}
