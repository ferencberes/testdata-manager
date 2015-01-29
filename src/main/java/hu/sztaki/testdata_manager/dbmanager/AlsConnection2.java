package hu.sztaki.testdata_manager.dbmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AlsConnection2 extends DatabaseConnection {

	public AlsConnection2(DbManager dm) {
		super(dm);
	}

	public void createTable(String tableName) {
		// NOTE: if tableName is not given then a daily table is created!
		String tableToInsert = (tableName.equals("") ? "ALS_TEST_"
				+ dm.queryDate() : tableName);
		// TODO: check whether table exist if yes, then stdout some msg

		String queryCreate = "CREATE TABLE IF NOT EXISTS "
				+ tableToInsert
				+ "(ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, START_TIME DATE NOT NULL, INPUT VARCHAR(200) NOT NULL, OUTPUT VARCHAR(200), Q_INPUT VARCHAR(200), NUMOFTASKS INTEGER, LAMBDA DOUBLE, FEATURE_K INTEGER NOT NULL, ITERATIONS INTEGER, PROGRAM VARCHAR(50) NOT NULL, TIME_TAKEN INTEGER, CONSTRAINT "
				+ tableToInsert + "_PK PRIMARY KEY( ID ))";
		createTable(tableToInsert, queryCreate, "Als");
	}

	public void insertData(String tableName) {
		String insertQuery = "insert into "
				+ tableName
				+ "(START_TIME,INPUT,OUTPUT,Q_INPUT,NUMOFTASKS,LAMBDA,FEATURE_K,ITERATIONS,PROGRAM,TIME_TAKEN)"
				+ " values (str_to_date(?,'%Y-%m-%d %T'),?,?,?,?,?,?,?,?,?)";
		insertData(tableName, insertQuery, "#Parameters of the job:", 10);
	}

	@Override
	public void parseLines(String[] parameters, PreparedStatement insertData) {
		try {
			String[] inputTokens = parameters[1].split("/");
			String[] qTokens = parameters[2].split("/");

			insertData.setString(1, parameters[0]);// start_time
			insertData.setString(2, inputTokens[inputTokens.length - 1]);// input
			insertData.setString(3, parameters[3]);// output
			insertData.setString(4, qTokens[qTokens.length - 1]);// q
																	// input
			insertData.setInt(5, Integer.parseInt(parameters[4]));// #subtasks
			insertData.setDouble(6, Double.parseDouble(parameters[6]));// lambda
			insertData.setInt(7, Integer.parseInt(parameters[5]));// k
																	// feature
			insertData.setInt(8, Integer.parseInt(parameters[7]));// #iteration
			insertData.setString(9, parameters[8]);// program
			if (!parameters[9].equals("-")) {// time taken
				insertData.setInt(10, Integer.parseInt(parameters[9]));
			} else {
				insertData.setNull(10, java.sql.Types.NULL);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void getAlsRuntimeData(String tableName, LinkedList<String> inputs,
			String qInput, String k, String lambda,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs, LinkedList<LinkedList<Double>> times,
			LinkedList<String> labels) throws RuntimeException {

		if (dm.existsTable(tableName)) {

			String whereFromClause = " from " + tableName + " where  q_input='"
					+ qInput + "'" + " and feature_k=" + k + " and lambda="
					+ lambda + " and (";

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

					st = dm.getCON().createStatement();
					rs = st.executeQuery(timesQuery);
					// int numOfIter = 0;
					while (rs.next()) {
						// numOfIter++;
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

	public void getAlsDeviationMultipleInput(String tableName,
			LinkedList<String> inputs, String qInput, String k, String lambda,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs,
			LinkedList<LinkedList<Double>> deviations) throws RuntimeException {

		if (dm.existsTable(tableName)) {

			String whereFromClause = " from " + tableName + " where  q_input='"
					+ qInput + "'" + " and feature_k=" + k + " and lambda="
					+ lambda + " and (";

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

					st = dm.getCON().createStatement();
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
