package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.script.Script;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public class P2SHOtherInputScript implements InputScript{

	private static final Logger logger = LogManager.getLogger("P2SHOtherInputScript");
	
	private final static String INSERT_P2SH_OTHER_SCRIPT = 
			"INSERT INTO unlock_script_p2sh_other"
			+ " (tx_id,tx_index,script_size,script_id,reedem_script_id,reedem_script_size)"
			+ " VALUES (?,?,?,?,?,?)";
	
	private int txIndex,scriptSize;
	private long txId;
	private Script script;
	
	@Override
	public ScriptType getType() {
		return ScriptType.IN_P2SH_OTHER;
	}

	@Override
	public void writeInput(DatabaseConnection connection) {

		byte[] reedemScript = getReedemScript();
		
		//TODO check if this works
		// remove redeem script
		Script.removeAllInstancesOf(script.getProgram(),reedemScript);
		
		ScriptManager sc = new ScriptManager();
		long scriptId = sc.writeScript(connection,script);
		long reedemId = sc.writeScript(connection,new Script(reedemScript));
		
		try{
			
			PreparedStatement insertStatement = connection.getPreparedStatement(INSERT_P2SH_OTHER_SCRIPT);
			insertStatement.setLong(1, txId);
			insertStatement.setInt(2, txIndex);
			insertStatement.setInt(3, scriptSize - reedemScript.length);
			insertStatement.setLong(4,scriptId);
			insertStatement.setLong(5,reedemId);
			insertStatement.setInt(6, reedemScript.length);
			
			insertStatement.executeLargeUpdate();
			insertStatement.close();
			
			}catch(SQLException e){
				logger.fatal("faild to insert Input script of type Coinbase",e);
				connection.commit();
				connection.closeConnection();
				System.exit(1);
			}
		
	}
	
	
	public byte[] getReedemScript(){
		return script.getChunks().get(script.getChunks().size()-1).data;
	}
}
