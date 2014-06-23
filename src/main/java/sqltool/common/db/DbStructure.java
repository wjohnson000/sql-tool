package sqltool.common.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sqltool.common.SqlToolkit;


/**
 * A basic structure for storing information about a given data element,
 * such as a table, view or stored procedure.
 * @author wjohnson000
 *
 */
public abstract class DbStructure {
	List<String> names;
	String[] columns;
	Map<String,List<Map<String,String>>> entries;
	
	/**
	 * Abstract method to be overridden by each subclass.  This method will
	 * return the name of the column that contains the entry name
	 * 
	 * @return  Column name
	 */
	protected abstract String getEntryColumnName();
	
	/**
	 * Abstract method to be overridden by each subclass.  This method will generate
	 * a "ResultSet" that can be used to retrieve a list of entries appropriate for
	 * this structure (tables, procedures, views, etc.)
	 * 
	 * @param conn     JDBC database connection
	 * @param catalog  catalog name
	 * @param schema   schema name
	 * @param type     type of entry (may be null in some cases)
	 * @return         ResultSet containing a list of entries
	 */
	protected abstract ResultSet getEntries(Connection conn, String catalog, String schema, String type)
	throws SQLException;
	
	/**
	 * Abstract method to be overridden by each subclass.  This method will generate
	 * a "ResultSet" that can be used to retrieve the details for a given entry.
	 * 
	 * @param conn       JDBC database connection
	 * @param catalog    catalog name
	 * @param schema     schema name
	 * @param entryName  name of object whose columns we wish to retrieve
	 * @return           ResultSet containing a list of details for the given entry
	 */
	protected abstract ResultSet getColumns(Connection conn, String catalog, String schema, String entryName)
	throws SQLException;
	
	
	public void setNames(Connection conn, String catalog, String schema, String type) {
		names   = new ArrayList<String>(10);
		columns = new String[0];
		entries = new TreeMap<String,List<Map<String,String>>>();
		
		// Retrieve the list of entries (tables, views, etc.)
		ResultSet rset = null;
		try {
			rset = getEntries(conn, catalog, schema, type);
			while (rset.next()) {
				names.add(rset.getString(getEntryColumnName()));
			}
		} catch (SQLException sqlex) {
			System.out.println("DbStruct.I.EX: " + sqlex);
			SqlToolkit.appLogger.logFatal("DbStructure.setNames.EX: " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (Exception ex) { }
		}
		Collections.sort(names);
		SqlToolkit.appLogger.logDebug("DbStructure.setNames, count: " + names.size());
	}
	
	/**
	 * Return the number of names, and the list of names as an array
	 */
	public int getNameCount() {
		return (names == null) ? 0 : names.size();
	}
	
	public String[] getNames() {
		return names.toArray(new String[names.size()]);
	}
	
	/**
	 * Return the number of columns, and the column at a given index
	 */
	public int getColumnCount() {
		return (columns == null) ? 0 : columns.length;
	}
	
	public String getColumnAt(int ndx) {
		if (ndx >= 0  &&  ndx < getColumnCount()) {
			return columns[ndx];
		} else {
			return null;
		}
	}
	
	/**
	 * Return the list of hash-maps associated with a given entry 
	 * @param name key into list
	 * @return list of hash-maps
	 */
	public List<Map<String,String>> getEntry(String name) {
		return entries.get(name);
	}
	
	/**
	 * Ensure that the entry details (column name, type, etc.) have been set for
	 * the given entry.  If not, pull them from the database.
	 * 
	 * @param conn database connection to use
	 * @param schema the schema name in which to search
	 * @param entryName the entry name [table/view] name to check
	 */
	public void populateEntry(Connection conn, String catalog, String schema, String entryName) {
		ResultSet rset = null;
		
		// If the details are already present, do nothing
		if (entries.containsKey(entryName)) {
			return;
		}
		SqlToolkit.appLogger.logDebug("DbStructure.populateEntry: " + entryName);

		// Retrieve the details for the given entry
		List<Map<String,String>> detailList = new ArrayList<Map<String,String>>(10);
		try {
			rset = getColumns(conn, catalog, schema, entryName);
			ResultSetMetaData rsmd = rset.getMetaData();
			
			// Don't bother retrieving the column data if already present
			if (columns.length == 0) {
				int colCnt = rsmd.getColumnCount();
				columns = new String[colCnt];
				for (int i=0;  i<colCnt;  i++) {
					columns[i] = rsmd.getColumnName(i+1);
				}
			}
			
			// Get all column data for this entry
			while (rset.next()) {
				HashMap<String,String> hmTemp = new HashMap<String,String>();
				for (int i=0;  i<columns.length;  i++) {
					String key = columns[i];
					String val = rset.getString(key);
					hmTemp.put(key, val);
				}
				detailList.add(hmTemp);
			}
			entries.put(entryName, detailList);
		} catch (SQLException sqlex) {
			System.out.println("DbStruct.II.EX: " + sqlex);
			SqlToolkit.appLogger.logFatal("DbStructure.populateEntry.EX: " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (Exception ex) { }
		}
	}
}
