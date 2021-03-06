package hu.sztaki.testdata_manager.runner;

import hu.sztaki.testdata_manager.chartapi.ChartApiManager;
import hu.sztaki.testdata_manager.database.DatabaseManager;
import hu.sztaki.testdata_manager.database.connections.DatabaseConnection;
import hu.sztaki.testdata_manager.database.connections.MulticastAlsConnection;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastAlsRunner extends TestRunner {

	private static final Logger LOG = LoggerFactory
			.getLogger(MulticastAlsRunner.class);

	public static void run(String[] args, DatabaseManager dm) {

		ChartApiManager.loadChartParameters(CHART_SAMPLE_PATH,
				CHART_TARGET_PATH);

		MulticastAlsConnection mc_als_conn = new MulticastAlsConnection(dm);
		ChartApiManager cam = new ChartApiManager();

		// create table
		if (args[1].equals("create")) {
			if (args[3].equals("-")) {
				mc_als_conn.createTable("");
			} else {
				mc_als_conn.createTable(args[3]);
			}
			// insert table
		} else if (args[1].equals("insert")) {
			mc_als_conn.insertData(args[3]);

		} else if (args[1].equals("chart")) {
			if (args.length == 10) {
				String chartName = (args[3].matches(".*.html") ? args[3]
						: args[3] + ".html");
				String TableName = args[4];
				String solver = args[5];
				String lambda = args[6];
				String k = args[7];

				LinkedList<Integer> numTasks = new LinkedList<Integer>();
				LinkedList<String> iterations = new LinkedList<>();
				String[] iters = args[8].split(":");

				for (int i = 0; i < iters.length; i++) {
					iterations.add(iters[i]);
				}

				programs.add("it");
				labels.add("it");
				times.add(new LinkedList<Double>());
				deviations.add(new LinkedList<Double>());

				String[] testData = args[9].split("\\|");
				for (String i : testData) {
					// System.out.println(i);
					programs.add(i.split(":")[0]);
					labels.add(i.split(":")[0]);
					numTasks.add(Integer.parseInt(i.split(":")[1]));
					inputs.add(i.split(":")[2]);
					mc_versions.add(i.split(":")[3]);
					times.add(new LinkedList<Double>());
					deviations.add(new LinkedList<Double>());
				}
				mc_als_conn.getMulticastAlsRuntimeData(TableName, inputs,
						mc_versions, solver, k, lambda, iterations, numTasks,
						programs, times, labels);
				mc_als_conn.getMulticastAlsDeviationMultipleInput(TableName,
						inputs, mc_versions, solver, k, lambda, iterations,
						numTasks, programs, deviations);

				if (dm.existsTable(TableName)) {
					cam.generateCharts(chartName, labels, times, deviations);
				}
			} else {
				LOG.error("There are missing parameters!" + getArgumentFormat());
			}
		}
	}

	private static String getArgumentFormat() {
		return "\nUsage: chart chartname tablename solver lambda k_feature iteration1:iteration2:...:last_iteration program1:numtask1:input1:multicast_version1|program2:numtask2:input2:multicast_version2|...";
	}
}