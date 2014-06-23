package sqltool.schema.custom.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sqltool.common.db.DbRawDataType;


/**
 * Implement the {@link DbRawDataType} interface for MySQL-specific data.
 * This will serve as the base class for all MySQL types that we are about.
 * @author wjohnson000
 *
 */
public abstract class BaseDataType implements DbRawDataType {

//	===========================================================================
//	static stuff ... constants, if you will ...
//	===========================================================================
	protected static final String DISCLAIMER =
		"-- This is an auto-generated script to create the DB object as \n" +
		"-- it is currently defined in the database.  There is no guarantee \n" +
		"-- that you won't have to 'tweak' it slightly to re-create the actual \n" +
		"-- definition.  But it's a start ...\n" +
		"\n";

	protected static final String PUBLIC_PUBLIC = "PUBLIC";


//	===========================================================================
//	instance variable(s)
//	===========================================================================
	String     sqlMessage;
	Connection conn;


	/**
	 * Return the result [message] of the latest SQL attempt ...
	 */
	protected String getSqlMessage() {
		return sqlMessage;
	}


	/**
	 * By default, a data type can generate a "CREATE OR REPLACE" instruction
	 * 
	 * @return TRUE if this object can have "CREATE ..." representation, and
	 *         FALSE otherwise
	 */
	public boolean canCreate() {
		return true;
	}


	/**
	 * By default, a data type can generate a "DROP" instruction
	 * 
	 * @return TRUE if this object can have "DROP ..." representation, and
	 *         FALSE otherwise
	 */
	public boolean canDrop() {
		return true;
	}


	/**
	 * Set the underlying JDBC connection instance to use
	 * 
	 * @param conn JDBC connection, already open and ready to use
	 */
	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	/**
	 * Retrieve the source for this thing, if it exists.  The source is the
	 * original DDL used to create it.
	 * @param catalog catalog name, or null
	 * @param schema schema name, or null
	 * @param entry object name
	 */
	public String getSource(String catalog, String schema, String entry) {
		return getSource(catalog, schema, entry, getDataType());
	}

	/**
	 * Retrieve the source for this thing, if it exists.  The source is the
	 * original DDL used to create it.
	 * @param catalog catalog name, or null
	 * @param schema schema name, or null
	 * @param entry object name
	 * @param type type of object
	 */
	public String getSource(String catalog, String schema, String entry, String type) {
		return null;
	}

	/**
	 * Return the sql for doing a "DROP" on the given object ... 
	 */
	public String getDrop(String catalog, String schema, String entry) {
		return DISCLAIMER + "DROP " + getDataType() + " " + qualifiedName(schema, entry) + ";";
	}

	/**
	 * Generic method for getting the results of a query.  The data is
	 * returned as a list of "String" array.
	 * @param query query to execute
	 * @return List of row data, each row being a "String" array
	 */
	protected List<String[]> runQuery(String query) {
		return runQuery(query, false);
	}

	/**
	 * Generic method for getting the results of a query.  The data is
	 * returned as a list of "String" array.
	 * @param query query to execute
	 * @param includeColNames flag to indicate of column names are to be
	 *        returned as the first row
	 * @return List of row data, each row being a "String" array
	 */
	protected List<String[]> runQuery(String query, boolean includeColNames) {
		List<String[]> res = new ArrayList<String[]>(100);
		Statement stmt = null;
		ResultSet rset = null;

		sqlMessage = "";
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rset.getMetaData();
			int count = rsmd.getColumnCount();

			// Add the column names, if so requested
			if (includeColNames) {
				String[] row = new String[count];
				for (int i=0;  i<count;  i++) {
					row[i] = rsmd.getColumnName(i+1);
				}
				res.add(row);
			}

			// Add the returned data
			while (rset.next()) {
				String[] row = new String[count];
				for (int i=0;  i<count;  i++) {
					row[i] = rset.getString(i+1);
				}
				res.add(row);
			}
			sqlMessage = "OK";
		} catch (Exception sqlex) {
			sqlMessage = sqlex.getMessage();
		} finally {
			if (rset != null) try { rset.close(); } catch (Exception ex) { }
			if (stmt != null) try { stmt.close(); } catch (Exception ex) { }
		}
		return res;
	}

	/**
	 * Create a fully-qualified entry name, either "SCHEMA.ENTRY" or "ENTRY"
	 * @param scheme schema name
	 * @param entry entry name
	 */
	protected String qualifiedName(String schema, String entry) {
		return (schema == null ? "" : (schema + ".")) + entry;
	}


	/**
	 * Output the column definitions for a table or view
	 * 
	 * @param colDefs List of column definitions, each entry being a String[] that
	 *        has the column name, type, null-ability, key, default, and "extra"
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	protected void writeColumnDef(List<String[]> colDefs, StringBuffer scriptSB) {
		int cLen = 0;
		for (String[] row : colDefs) {
			String colName = row[0];
			cLen = Math.max(cLen, colName.length());
		}
		
		for (int i=0;  i<colDefs.size();  i++) {
			String[] row = colDefs.get(i);
			String colName = row[0];
			String typName = row[1];
			String isNull  = row[2];
			String colDflt = row[4];
			String extra   = row[5];

			scriptSB.append("   " + colName);
			for (int pad=0;  pad<cLen-colName.length()+3;  pad++) {
				scriptSB.append(" ");
			}

			scriptSB.append(" ").append(typName);
			if ("NO".equalsIgnoreCase(isNull)) {
				scriptSB.append("  NOT NULL");
			}

			if (colDflt != null  &&  colDflt.length() > 0) {
				scriptSB.append("  DEFAULT ").append(colDflt);
			}

			if (extra != null  &&  extra.length() > 0) {
				scriptSB.append("  ").append(extra);
			}

			if (i < colDefs.size()-1) {
				scriptSB.append(",\n");
			}
		}
	}
}
