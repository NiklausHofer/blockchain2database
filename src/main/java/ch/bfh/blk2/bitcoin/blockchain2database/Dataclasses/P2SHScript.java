package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;
import ch.bfh.blk2.bitcoin.util.Utility;

public class P2SHScript implements OutputScript {

	private static final Logger logger = LogManager.getLogger("OutputScript");
	
	private Script script;
	
	private long txId;
	private int txIndex,
	  scriptSize;
	
	private static final String INSERT_P2SHSCRIPT=
			"INSERT INTO out_script_p2sh (tx_id,tx_index,script_size,script_hash) VALUES(?,?,?,?)";

	public P2SHScript(Script script,int scriptSize,long txId,int txIndex) {
		if (!script.isPayToScriptHash())
			throw new IllegalArgumentException("Script must be of type P2SH");

		this.script = script;
		this.scriptSize = scriptSize;
		this.txId = txId;
		this.txIndex = txIndex;
	}

	
	@Override
	public OutputType getType() {
		return OutputType.P2SH;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {

		try{
		String scriptHash = script.getToAddress(Utility.PARAMS, false).toString();
		
		PreparedStatement insertScript = connection.getPreparedStatement(INSERT_P2SHSCRIPT);
		insertScript.setLong(1, txId);
		insertScript.setInt(2, txIndex);
		insertScript.setInt(3, scriptSize);
		insertScript.setString(4, scriptHash);
		
		insertScript.executeUpdate();
		}
		catch(SQLException e){
			logger.fatal("Failed to write P2SH script");
			logger.fatal("in output [tx_id: "+txId+", tx_index:"+txIndex+"]");
			logger.fatal(e);
			connection.commit();
			connection.closeConnection();
			System.exit(1);
		}
		
	}

}
