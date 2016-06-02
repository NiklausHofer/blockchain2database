package ch.bfh.blk2.bitcoin.blockchain2database.Dataclasses;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

/**
 * Represents invalid Output scripts. These are scripts that cannot be parsed at all.
 * So since there is no parsable data, there's nothing to be written to the database either.
 * Thus, writeOutputScript is just a dummy to comply with the interface.
 * 
 * @author niklaus
 *
 */
public class InvalidScript implements OutputScript {

	/**
	 * Invalid script. Don't need a script for that ;)
	 */
	public InvalidScript() {
	}

	@Override
	public ScriptType getType() {
		return ScriptType.OUT_INVALID;
	}

	@Override
	public void writeOutputScript(DatabaseConnection connection) {
		return;
	}

}
