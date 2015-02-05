package hu.sztaki.testdata_manager.runner;

import hu.sztaki.testdata_manager.database.DatabaseManager;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: there should be default path settings and optional configured path settings
//e.g: mix up DB_CONFIG_DIR and LOGPATH handling...

//TODO: exception handling if chart and config directories does not exists!

public class TestRunner {
	
	protected static final Logger LOG = LoggerFactory
			.getLogger(TestRunner.class);

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
		// TODO: set these paths from config file! Then check whether they
		// exists...
		DB_CONFIG_DIR = args[0] + "/config";
		CHART_TARGET_PATH = args[0] + "/charts";
		CHART_SAMPLE_PATH = args[0] + "/src/main/resources/chart_sample";

		DatabaseManager dm = new DatabaseManager(DB_CONFIG_DIR);
		String command = args[1];

		if (!command.equals("list") && !command.equals("drop")
				&& !command.equals("create") && !command.equals("insert")
				&& !command.equals("chart")) {
			LOG.error("The " + command + " command is not implemented."
					+ printCommandOptions());
		}

		if (args.length == 2 && command.equals("list")) {
			// list tables
			System.out.println(dm.queryTables());
		} else if (args.length == 3 && command.equals("drop")) {
			// drop table
			dm.dropTable(args[2]);
		} else {
			if (args[2].equals("multicast_als")) {
				MulticastAlsRunner.run(args, dm);
			} else if (args[2].equals("als")) {
				AlsRunner.run(args, dm);
			} else if (args[2].equals("pagerank")) {
				PageRankRunner.run(args, dm);
			} else {
				LOG.error("The " + args[2] + " command is not implemented."
						+ printProgramOptions());
			}
		}
		dm.close();
	}

	public static String printCommandOptions() {
		return "\nChoose only from 'list','create','drop','chart' options";
	}

	public static String printProgramOptions() {
		return "\nChoose only from 'als','pagerank','multicast_als' options";
	}
}
