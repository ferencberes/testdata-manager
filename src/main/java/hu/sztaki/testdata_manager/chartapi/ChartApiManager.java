package hu.sztaki.testdata_manager.chartapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: chart of failed jobs

public class ChartApiManager {

	private static final Logger LOG = LoggerFactory
			.getLogger(ChartApiManager.class);

	private static String SAMPLE_PATH;
	private static String TARGET_PATH;
	private static int MAX_NUMBER_OF_PROGRAMS;

	private static final String[] colors = new String[] { "#0066FF", "#339933",
			"#FF6600", "#FF0000", "#FFFF00", "#800000" };

	public static void loadChartParameters(String samplePath, String targetPath) {
		SAMPLE_PATH = samplePath + "/";
		TARGET_PATH = targetPath + "/";
		MAX_NUMBER_OF_PROGRAMS = colors.length;
	}

	public void generateCharts(String fileName, LinkedList<String> labels,
			LinkedList<LinkedList<Double>> times,
			LinkedList<LinkedList<Double>> deviations) {

		BufferedReader br = null;
		PrintWriter pw = null;
		String line;
		String chartLine = "";
		String sampleFilePath = SAMPLE_PATH + "sampleWithDev.txt";

		int numOfPrograms = labels.size();
		int numOfIterations = times.get(0).size();

		try {
			LOG.info("Using '" + sampleFilePath
					+ "' as blank sample chart for chart generation.");
			br = new BufferedReader(new FileReader(sampleFilePath));
			pw = new PrintWriter(new FileWriter(
					new File(TARGET_PATH + fileName)));

			LOG.info("html chart generation STARTED");
			while ((line = br.readLine()) != null) {
				if (line.matches("#CHART1.*")) {
					chartLine = "[";
					for (int j = 0; j < numOfPrograms; j++) {
						if (j != numOfPrograms - 1) {
							chartLine = chartLine + "'" + labels.get(j) + "',";
						} else {
							chartLine = chartLine + "'" + labels.get(j) + "'";
						}
					}
					chartLine = chartLine + "],";
					pw.println(chartLine);
					chartLine = "";

					for (int i = 0; i < numOfIterations; i++) {
						chartLine = "[";
						for (int j = 0; j < numOfPrograms; j++) {
							if (j != numOfPrograms - 1) {
								if (times.get(j).size() > i) {
									chartLine = chartLine
											+ " "
											+ Double.toString(times.get(j).get(
													i)) + ",";
								} else {
									chartLine = chartLine + " ,";
								}
							} else {
								if (times.get(j).size() > i) {
									chartLine = chartLine
											+ " "
											+ Double.toString(times.get(j).get(
													i)) + "]";
								} else {
									chartLine = chartLine + " ]";
								}
							}
						}
						if (i != numOfIterations - 1) {
							chartLine = chartLine + ",";
						}
						pw.println(chartLine);
						chartLine = "";
					}
					LOG.info("SUCCESS: runtime table inserted into chart.");
				} else if (line.matches("#COLORS.*")) {
					pw.println(writeColors(MAX_NUMBER_OF_PROGRAMS));
					LOG.info("SUCCESS: color codes inserted into chart.");
				} else if (line.matches("#CHARTDEVS.*")) {

					for (int prog = 0; prog < numOfPrograms - 1; prog++) {
						// start deviation chart script
						pw.print(writeHeader(prog));

						chartLine = "['" + labels.get(0) + "'," + "'"
								+ labels.get(prog + 1) + "'],";

						pw.println(chartLine);
						chartLine = "";

						for (int i = 0; i < numOfIterations; i++) {
							chartLine = "["
									+ Double.toString(deviations.get(0).get(i))
									+ ","
									+ Double.toString(deviations.get(prog + 1)
											.get(i)) + "]";

							if (i != numOfIterations - 1) {
								chartLine = chartLine + ",";
							}
							pw.println(chartLine);
							chartLine = "";
						}
						// finish deviation chart script
						pw.print(writeOthers(labels.get(prog + 1), prog));
					}
					LOG.info("SUCCESS: standard deviation tables inserted into chart.");
				} else if (line.matches("#DIVS.*")) {
					// write divs to scripts
					pw.print(writeDivs(numOfPrograms - 1));
					LOG.info("SUCCESS: html tags inserted into chart.");
				} else {
					pw.println(line);
				}
			}
			LOG.info("html chart generation STOPPED");
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			pw.close();
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
			}
			LOG.info("Chart " + fileName + " was created.");
		}
	}

	public String writeDivs(int db) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div id=\"programs\">\n");
		sb.append("<div id=\"chart_runtime\" style=\"width: 700px; height: 400px; float:left;\"></div>\n");
		sb.append("</div>\n");

		for (int i = 0; i < db; i++) {
			sb.append("\n<div id=\"stdnew" + i + "\">\n");
			sb.append("<div id=\"chart_stdev"
					+ i
					+ "\" style=\"width: 700px; height: 400px; float:left;\"></div>\n");
			sb.append("</div>\n");
		}

		return sb.toString();
	}

	public String writeColors(int id) {

		String out = "colors: [";
		if (id > MAX_NUMBER_OF_PROGRAMS) {
			throw new IllegalArgumentException(
					"The number of programs cannot exceed "
							+ MAX_NUMBER_OF_PROGRAMS + "! Only "
							+ MAX_NUMBER_OF_PROGRAMS
							+ " colors are predefined.");
		} else {

			if (id == colors.length) {
				for (int i = 0; i < colors.length; i++) {
					out += "'" + colors[i] + "'";
					if (i != colors.length - 1) {
						out += ",";
					}
				}
			} else {
				out += "'" + colors[id] + "'";
			}
			out += "]";
			return out;
		}
	}

	public String writeHeader(int id) {
		String out = "\nvar stdev" + id
				+ " = google.visualization.arrayToDataTable([\n";
		return out;
	}

	public String writeOthers(String msg, int id) {
		StringBuilder sb = new StringBuilder();
		sb.append("]);\n");

		sb.append("\nvar options_stdev" + id + " = {\n");
		sb.append("title: 'The standard deviation of " + msg + "',\n");
		sb.append("hAxis: {title: 'Iterations'},\n");
		sb.append("vAxis: {title: 'Standard deviation (sec)'},\n");
		sb.append("legend: 'none',\n");
		sb.append(writeColors(id));
		sb.append("};\n");
		sb.append("\nvar chart_stdev"
				+ id
				+ " = new google.visualization.ScatterChart(document.getElementById('chart_stdev"
				+ id + "'));\n");
		sb.append("chartstdev" + id + ".draw(stdev" + id + ", options_stdev"
				+ id + ");\n");
		return sb.toString();
	}
}
