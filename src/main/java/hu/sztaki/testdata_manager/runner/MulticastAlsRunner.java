package hu.sztaki.testdata_manager.runner;

import hu.sztaki.testdata_manager.chartapi.ChartApiManager;
import hu.sztaki.testdata_manager.dbmanager.DbManager;
import hu.sztaki.testdata_manager.dbmanager.MulticastAlsConnection2;
import java.util.LinkedList;

public class MulticastAlsRunner extends TestRunner{

	public static void runMulticastAlsTest(String[] args) {

		ChartApiManager.loadChartParameters(CHART_SAMPLE_PATH,
				CHART_TARGET_PATH);
		DbManager.loadDBParameters(DB_CONFIG_DIR);
		MulticastAlsConnection2 dbm = new MulticastAlsConnection2();
		ChartApiManager cam = new ChartApiManager();

		if (args.length == 4) {
			// create table
			if (args[1].equals("create")) {
				if (args[3].equals("-")) {
					dbm.createMulticastAlsTable("");
				} else {
					dbm.createMulticastAlsTable(args[3]);
				}
			// insert table
			} else if (args[1].equals("insert")) {
				dbm.insertData(args[3]);

			} else {
				System.out.println("There is no such command");
			}
		} else if (args.length == 9) {
			String chartName = (args[2].matches(".*.html") ? args[2]
					: args[2] + ".html");
			String TableName = args[3];
			String lambda = args[5];
			String k = args[6];

			LinkedList<Integer> numTasks = new LinkedList<Integer>();
			LinkedList<String> iterations = new LinkedList<>();
			String[] iters = args[7].split(":");
			
			if(args[1].equals("multicast_als")) {
				String solver = args[4];
				for (int i = 0; i < iters.length; i++) {
					iterations.add(iters[i]);
				}

				programs.add("it");
				labels.add("it");
				times.add(new LinkedList<Double>());
				deviations.add(new LinkedList<Double>());

				String[] testData = args[8].split("\\|");
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
				dbm.getMulticastAlsRuntimeData(TableName, inputs, mc_versions, solver,
						k, lambda, iterations, numTasks, programs, times,
						labels);
				dbm.getMulticastAlsDeviationMultipleInput(TableName,
						inputs, mc_versions, solver, k, lambda, iterations, numTasks,
						programs, deviations);

				if (dbm.existsTable(TableName)) {
					cam.generateCharts(chartName, labels, times, deviations);
				}
			} else {
				System.out.println("There are missing parameters!");
				System.out
						.println("chart chartname tablename qinput lambda k_feature program1:numtask1:input1|program2:numtask2:input2|...");
			}

		} else {
			System.out.println("Only als and pagerank options are available");
		}
	}
}