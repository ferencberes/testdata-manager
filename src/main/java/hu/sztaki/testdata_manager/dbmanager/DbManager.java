package hu.sztaki.testdata_manager.dbmanager;

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

public class DbManager {

	private Connection CON;
	private static String CONFIG_DIR;
	private static String DB_USER;
	private static String DB_PASSWD;
	private static String DB_HOST;
	private static int DB_PORT;
	private static String DB_NAME;
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static File LOGDIR;

	public static void loadDBParameters(String configDir) {
		CONFIG_DIR = configDir;
	}
	
	public DbManager() {
		CON = configure(CONFIG_DIR);
	}

	public Connection configure(String confDir) {
		BufferedReader br_mysql = null;
		BufferedReader br_filePath = null;
		File mysqlConfig = null;
		File filePathConfig = null;
		Connection output = null;
		String line = "";

		try {
			File configDir = new File(confDir);
			if (!configDir.isDirectory()) {
				throw new IllegalArgumentException("'" + confDir
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
		return tableExists;
	}

	public void dropTable(String tableName) {
		Statement stmt = null;
		// TODO: if not exists stdout some msg
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
			System.out.println(tableName + " table is dropped.");
		}
	}
	
	public static File getLOGDIR() {
		return LOGDIR;
	}

	public Connection getCON() {
		return CON;
	}

}

