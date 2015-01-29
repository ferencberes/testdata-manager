package hu.sztaki.testdata_manager.runner;

import hu.sztaki.testdata_manager.dbmanager.MulticastAlsConnection2;

import java.util.LinkedList;

//TODO: there should be default path settings and optional configured path settings
//e.g: mix up DB_CONFIG_DIR and LOGPATH handling...

//TODO: exception handling if chart and config directories does not exists!

public class TestRunner {

	protected static String DB_CONFIG_DIR;
	protected static String CHART_SAMPLE_PATH;
	protected static String CHART_TARGET_PATH;
	protected static LinkedList<String> programs = new LinkedList<String>();
	protected static LinkedList<String> mc_versions = new LinkedList<String>();
	protected static LinkedList<String> inputs = new LinkedList<String>();
	protected static LinkedList<String> labels = new LinkedList<String>();
	protected static LinkedList<LinkedList<Double>> times = new LinkedList<LinkedList<Double>>();
	protected static LinkedList<LinkedList<Double>> deviations = new LinkedList<LinkedList<Double>>();
	
	public static void main(String[] args) {
		DB_CONFIG_DIR = args[0] + "/config";
		CHART_TARGET_PATH = args[0] + "/charts";
		CHART_SAMPLE_PATH = args[0] + "/resource/chart_sample";

		if (args.length == 2) {
			// list tables
			if (args[1].equals("list")) {
				//TODO: DatabaseConnection should be called but it is abstract!!!
				System.out.println(new MulticastAlsConnection2().queryTables());
			} else {
				System.out.println("There is no such command");
			}
		} else if (args.length == 3) {
			// drop table
			if (args[1].equals("drop")) {
				//TODO: DatabaseConnection should be called but it is abstract!!!
				new MulticastAlsConnection2().dropTable(args[2]);
			} else {
				System.out.println("There is no such command");
			}
		} else {
			if(args[2].equals("multicast_als")) {
				MulticastAlsRunner.runMulticastAlsTest(args);
			}
		}

	}
}