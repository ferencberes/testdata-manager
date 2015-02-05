package hu.sztaki.testdata_manager.database.connections;

import hu.sztaki.testdata_manager.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastAlsConnection extends DatabaseConnection {

	// TODO: this function is not used yet!!!
	// this map stores which variable is fixed in the sql queries
	private static final Map<String, Boolean> VARIABLES;
	static {
		VARIABLES = new HashMap<String, Boolean>();
		VARIABLES.put("input", true);
		VARIABLES.put("output", true);
		VARIABLES.put("solver", true);
		VARIABLES.put("numoftasks", true);
		VARIABLES.put("lambda", false);
		VARIABLES.put("feature_k", false);
		VARIABLES.put("iterations", false);
		VARIABLES.put("program", true);
		VARIABLES.put("mc_version", false);
		VARIABLES.put("time_taken", false);
	}

	public MulticastAlsConnection(DatabaseManager dm) {
		super(dm);
	}

	public String getFixedWhereClausePart(
			LinkedList<String> fixedVariableNames,
			LinkedList<String> fixedVariableValues) {
		String out = "";
		String fixedVar;
		int numberOfFixedVariables = fixedVariableNames.size();
		try {
			for (int i = 0; i < numberOfFixedVariables; i++) {
				fixedVar = fixedVariableNames.get(i);
				if (VARIABLES.get(fixedVar) == null) {
					throw new IllegalArgumentException(
							"There is no such variable as " + fixedVar);
				}
				out += fixedVar + "=";
				out += (VARIABLES.get(fixedVar) ? "'" : "");
				out += fixedVariableValues.get(i);
				out += (VARIABLES.get(fixedVar) ? "'" : "");
				out += " and ";
			}
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return out;
		}
	}

	public void createTable(String tableName) {
		String tableToInsert = "";
		if (tableName.equals("")) {
			tableToInsert = "MULTICAST_ALS_TEST_" + dm.queryDate();
			LOG.warn("Tablename was not given so a default daily table was created with name: '"
					+ tableToInsert + "'");
		} else {
			tableToInsert = tableName;
		}

		String queryCreate = "CREATE TABLE IF NOT EXISTS "
				+ tableToInsert
				+ "(ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, START_TIME DATE NOT NULL, INPUT VARCHAR(200) NOT NULL, OUTPUT VARCHAR(200), SOLVER VARCHAR(10), NUMOFTASKS INTEGER, LAMBDA DOUBLE, FEATURE_K INTEGER NOT NULL, ITERATIONS INTEGER, PROGRAM VARCHAR(50) NOT NULL, MC_VERSION INTEGER,TIME_TAKEN INTEGER, CONSTRAINT "
				+ tableToInsert + "_PK PRIMARY KEY( ID ))";
		createTable(tableToInsert, queryCreate, "Multicast Als");
	}

	public void insertData(String tableName) {
		String insertQuery = "insert into "
				+ tableName
				+ "(START_TIME,INPUT,OUTPUT,SOLVER,NUMOFTASKS,LAMBDA,FEATURE_K,ITERATIONS,PROGRAM,MC_VERSION,TIME_TAKEN)"
				+ " values (str_to_date(?,'%Y-%m-%d %T'),?,?,?,?,?,?,?,?,?,?)";
		insertData(tableName, insertQuery, "#Parameters of the als job:", 11);
	}

	@Override
	public void parseLines(String[] parameters, PreparedStatement insertData) {
		try {
			String[] inputTokens = parameters[1].split("/");

			insertData.setString(1, parameters[0]);
			insertData.setString(2, inputTokens[inputTokens.length - 1]);// input
			insertData.setString(3, parameters[2]);// output
			insertData.setInt(5, Integer.parseInt(parameters[3]));// #subtasks
			insertData.setString(4, parameters[7]);// solver
			insertData.setDouble(6, Double.parseDouble(parameters[5]));// lambda
			insertData.setInt(7, Integer.parseInt(parameters[4]));// k
																	// feature
			insertData.setInt(8, Integer.parseInt(parameters[6]));// #iteration
			insertData.setString(9, parameters[8]);// program
			insertData.setInt(10, Integer.parseInt(parameters[9]));// mc_version
			if (!parameters[10].equals("-")) {// time taken
				insertData.setInt(11, Integer.parseInt(parameters[10]));
			} else {
				insertData.setNull(11, java.sql.Types.NULL);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void getMulticastAlsRuntimeData(String tableName,
			LinkedList<String> inputs, LinkedList<String> mc_versions,
			String solver, String k, String lambda,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs, LinkedList<LinkedList<Double>> times,
			LinkedList<String> labels) throws RuntimeException {

		if (dm.existsTable(tableName)) {
			LOG.info("Extracting average runtime data from database STARTED");
			String whereFromClause = " from " + tableName + " where  solver='"
					+ solver + "'" + " and feature_k=" + k + " and lambda="
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
			String mc_version = "";
			try {
				for (int index = 0; index < numOfTasks.size(); index++) {
					timesQuery = "select iterations,avg(time_taken) ";
					programName = programs.get(index + 1);
					numTaskCount = numOfTasks.get(index).toString();
					inputName = inputs.get(index);
					mc_version = mc_versions.get(index);
					timesQuery = timesQuery + whereFromClause
							+ " and program='" + programName + "' and"
							+ " numoftasks=" + numTaskCount + " and input='"
							+ inputName + "' and mc_version=" + mc_version
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

	public void getMulticastAlsDeviationMultipleInput(String tableName,
			LinkedList<String> inputs, LinkedList<String> mc_versions,
			String solver, String k, String lambda,
			LinkedList<String> iterations, LinkedList<Integer> numOfTasks,
			LinkedList<String> programs,
			LinkedList<LinkedList<Double>> deviations) throws RuntimeException {

		if (dm.existsTable(tableName)) {
			LOG.info("Extracting standard deviations data from database STARTED");
			String whereFromClause = " from " + tableName + " where  solver='"
					+ solver + "'" + " and feature_k=" + k + " and lambda="
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
			String mc_version = "";
			try {
				for (int index = 0; index < numOfTasks.size(); index++) {
					timesQuery = "select iterations,stddev(time_taken) ";
					programName = programs.get(index + 1);
					numTaskCount = numOfTasks.get(index).toString();
					inputName = inputs.get(index);
					mc_version = mc_versions.get(index);
					timesQuery = timesQuery + whereFromClause
							+ " and program='" + programName + "' and"
							+ " numoftasks=" + numTaskCount + " and input='"
							+ inputName + "' and mc_version=" + mc_version
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
