package hu.sztaki.testdata_manager.runner;

//TODO: there should be default path settings and optional configured path settings
//e.g: mix up DB_CONFIG_DIR and LOGPATH handling...

//TODO: exception handling if chart and config directories does not exists!

import hu.sztaki.testdata_manager.chartapi.ChartApiManager;
import hu.sztaki.testdata_manager.dbmanager.AlsConnection;
import hu.sztaki.testdata_manager.dbmanager.DbManager;
import hu.sztaki.testdata_manager.dbmanager.MulticastAlsConnection;
import hu.sztaki.testdata_manager.dbmanager.PageRankConnection;

import java.util.LinkedList;

public class TestDataManagerRunner {

	private static String DB_CONFIG_DIR;
	private static String CHART_SAMPLE_PATH;
	private static String CHART_TARGET_PATH;

	public static void main(String[] args) {

		LinkedList<String> programs = new LinkedList<String>();
		LinkedList<String> mc_versions = new LinkedList<String>();
		LinkedList<String> inputs = new LinkedList<String>();
		LinkedList<String> labels = new LinkedList<String>();
		LinkedList<LinkedList<Double>> times = new LinkedList<LinkedList<Double>>();
		LinkedList<LinkedList<Double>> deviations = new LinkedList<LinkedList<Double>>();

		DB_CONFIG_DIR = args[0] + "/config";
		CHART_TARGET_PATH = args[0] + "/charts";
		CHART_SAMPLE_PATH = args[0] + "/resource/chart_sample";

		ChartApiManager.loadChartParameters(CHART_SAMPLE_PATH,
				CHART_TARGET_PATH);
		DbManager dbm = new DbManager(DB_CONFIG_DIR);
		ChartApiManager cam = new ChartApiManager();

		if (args.length == 2) {
			// list tables
			if (args[1].equals("list")) {
				System.out.println(dbm.queryTables());
			} else {
				System.out.println("There is no such command");
			}
		} else if (args.length == 3) {
			// drop table
			if (args[1].equals("drop")) {
				dbm.dropTable(args[2]);
			} else {
				System.out.println("There is no such command");
			}
		} else if (args.length == 4) {
			// create table
			if (args[1].equals("create") && args[2].equals("als")) {
				if (args[3].equals("-")) {
					AlsConnection.createAlsTable(dbm, "");
				} else {
					AlsConnection.createAlsTable(dbm, args[3]);
				}
				// insert into table
			} else if (args[1].equals("insert") && args[2].equals("als")) {
				AlsConnection.insertAlsData(dbm, args[3]);
				// create table
			} else if (args[1].equals("create") && args[2].equals("pagerank")) {
				if (args[3].equals("-")) {
					PageRankConnection.createPageRankTable(dbm, "");
				} else {
					PageRankConnection.createPageRankTable(dbm, args[3]);
				}
				// insert into table
			} else if (args[1].equals("insert") && args[2].equals("pagerank")) {
				PageRankConnection.insertPageRankData(dbm, args[3]);

				// create table
			} else if (args[1].equals("create")
					&& args[2].equals("multicast_als")) {
				if (args[3].equals("-")) {
					MulticastAlsConnection.createMulticastAlsTable(dbm, "");
				} else {
					MulticastAlsConnection
							.createMulticastAlsTable(dbm, args[3]);
				}
				// insert into table
			} else if (args[1].equals("insert")
					&& args[2].equals("multicast_als")) {
				MulticastAlsConnection.insertMulticastAlsData(dbm, args[3]);

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
			
			if (args[1].equals("als")) {
				String qInput = args[4];
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
					times.add(new LinkedList<Double>());
					deviations.add(new LinkedList<Double>());
				}
				AlsConnection.getAlsRuntimeData(dbm, TableName, inputs, qInput,
						k, lambda, iterations, numTasks, programs, times,
						labels);
				AlsConnection.getAlsDeviationMultipleInput(dbm, TableName,
						inputs, qInput, k, lambda, iterations, numTasks,
						programs, deviations);

				if (dbm.existsTable(TableName)) {
					cam.generateCharts(chartName, labels, times, deviations);
				}
			} else if(args[1].equals("multicast_als")) {
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
				MulticastAlsConnection.getMulticastAlsRuntimeData(dbm, TableName, inputs, mc_versions, solver,
						k, lambda, iterations, numTasks, programs, times,
						labels);
				MulticastAlsConnection.getMulticastAlsDeviationMultipleInput(dbm, TableName,
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
		} else if (args.length == 8) {

			if (args[1].equals("pagerank")) {
				String chartName = (args[2].matches(".*.html") ? args[2]
						: args[2] + ".html");
				String TableName = args[3];
				String dampening = args[4];
				String epsilon = args[5];

				LinkedList<Integer> numTasks = new LinkedList<Integer>();
				LinkedList<String> iterations = new LinkedList<>();
				String[] iters = args[6].split(":");
				for (int i = 0; i < iters.length; i++) {
					iterations.add(iters[i]);
				}

				programs.add("it");
				labels.add("it");
				times.add(new LinkedList<Double>());
				deviations.add(new LinkedList<Double>());

				String[] testData = args[7].split("\\|");
				for (String i : testData) {
					System.out.println(i);
					programs.add(i.split(":")[0]);
					labels.add(i.split(":")[0]);
					numTasks.add(Integer.parseInt(i.split(":")[1]));
					inputs.add(i.split(":")[2]);
					times.add(new LinkedList<Double>());
					deviations.add(new LinkedList<Double>());
				}

				PageRankConnection.getPageRankRuntimeData(dbm, TableName,
						inputs, dampening, epsilon, iterations, numTasks,
						programs, times, labels);
				PageRankConnection.getPageRankDeviationMultipleInput(dbm,
						TableName, inputs, dampening, epsilon, iterations,
						numTasks, programs, deviations);
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