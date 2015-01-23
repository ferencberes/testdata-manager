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
import java.util.LinkedList;

public class PageRankConnection {

	public static void createPageRankTable(DbManager dbm, String tableName) {
		Statement createTable = null;
		ResultSet rs = null;
		String queryCreate = "";

		// NOTE: if tbaleName is not given then a daily table is created!
		String tableToInsert = (tableName.equals("") ? "PAGERANK_TEST_"
				+ dbm.queryDate() : tableName);
		// TODO: check whether table exist if yes, then stdout some msg

		try {
			createTable = dbm.getCON().createStatement();
			queryCreate = "CREATE TABLE IF NOT EXISTS "
					+ tableToInsert
					+ "(ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, START_TIME DATE NOT NULL, INPUT VARCHAR(200) NOT NULL, OUTPUT VARCHAR(200), NUMOFTASKS INTEGER, NUMOFSUPERNODES INTEGER, DAMPENING DOUBLE, EPSILON DOUBLE, ITERATIONS INTEGER, PROGRAM VARCHAR(50) NOT NULL, TIME_TAKEN INTEGER, CONSTRAINT "
					+ tableToInsert + "_PK PRIMARY KEY( ID ))";
			// System.out.println(queryCreate);
			createTable.executeUpdate(queryCreate);
			System.out.println("PageRank Table is created.");

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

	public static void insertPageRankData(DbManager dbm, String tableName) {
		BufferedReader br = null;
		ResultSet rs = null;
		PreparedStatement insertData = null;

		String insertTest = "insert into "
				+ tableName
				+ "(START_TIME,INPUT,OUTPUT,NUMOFTASKS, NUMOFSUPERNODES,DAMPENING,EPSILON,ITERATIONS,PROGRAM,TIME_TAKEN)"
				+ " values (str_to_date(?,'%Y-%m-%d %T'),?,?,?,?,?,?,?,?,?)";

		int numOfParams = 10;

		String line = "";
		String[] actualLog;
		String[] parameters = new String[numOfParams];

		try {
			File[] logs = dbm.getLOGDIR().listFiles();

			// NOTE: suppose that in logDir there are only new logs
			for (File i : logs) {
				System.out.println(i.getCanonicalPath());
				br = new BufferedReader(new FileReader(i));
				insertData = dbm.getCON().prepareStatement(insertTest);
				while ((line = br.readLine()) != null) {
					if (line.matches("#Parameters of the pagerank job:")) {
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

						String[] inputTokens = parameters[1].split("/");

						insertData.setString(1, parameters[0]);// start_time
						insertData.setString(2,
								inputTokens[inputTokens.length - 1]);// input
						insertData.setString(3, parameters[2]);// output
						insertData.setInt(4, Integer.parseInt(parameters[3]));// #subtasks
						insertData.setInt(5, Integer.parseInt(parameters[4]));// #numberOfSuperNodes
						insertData.setDouble(6,
								Double.parseDouble(parameters[5]));// dampening
						insertData.setDouble(7,
								Double.parseDouble(parameters[6]));// epsilon
						insertData.setInt(8, Integer.parseInt(parameters[7]));// #iteration
						insertData.setString(9, parameters[8]);// program
						if (!parameters[9].equals("-")) {// time taken
							insertData.setInt(10,
									Integer.parseInt(parameters[9]));
						} else {
							insertData.setNull(10, java.sql.Types.NULL);
						}

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

	public static void getPageRankRuntimeData(DbManager dbm, String tableName,
			LinkedList<String> inputs, String dampening, String epsilon,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs, LinkedList<LinkedList<Double>> times,
			LinkedList<String> labels) throws RuntimeException {

		// TODO set numberofsupernodes!!!

		if (dbm.existsTable(tableName)) {

			String whereFromClause = " from " + tableName + " where dampening="
					+ dampening + " and epsilon=" + epsilon + " and (";

			for (int s = 0; s < iterations.size(); s++) {
				whereFromClause = whereFromClause + " iterations="
						+ iterations.get(s)
						+ (s == iterations.size() - 1 ? ")" : " or");
				times.get(0).add(Double.parseDouble(iterations.get(s)));
			}

			String timesQuery = "";
			ResultSet rs = null;
			Statement st = null;
			String programName = "";
			String numTaskCount = "";
			String inputName = "";
			try {
				for (int index = 0; index < numOfTasks.size(); index++) {
					timesQuery = "select iterations,avg(time_taken) ";
					programName = programs.get(index + 1);
					numTaskCount = numOfTasks.get(index).toString();
					inputName = inputs.get(index);
					timesQuery = timesQuery + whereFromClause
							+ " and program='" + programName + "' and"
							+ " numoftasks=" + numTaskCount + " and input='"
							+ inputName + "'"
							+ " group by iterations order by iterations asc";
					System.out.println(timesQuery + "\n");

					st = dbm.getCON().createStatement();
					rs = st.executeQuery(timesQuery);
					while (rs.next()) {
						labels.set(index + 1, programName + ": " + numTaskCount
								+ " subTasks, " + inputName);
						times.get(index + 1).add(rs.getDouble(2) / 1000 / 60);
					}
				}
			} catch (SQLException sex) {
				sex.printStackTrace();
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException sex2) {
					sex2.printStackTrace();
				}
			}
		} else {
			System.out.println("The table does not exists!");
		}
	}

	public static void getPageRankDeviationMultipleInput(DbManager dbm,
			String tableName, LinkedList<String> inputs, String dampening,
			String epsilon, LinkedList<String> iterations,
			LinkedList<Integer> numOfTasks, LinkedList<String> programs,
			LinkedList<LinkedList<Double>> deviations) throws RuntimeException {

		// TODO set numberofsupernodes!!!

		if (dbm.existsTable(tableName)) {

			String whereFromClause = " from " + tableName + " where dampening="
					+ dampening + " and epsilon=" + epsilon + " and (";

			for (int s = 0; s < iterations.size(); s++) {
				whereFromClause = whereFromClause + " iterations="
						+ iterations.get(s)
						+ (s == iterations.size() - 1 ? ")" : " or");
				deviations.get(0).add(Double.parseDouble(iterations.get(s)));
			}

			String timesQuery = "";
			ResultSet rs = null;
			Statement st = null;
			String programName = "";
			String numTaskCount = "";
			String inputName = "";
			try {
				for (int index = 0; index < numOfTasks.size(); index++) {
					timesQuery = "select iterations,stddev(time_taken) ";
					programName = programs.get(index + 1);
					numTaskCount = numOfTasks.get(index).toString();
					inputName = inputs.get(index);
					timesQuery = timesQuery + whereFromClause
							+ " and program='" + programName + "' and"
							+ " numoftasks=" + numTaskCount + " and input='"
							+ inputName + "'"
							+ " group by iterations order by iterations asc";
					System.out.println(timesQuery + "\n");

					st = dbm.getCON().createStatement();
					rs = st.executeQuery(timesQuery);
					while (rs.next()) {
						deviations.get(index + 1).add(rs.getDouble(2) / 1000);
					}
				}
			} catch (SQLException sex) {
				sex.printStackTrace();
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException sex2) {
					sex2.printStackTrace();
				}
			}
		} else {
			System.out.println("The table does not exists!");
		}
	}
}

