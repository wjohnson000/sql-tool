package sqltool.common.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import sqltool.common.SqlToolkit;


/**
 * Cache "DbInfoModel" stuff: this is fairly expensive to retrieve using
 * DatabaseMetaData, but rarely changes.
 * 
 * @author wjohnson000
 */
public class DbInfoCache {
	
	private static DbInfoCache onlyInstance = new DbInfoCache();
	private Map<Connection,DbInfoModel> myCache = new HashMap<Connection,DbInfoModel>();
	
	/**
	 * Private constructor to enforce the "singleton" pattern
	 */
	private DbInfoCache() {
		myCache = new HashMap<Connection,DbInfoModel>();
	}
	
	/**
	 * Retrieve the "DbInfoModel" associated with this connection ...
	 * @param conn JDBC connection, already established
	 * @return Fully or partially-retrieved "DbInfoModel" for this connection
	 */
	public static DbInfoModel GetInfoModel(Connection conn) {
		SqlToolkit.appLogger.logDebug("DbInfoCache.GetInfoModel: " + conn);
		return onlyInstance.getInfoModel(conn);
	}
	
	/**
	 * A database is being closed or invalidated or refreshed, and we need
	 * to clear out whatever we have for the connection 
	 * @param conn JDBC connection, already established
	 */
	public static void ReleaseInfoModel(Connection conn) {
		SqlToolkit.appLogger.logDebug("DbInfoCache.ReleaseInfoModel: " + conn);
		onlyInstance.myCache.remove(conn);
	}
	
	/**
	 * Retrieve the "DbInfoModel" associated with this connection ...
	 * @param conn JDBC connection, already established
	 * @return Fully or partially-retrieved "DbInfoModel" for this connection
	 */
	private DbInfoModel getInfoModel(Connection conn) {
		DbInfoModel dbModel = (DbInfoModel)myCache.get(conn);
		if (dbModel == null) {
			dbModel = new DbInfoModel(conn);
			myCache.put(conn, dbModel);
		}
		return dbModel;
	}
}
