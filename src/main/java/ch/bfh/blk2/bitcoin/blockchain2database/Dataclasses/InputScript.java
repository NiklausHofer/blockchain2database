package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Use this to build classes that hold and represent different type of Input scripts.
 * 
 * @author niklaus
 */
public interface InputScript {
		
	/**
	 * Retrieve information on what type of Input Script the object represents
	 * 
	 * @return The type of Input Script represented by the object
	 */
	public ScriptType getType();
	
	/**
	 * Triggers writing of the Input script into the corresponding table in the database
	 * 
	 * @param connection Connection to the database
	 */
	public void writeInput(DatabaseConnection connection);
	
}
