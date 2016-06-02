package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Used to build representations of Output Scripts of different types. You probably want an implementation
 * for at least all the standard types.
 * 
 * @author niklaus
 */
public interface OutputScript {

	/**
	 * Lets you know of which type the output script actually is
	 * 
	 * @return The type of which this output script is
	 */
	public ScriptType getType();

	/**
	 * Writes the output script into the database. The kind of data that is written and the table it is
	 * written to will probably depend on the specific type of output script
	 * 
	 * @param connection Provides connectivity to the database
	 */
	public void writeOutputScript(DatabaseConnection connection);
}
