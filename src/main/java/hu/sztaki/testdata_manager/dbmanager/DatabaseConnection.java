package hu.sztaki.testdata_manager.dbmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseConnection {
	
	protected DbManager dm;
	
	public DatabaseConnection(DbManager dbManager) {
		dm=dbManager;
	}

	public void createTable(String tableToInsert, String queryCreate, String message) {
		Statement createTable = null;
		ResultSet rs = null;

		try {
			createTable = dm.getCON().createStatement();
			queryCreate = "CREATE TABLE IF NOT EXISTS "
					+ tableToInsert
					+ "(ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, START_TIME DATE NOT NULL, INPUT VARCHAR(200) NOT NULL, OUTPUT VARCHAR(200), SOLVER VARCHAR(10), NUMOFTASKS INTEGER, LAMBDA DOUBLE, FEATURE_K INTEGER NOT NULL, ITERATIONS INTEGER, PROGRAM VARCHAR(50) NOT NULL, MC_VERSION INTEGER,TIME_TAKEN INTEGER, CONSTRAINT "
					+ tableToInsert + "_PK PRIMARY KEY( ID ))";
			// System.out.println(queryCreate);
			createTable.executeUpdate(queryCreate);
			System.out.println(message+" table is created.");

		} catch (SQLException sex) {
			sex.printStackTrace();
		} finally {
			try {
				if (createTable != null) {
					createTable.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sex2) {
				sex2.printStackTrace();
			}
		}
	}
	
	public void insertData(String tableName, String insertQuery, String logHeader, int numOfParams) {
		BufferedReader br = null;
		ResultSet rs = null;
		PreparedStatement insertData = null;

		String line = "";
		String[] actualLog;
		String[] parameters = new String[numOfParams];

		try {
			File[] logs = dm.getLOGDIR().listFiles();

			// NOTE: suppose that in logDir there are only new logs
			for (File i : logs) {
				System.out.println(i.getCanonicalPath());				
				br = new BufferedReader(new FileReader(i));
				insertData = dm.getCON().prepareStatement(insertQuery);
				while ((line = br.readLine()) != null) {
					
					if (line.matches(logHeader)) {
						actualLog = new String[numOfParams];
						
						for (int j = 0; j < numOfParams; j++) {
							line = br.readLine();
							actualLog[j] = line;
							if (j == numOfParams - 1
									&& !line.matches("Time\\staken.*")) {
								parameters[j] = "-";
							} else {
								parameters[j] = line.split(": ")[1];
							}
						}

						parseLines(parameters, insertData);
						insertData.execute();
						System.out
						.println("testdata was loaded into the database.");
						
					}
				}
			}
		} catch (SQLException sex) {
			sex.printStackTrace();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (insertData != null) {
					insertData.close();
				}
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
			} catch (SQLException sex2) {
				sex2.printStackTrace();
			}
		}
	}
	
	public abstract void parseLines(String[] parameters, PreparedStatement insertData);

}

