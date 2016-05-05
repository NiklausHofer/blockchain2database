package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class CoinbaseInputScript implements InputScript{
	
	private static final Logger logger = LogManager.getLogger("InputScriptCreator");
	
	private final static String INSERT_COINBASE_SCRIPT = 
			"INSERT INTO unlock_script_coinbase (tx_id,tx_index,script_size,information) VALUES (?,?,?,?)";
	
	private int txIndex,scriptSize;
	private long txId;
	private Script script;
	
	public CoinbaseInputScript(Script script,long txId,int txIndex,int scriptSize) {
		
		this.txId = txId;
		this.txIndex = txIndex;
		this.scriptSize = scriptSize;
	}

	@Override
	public ScriptType getType() {
		return ScriptType.IN_COINBASE;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {
		
		byte[] information=new byte[0];
		
		for( ScriptChunk sc : script.getChunks()){
			
			byte[] info1 = new byte[information.length+sc.data.length];
			System.arraycopy(information, 0, info1, 0,                  information.length);
			System.arraycopy(sc.data    , 0, info1, information.length, sc.data.length    );
			information = info1;
		}
		
		try{
		
		PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_COINBASE_SCRIPT);
		insertStatement.setLong(1, txId);
		insertStatement.setInt(2, txIndex);
		insertStatement.setInt(3, scriptSize);
		insertStatement.setBytes(4,information);
		
		}catch(SQLException e){
			logger.fatal("faild to insert Input script of type Coinbase",e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
	}

}
