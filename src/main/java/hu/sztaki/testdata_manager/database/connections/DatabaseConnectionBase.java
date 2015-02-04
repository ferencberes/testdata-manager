package hu.sztaki.testdata_manager.database.connections;

public interface DatabaseConnectionBase {
	
	public void createTable(String tableToInsert, String queryCreate, String message);
	
	public void insertData(String tableName, String insertQuery, String logHeader, int numOfParams);
}
