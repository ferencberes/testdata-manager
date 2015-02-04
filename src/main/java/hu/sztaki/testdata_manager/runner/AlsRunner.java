package hu.sztaki.testdata_manager.runner;

import hu.sztaki.testdata_manager.chartapi.ChartApiManager;
import hu.sztaki.testdata_manager.database.DatabaseManager;
import hu.sztaki.testdata_manager.database.connections.AlsConnection;

import java.util.LinkedList;

public class AlsRunner extends TestRunner {

	//TODO: enable logging!!!
	
	public static void run(String[] args, DatabaseManager dm) {

		ChartApiManager.loadChartParameters(CHART_SAMPLE_PATH,
				CHART_TARGET_PATH);

		AlsConnection als_conn = new AlsConnection(dm);
		ChartApiManager cam = new ChartApiManager();

		// create table
		if (args[1].equals("create")) {
			if (args[3].equals("-")) {
				 als_conn.createTable("");
			} else {
				 als_conn.createTable(args[3]);
			}
			// insert table
		} else if (args[1].equals("insert")) {
			 als_conn.insertData(args[3]);

		} else if (args[1].equals("chart")) {
			if (args.length == 10) {
				String chartName = (args[3].matches(".*.html") ? args[3]
						: args[3] + ".html");
				String TableName = args[4];
				String lambda = args[6];
				String k = args[7];

				LinkedList<Integer> numTasks = new LinkedList<Integer>();
				LinkedList<String> iterations = new LinkedList<>();
				String[] iters = args[8].split(":");

				String qInput = args[5];
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
					times.add(new LinkedList<Double>());
					deviations.add(new LinkedList<Double>());
				}
				 als_conn.getAlsRuntimeData(TableName, inputs, qInput,
						k, lambda, iterations, numTasks, programs, times,
						labels);
				 als_conn.getAlsDeviationMultipleInput(TableName,
						inputs, qInput, k, lambda, iterations, numTasks,
						programs, deviations);

				if (dm.existsTable(TableName)) {
					cam.generateCharts(chartName, labels, times, deviations);
				}
			} else {
				System.out.println("There are missing parameters!");
				System.out
						.println("chart chartname tablename qinput lambda k_feature program1:numtask1:input1|program2:numtask2:input2|...");
			}

		} else {
			System.out.println("There is no such command");
		}
	}
}