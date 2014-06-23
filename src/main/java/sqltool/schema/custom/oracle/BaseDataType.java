package sqltool.schema.custom.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import sqltool.common.db.DbRawDataType;


/**
 * Implement the {@link DbRawDataType} interface for Oracle-specific data.
 * This will serve as the base class for all Oracle types that we are about.
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

	/** Keep track of datatypes that don't need additional parenthesis */
	protected static HashSet<String> NO_PARENS = new HashSet<String>(20);
	static {
		NO_PARENS.add("DATE");
		NO_PARENS.add("TIMESTAMP(6)");
		NO_PARENS.add("TIMESTAMP(6) WITH LOCAL TIME ZONE");
	}

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
		String query =
			"Select Text " +
			"  From sys.ALL_SOURCE " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(name)  = '" + entry.toUpperCase() + "'" +
			"   And upper(type)  = '" + type.toUpperCase() + "'" +
			" Order By Line ";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(rows.size() * 80);
		for (String[] row : rows) {
			scriptSB.append(row[0]);
			if (! (row[0].endsWith("\n")  ||  row[0].endsWith("\r"))) {
				scriptSB.append("\n");
			}
		}

		return scriptSB.toString();
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
		List<String[]> res = new ArrayList<String[]>(100);
		Statement stmt = null;
		ResultSet rset = null;

		sqlMessage = "";
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rset.getMetaData();
			int count = rsmd.getColumnCount();
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
	 * Output the column definitions for a table
	 * 
	 * @param colDefs List of column definitions, each entry being a String[] that
	 *        has the column name, data type, length, precision, scale and nullable
	 *        values
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	protected void writeColumnDef(List<String[]> colDefs, StringBuffer scriptSB) {
		int cLen = 0;
		int tLen = 14;
		for (String[] row : colDefs) {
			String colName = row[0];
			cLen = Math.max(cLen, colName.length());
		}
		
		for (int i=0;  i<colDefs.size();  i++) {
			String[] row = colDefs.get(i);
			String colName = row[0];
			String typName = row[1];
			String colSize = row[2];
			String colPrcs = row[3];
			String colScal = row[4];
			String isNull  = row[5];

			boolean parens = ! NO_PARENS.contains(typName);
			if (parens  &&  (colSize == null  ||  colSize.length() == 0)  &&  (colPrcs == null  ||  colPrcs.length() == 0)) {
				parens = false;
			}
			scriptSB.append("   " + colName);
			for (int pad=0;  pad<cLen-colName.length()+3;  pad++) {
				scriptSB.append(" ");
			}

			int xLen1 = scriptSB.length();
			scriptSB.append(typName);
			if (parens) {
				scriptSB.append("(");
				if (colPrcs != null  &&  colPrcs.length() > 0) {
					if (colScal != null  &&  colScal.length() > 0  &&  ! "0".equals(colScal)) {
						scriptSB.append(colPrcs + "," + colScal);
					} else {
						scriptSB.append(colPrcs);
					}
				} else {
					scriptSB.append(colSize);
				}
				scriptSB.append(parens ? ")" : "");
			}

			if ("N".equals(isNull)) {
				int xLen2 = scriptSB.length();
				for (int pad=0;  pad<tLen-(xLen2-xLen1)+3;  pad++) {
					scriptSB.append(" ");
				}
				scriptSB.append("NOT NULL");
			}
			if (i < colDefs.size()-1) {
				scriptSB.append(",\n");
			}
		}
	}
}
