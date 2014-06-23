package sqltool.common.db;

import java.sql.*;


/**
 * Concrete class for managing information about the tables or views in a given
 * catalog/schema.
 * 
 * @author wjohnson000
 *
 */
public class DbTableSet extends DbStructure {

	/**
	 * Create an instance for a given database, catalog and schema.  At least
	 * one of "catalog" and "schema" should be non-null.  Note: the "catalog"
	 * and "schema" values are transient, and so appear in most other method's
	 * parameter list.
	 * 
	 * @param conn database connection
	 * @param catalog catalog name, could be null
	 * @param schema schema name, could be null
	 */
	public DbTableSet(Connection conn, String catalog, String schema, String type) {
		setNames(conn, catalog, schema, type);
	}
	
	/**
	 * @return the column header name for this field
	 */
	protected String getEntryColumnName() {
		return "TABLE_NAME";
	}

	/**
	 * Get a "ResultSet" representing data for all tables or views in the given
	 * database/catalog/schema.
	 * 
	 * @param conn database connection
	 * @param catalog catalog name, could be null
	 * @param schema schema name, could be null
	 * @param type of data, which could be "table" or "view"
	 * @return ResultSet containing all procedure names
	 */
	protected ResultSet getEntries(Connection conn, String catalog, String schema, String type)
	throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		return dbmd.getTables(catalog, schema, null, new String[] { type });
	}

	/**
	 * Get a "ResultSet" representing column data for the given
	 * database/catalog/schema/table or view.
	 * 
	 * @param conn database connection
	 * @param catalog catalog name, could be null
	 * @param schema schema name, could be null
	 * @param entryName procedure name
	 * @return ResultSet containing column definitions for the given procedure
	 */
	protected ResultSet getColumns(Connection conn, String catalog, String schema, String entryName)
	throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		return dbmd.getColumns(catalog, schema, entryName, null);
	}
}
