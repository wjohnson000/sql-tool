package sqltool.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import sqltool.common.db.DbInfoCache;
import sqltool.server.DbDefinition;


/**
 * This class tries to make a database connection on a separate thread so
 * that the calling class can check the progress periodically.  If too
 * much time passes it can cancel the operation so the attempt to connect
 * doesn't hang the application.
 * 
 * @author wjohnson000
 *
 */
class ConnectionWorker implements Runnable {

	private Connection conn = null;  // JDBC Connection
	private String url;              // Database URL, vendor-specific format
	private String user;             // User name
	private String password;         // Password
	private SQLException reason;     // SQLException thrown during connect attempt 
	private boolean isDone = false;  // Flag set when the operation completes


	/**
	 * Constructor takes the standard three parameters: URL, username, password
	 * @param url Database URL
	 * @param user Database user name
	 * @param password Database password for the user
	 */
	public ConnectionWorker(String url, String user, String password) {
		this.url      = url;
		this.user     = user;
		this.password = password;
	}


	/**
	 * The "run()" method must be defined for all "Runnable" classes.
	 * All we do is wrap the connection attempt inside a try/catch
	 * block, saving the exception if one is thrown.
	 */
	@Override
	public void run() {
		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException sqlex) {
			reason = sqlex;
		}
		isDone = true;
	}


	/**
	 * Return the database connection, or re-throw whatever SQL
	 * exception occurred when we tried to connect.
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		if (reason != null) {
			throw reason;
		}
		return conn;
	}


	/**
	 * Check to see if the "connect" operation has finished
	 * @return
	 */
	public boolean isDone() {
		return isDone;
	}
}


/**
 * Main class with static methods.  We cache database connections if so
 * dictated.  We'll always look for a cached connection before trying to
 * create a new one.
 * 
 * @author wjohnson000
 */
public class ConnectionManager {
	// Manage a cache of connections
	private static HashMap<DbDefinition,Connection> connCache = new HashMap<DbDefinition,Connection>();

	// Manage a cache of error messages, specific to a connection
	private static HashMap<DbDefinition,String> errorMsgCache = new HashMap<DbDefinition,String>();


	// Make the constructor private so nobody can instantiate one or
	// accidentally shutdown a database connection
	private ConnectionManager() { }


	/**
	 * Get a specific database connection and optionally cache it for future
	 * reference.  The connection parameters are supplied in the "DbDefinition"
	 * parameter.  This method uses the "ConnectionWorker" to make the attempt
	 * in a separate thread so the calling application won't be blocked for
	 * more than 5 seconds.
	 */
	public static Connection GetConnection(DbDefinition dbDef, boolean cacheIt) {
		Thread connThread = null;
		ConnectionWorker worker = null;

		Connection conn = connCache.get(dbDef);
		// If we have a valid connection, ensure it's still active and viable; if
		// a "validation" query has been defined, run it to ensure that the
		// connection is still open
		if (conn != null) {
		    ResultSet rset = null;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				if (dbDef.getTestQuery() != null  &&  dbDef.getTestQuery().trim().length() > 0) {
				    rset = stmt.executeQuery(dbDef.getTestQuery().trim());
				}
			} catch (Exception ex) {
				SqlToolkit.appLogger.logFatal("   ConnectionManager.GetConnection.EX: " + ex);
				DbInfoCache.ReleaseInfoModel(conn);
				try {
					conn.close();
				} catch (Exception ex2) {}
				conn = null;
			} finally {
                if (rset != null) try { rset.close(); } catch (Exception ex) { }
				if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
			}
		}

		// Create a new connection if required
		if (conn == null) {
			try {
				Class.forName(dbDef.getDriver());
				worker = new ConnectionWorker(dbDef.getURL(), dbDef.getUser(), dbDef.getPassword());
				connThread = new Thread(worker);
				connThread.start();	// Shutdown a database connection


				// Wait up to 5 seconds in 1/4-second intervals for the database
				// connection to be made
				for (int i=0;  i<20 && !worker.isDone();  i++) {
					try {
						Thread.sleep(250);
					} catch (Exception ex) { }
				}

				// If the connection hasn't been made, interrupt the thread
				if (! worker.isDone()) {
					connThread.interrupt();
				}

				// Get the connection from the worker; if it is null it means we
				// timed out
				conn = worker.getConnection();
				if (conn == null) {	// Shutdown a database connection
					errorMsgCache.put(dbDef, "Unable to connect to database ... operation timed out");
				} else if (cacheIt) {
					connCache.put(dbDef, conn);
				}
			} catch (SQLException ex) {
				String msg = ex.getMessage();
				ex = ex.getNextException();
				while (ex != null) {
					msg += "\n  -- " + ex.getMessage().trim();
					ex = ex.getNextException();
				}
				errorMsgCache.put(dbDef, msg);
				conn = null;
			} catch (ClassNotFoundException ex) {
				errorMsgCache.put(dbDef, "Unknown driver: " + dbDef.getDriver());
			} finally {
				connThread = null;	// Shutdown a database connection
			}
		}
		return conn;
	}


	/**
	 * Return the error message (reason) for a connection failure
	 */
	public static String GetConnectionError(DbDefinition dbDef) {
		return errorMsgCache.get(dbDef);
	}


	/**
	 * Close a specific database connection and remove it from the cache
	 */
	public static void ShutDown(DbDefinition dbDef) {
		ShutDownPrivate(dbDef);
		connCache.remove(dbDef);
	}


	/**
	 * Close all database connections
	 */
	public static void ShutDown() {
		Iterator<DbDefinition> iter = connCache.keySet().iterator();
		while (iter.hasNext()) {
			DbDefinition dbDef = iter.next();
			ShutDownPrivate(dbDef);
			iter.remove();
		}
	}


	/**
	 * Return the name (alias) of every connection that is currently
	 * being managed.
	 */
	public static Iterator<DbDefinition> GetDbDefList() {
		return connCache.keySet().iterator();
	}


	/**
	 * Internal method to shut down (close) a database connection
	 */
	private static void ShutDownPrivate(DbDefinition dbDef) {
		Connection conn = GetConnection(dbDef, true);
		DbInfoCache.ReleaseInfoModel(conn);
		try {
			conn.close();
		} catch (Exception ex) {}
	}
}