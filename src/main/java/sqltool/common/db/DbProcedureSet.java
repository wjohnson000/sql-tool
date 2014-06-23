package sqltool.common.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import sqltool.common.SqlToolkit;


/**
 * Concrete class for managing information about the procedures in a given
 * catalog/schema.
 * 
 * @author wjohnson000
 *
 */
public class DbProcedureSet extends DbStructure {

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
	public DbProcedureSet(Connection conn, String catalog, String schema) {
		setNames(conn, catalog, schema, null);
	}

	/**
	 * @return the column header name for this field
	 */
	protected String getEntryColumnName() {
		return "PROCEDURE_NAME";
	}

	/**
	 * Get a "ResultSet" representing data for all procedures in the given
	 * database/catalog/schema.
	 * 
	 * @param conn database connection
	 * @param catalog catalog name, could be null
	 * @param schema schema name, could be null
	 * @param type type of date:  ignored
	 * @return ResultSet containing all procedure names
	 */
	protected ResultSet getEntries(Connection conn, String catalog, String schema, String type)
	throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		return dbmd.getProcedures(catalog, schema, null);
	}
	
	/**
	 * Get a "ResultSet" representing column data for the given
	 * database/catalog/schema/procedure.
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
		return dbmd.getProcedureColumns(catalog, schema, entryName, null);
	}

	/**
	 * Retrieve a list of all procedures in the given database/catalog/schema.
	 * @param conn database connection
	 * @param catalog catalog name, could be null
	 * @param schema schema name, could be null
	 * @param type type of data to retrieved
	 */
	public void setNames(Connection conn, String catalog, String schema, String type) {
		names   = new ArrayList<String>(10);
		columns = new String[0];
		entries = new TreeMap<String,List<Map<String,String>>>();
		Set<String> inUse = new HashSet<String>(1000);
		
		// Retrieve the list of entries (tables, views, etc.)
		ResultSet rset = null;
		try {
			rset = getEntries(conn, catalog, schema, type);
			while (rset.next()) {
				String owner = rset.getString("PROCEDURE_CAT");
				String name  = rset.getString(getEntryColumnName());
				if (owner == null) {
					names.add(name); //rset.getString(getEntryColumnName()));
				} else {
					String entry = owner + ".<package>";
					if (! inUse.contains(entry)) {
						inUse.add(entry);
						names.add(entry);
					}
				}
			}
		} catch (SQLException sqlex) {
			System.out.println("DbStruct.I.EX: " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (Exception ex) { }
		}
		Collections.sort(names);
		SqlToolkit.appLogger.logDebug("DbProcedureSet.setNames, count: " + names.size());
	}
}
