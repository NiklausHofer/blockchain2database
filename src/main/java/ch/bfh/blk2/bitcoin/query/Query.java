package ch.bfh.blk2.bitcoin.query;

import ch.bfh.blk2.bitcoin.blockchain2database.DatabaseConnection;

public interface Query<R>{

	public void exequte(DatabaseConnection connection);
	
	public R getResult();
	
	public String resultToString();
	
}
