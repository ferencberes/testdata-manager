package hu.sztaki.testdata_manager.database.connections;

import hu.sztaki.testdata_manager.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class AlsConnection extends DatabaseConnection {

	public AlsConnection(DatabaseManager dm) {
		super(dm);
	}

	public void createTable(String tableName) {
		String tableToInsert = "";
		if (tableName.equals("")) {
			tableToInsert = "ALS_TEST_" + dm.queryDate();
			LOG.warn("Tablename was not given so a default daily table was created with name: '"
					+ tableToInsert + "'");
		} else {
			tableToInsert = tableName;
		}

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
			LOG.info("Extracting average runtime data from database STARTED");
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
					// System.out.println(timesQuery + "\n");
					LOG.info("SUCCESS: sql query for average runtimes was generated program "+Integer.toString(index+1)+".");
					st = dm.getCON().createStatement();
					rs = st.executeQuery(timesQuery);
					while (rs.next()) {
						labels.set(index + 1, programName + ": " + numTaskCount
								+ " subTasks, " + inputName);
						times.get(index + 1).add(rs.getDouble(2) / 1000 / 60);
					}
					LOG.info("SUCCESS: sql query for average runtimes was executed program "+Integer.toString(index+1)+".");
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
			LOG.info("Extracting average runtime data from database FINISHED");
		} else {
			LOG.error("The given table '" + tableName
					+ "' does not exists! So the query could not be executed.");
		}
	}

	public void getAlsDeviationMultipleInput(String tableName,
			LinkedList<String> inputs, String qInput, String k, String lambda,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs,
			LinkedList<LinkedList<Double>> deviations) throws RuntimeException {

		if (dm.existsTable(tableName)) {
			LOG.info("Extracting standard deviations data from database STARTED");
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
					// System.out.println(timesQuery + "\n");
					LOG.info("SUCCESS: sql query for standard deviations was generated program "+Integer.toString(index+1)+".");
					st = dm.getCON().createStatement();
					rs = st.executeQuery(timesQuery);
					while (rs.next()) {
						deviations.get(index + 1).add(rs.getDouble(2) / 1000);

					}
					LOG.info("SUCCESS: sql query for standard deviations was executed program "+Integer.toString(index+1)+".");
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
			LOG.info("Extracting standard deviations data from database FINISHED");
		} else {
			LOG.error("The given table '" + tableName
					+ "' does not exists! So the query could not be executed.");
		}
	}

}
