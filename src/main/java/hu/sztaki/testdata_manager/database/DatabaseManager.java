package hu.sztaki.testdata_manager.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

	protected static final Logger LOG = LoggerFactory
			.getLogger(DatabaseManager.class);

	// TODO: should I introduce final or static members?
	private Connection CON;
	private String CONFIG_DIR;
	private String DB_USER;
	private String DB_PASSWD;
	private String DB_HOST;
	private int DB_PORT;
	private String DB_NAME;
	private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private File LOGDIR;

	public DatabaseManager(String configDir) {
		LOG.info("DataBaseManager configuration STARTED.");
		CONFIG_DIR = configDir;
		CON = configure();
		LOG.info("DataBaseManager configuration FINISHED.");
	}

	private Connection configure() {
		BufferedReader br_mysql = null;
		BufferedReader br_filePath = null;
		File mysqlConfig = null;
		File filePathConfig = null;
		Connection output = null;
		String line = "";

		try {
			File configDir = new File(CONFIG_DIR);
			if (!configDir.isDirectory()) {
				throw new IllegalArgumentException("'" + CONFIG_DIR
						+ "' is not a directory!");
			}

			File[] configs = configDir.listFiles();
			for (File i : configs) {
				if (i.getName().equals("mysql.conf")) {
					mysqlConfig = i;
				}
				if (i.getName().equals("filePath.conf")) {
					filePathConfig = i;
				}
			}
			if (mysqlConfig == null) {
				throw new FileNotFoundException(
						"mysql.conf does not exists in the '" + configDir
								+ "' directory");
			} else {
				br_mysql = new BufferedReader(new FileReader(mysqlConfig));
				while ((line = br_mysql.readLine()) != null) {
					String[] splittedLine = line.split(":");
					switch (splittedLine[0]) {
					case "user_name":
						DB_USER = splittedLine[1];
						break;
					case "passwd":
						DB_PASSWD = splittedLine[1];
						break;
					case "host":
						DB_HOST = splittedLine[1];
						break;
					case "port":
						DB_PORT = Integer.parseInt(splittedLine[1]);
						break;
					case "database_name":
						DB_NAME = splittedLine[1];
						break;
					default:
						throw new IllegalArgumentException(
								"mysql.conf does not have the required format!");

					}
				}
				Class.forName(JDBC_DRIVER);
				String db_url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/"
						+ DB_NAME;
				output = DriverManager
						.getConnection(db_url, DB_USER, DB_PASSWD);
			}

			if (filePathConfig == null) {
				throw new FileNotFoundException(
						"filePath.conf does not exists in the '" + configDir
								+ "' directory");
			} else {
				br_filePath = new BufferedReader(new FileReader(filePathConfig));
				while ((line = br_filePath.readLine()) != null) {
					String[] splittedLine = line.split(":");
					switch (splittedLine[0]) {
					case "log_dir":
						LOGDIR = new File(splittedLine[1]);
						break;
					default:
						throw new IllegalArgumentException(
								"filePath.conf does not have the required format!");

					}
				}
			}
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (br_mysql != null) {
					br_mysql.close();
				}
				if (br_filePath != null) {
					br_filePath.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			return output;
		}
	}

	public void close() {
		try {
			CON.close();
		} catch (SQLException sex) {
			sex.printStackTrace();
		}
		LOG.info("SUCCESS: DataBaseManager was closed.");
	}

	public String queryDate() {
		Statement stmt = null;
		ResultSet rs = null;
		String querySystime = "SELECT DATE_FORMAT(NOW(), '%Y_%m_%d')";
		String date = "";
		try {
			stmt = CON.createStatement();
			rs = stmt.executeQuery(querySystime);
			rs.next();
			date = rs.getString(1);
		} catch (SQLException sex) {
			sex.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sex2) {
				sex2.printStackTrace();
			}
			return date;
		}
	}

	public String queryTables() {
		Statement stmt = null;
		ResultSet rs = null;
		String querySystime = "SHOW TABLES";
		Set<String> tables = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		try {
			stmt = CON.createStatement();
			rs = stmt.executeQuery(querySystime);
			while (rs.next()) {
				tables.add(rs.getString(1));
			}
			;
			for (String i : tables) {
				sb.append(i + "\n");
			}
		} catch (SQLException sex) {
			sex.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sex2) {
				sex2.printStackTrace();
			}
			return sb.toString();
		}
	}

	public boolean existsTable(String tableName) {
		String[] tables = queryTables().split("\n");
		boolean tableExists = false;
		for (String i : tables) {
			if (tableName.equals(i)) {
				tableExists = true;
				break;
			}
		}
		if (!tableExists) {
			LOG.warn(tableName + " table does not exists!");
		}
		return tableExists;
	}

	public void dropTable(String tableName) {
		Statement stmt = null;
		String queryDrop = "DROP TABLE IF EXISTS " + tableName;
		try {
			stmt = CON.createStatement();
			stmt.executeUpdate(queryDrop);
		} catch (SQLException sex) {
			sex.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sex) {
				sex.printStackTrace();
			}
			LOG.info("SUCCESS:+)" + tableName + " table was dropped.");
		}
	}

	public File getLOGDIR() {
		return LOGDIR;
	}

	public Connection getCON() {
		return CON;
	}

}
