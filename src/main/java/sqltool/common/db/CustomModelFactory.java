package sqltool.common.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;


/**
 * Admittedly ugly, this generates a handler for doing custom database stuff,
 * which means anything outside of the standard JDBC and/or meta-data.
 * 
 * Currently there are implementations for Oracle, MySQL and PostgreSQL, and others could be added
 * 
 * @author wjohnson000
 *
 */
public class CustomModelFactory {

	/**
	 * Return a "DbCustomModel" object appropriate for a given database connection
	 * @param conn database connection
	 * @return custom model
	 */
	protected static DbCustomModel getCustomModel(Connection conn) {
		DbCustomModel res = null;
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			String dbName = dbmd.getDatabaseProductName();

			if (dbName.equalsIgnoreCase("Oracle")) {
				res = new sqltool.schema.custom.oracle.OracleCustomModel(conn);
			} else if (dbName.equalsIgnoreCase("MySQL")) {
				res = new sqltool.schema.custom.mysql.MySqlCustomModel(conn);
			} else if (dbName.equalsIgnoreCase("PostgreSQL")) {
			    res = new sqltool.schema.custom.postgres.PostgresCustomModel(conn);
			} else {
			    res = new sqltool.schema.custom.generic.GenericCustomModel(conn);
			}
		} catch (SQLException sqlex) {
		} 
		return res;
	}

	/**
	 * Private constructor, so the class can't be instantiated.
	 */
	private CustomModelFactory() { }
}
