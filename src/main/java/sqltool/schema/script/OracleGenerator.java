package sqltool.schema.script;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sqltool.common.SqlToolkit;

/**
 * Do some Oracle-specific stuff in terms of generating SQL for tables, views,
 * procedures and synonyms.  It overrides {@link DefaultScriptGenerator}.
 * @author wjohnson
 * 
 * Here are some useful views from which lots of goodies can be extracted ..
 * 
 *   ALL_ARGUMENTS      (PL/SQL proc arguments)
 *   ALL_CATALOG        (All TABLE, VIEW and SYNONYM names)
 *   ALL_COLL_TYPES     (COLL type definitions (?) ... XXX_TBL --> XXX_TYP assocations)
 *   ALL_COL_COMMENTS   (Owner, Table_Name, Column_Name, Comments)
 *   ALL_DB_LINKS       (Owner, Db_Link, UserName, Host, Created)
 *   ALL_INDEXES        (Owner, Index_Name, Index_Type, ...)
 *   ALL_IND_COLUMNS    (Index_Owner, Index_Name, ...)
 *   ALL_JAVA_XXXXX     (Bunch of Java-specific tables)
 *   ALL_METHOD_PARAMS  (Owner, Type_Name, Method_Name, ...)  [Useful ??]
 *   ALL_METHOD_RESULTS (Owner, Type_Name, Method_Name, ...)  [Useful ??]
 *   ALL_OBJECTS        (Owner, Object_Name, ..., Object_Type)
 *   ALL_PROCEDURES     (Owner, Object_Name, Procedure_Name) ... all built-in functions, etc.
 *   ALL_SEQUENCES      (Sequence definitions ...)
 *   ALL_SOURCE         (Owner, Name, Type, Line, Text) ... All PL/SQL source !!
 *   ALL_SYNONYMS       (Owner, Synonym_Name, Table_Owner, Table_Name, Db_Link)
 *   ALL_TABLES         (Owner, Table_Name, TableSpace_Name, ...)
 *   ALL_TAB_COLS       (Owner, Table_Name, Column_Name, Data_Type, ...)
 *   ALL_TAB_COLUMNS    (Owner, Table_Name, Column_Name, Data_Type, ...)
 *   ALL_TRIGGERS       (Owner, Trigger_Name, Trigger_Type, ...)
 *   ALL_TRIGGER_COLS   (Trigger_Owner, Trigger_Name, ...)
 *   ALL_TYPES          (Owner, Type_Name, Type_OID, TypeCode ...)  Oracle data types
 *   ALL_TYPE_ATTRS     (Owner, Type_Name, Attr_Name, ...)          Oracle data type details
 *   All_USERS          (UserName, User_ID, Created)
 *   ALL_VIEWS          (Owner, View_Name, Text_Length, Text, ...)
 */
public class OracleGenerator extends DefaultScriptGenerator {
	
	
	/**
	 * Given a catalog, schema and a view name, generate the SQL script that defines
	 * the view.  The default generator does very little
	 * 
	 * @param catalog catalog name
	 * @param schema  schema name
	 * @param entryName view name
	 * 
	 * @return SQL that can be used to describe the view
	 */
	@Override
	public String getViewDef(String catalog, String schema, String entryName) {
		SqlToolkit.appLogger.logDebug("OracleGenerator.getViewDef: " + schema + "." + entryName);

		StringBuffer scriptSB = new StringBuffer(getHeader("view", entryName));
		scriptSB.append("\n");
		scriptSB.append("CREATE VIEW ");
		scriptSB.append(entryName);
		scriptSB.append(" AS\n");
		String query =
			"SELECT Text " +
			"  FROM all_views " +
			" WHERE UPPER(owner) = '" + schema.toUpperCase() + "' " +
			"   AND UPPER(view_name) = '" + entryName.toUpperCase() + "'";
		scriptSB.append(getResults(query));
		return scriptSB.toString();
	}
	
	/**
	 * Given a catalog, schema and a procedure name, generate the SQL script that
	 * defines the procedure.  The default generator does very little
	 * 
	 * @param catalog catalog name
	 * @param schema  schema name
	 * @param entryName procedure name
	 * 
	 * @return SQL that can be used to describe the procedure
	 */
	@Override
	public String getProcedureDef(String catalog, String schema, String entryName) {
		SqlToolkit.appLogger.logDebug("OracleGenerator.getProcedureDef: " + schema + "." + entryName);

		StringBuffer scriptSB = new StringBuffer(getHeader("procedure", entryName));
		scriptSB.append("\n");
		scriptSB.append("CREATE or REPLACE\n");
		String[] fullName = entryName.split("\\.");
		
		String queryOld =
			"SELECT Text " +
			"  FROM sys.all_source " +
			" WHERE UPPER(owner) = '" + schema.toUpperCase() + "' " +
//			"   AND UPPER(name) = '" + entryName.toUpperCase() + "' " +
			"   AND UPPER(name) = '" + fullName[0].toUpperCase() + "' " +
			" ORDER BY type, line";
		String query =
			"SELECT s.source, u.name, o.name, " +
			"       decode(o.type#, 7, 'PROCEDURE', 8, 'FUNCTION', 9, 'PACKAGE', " +
			"                       11, 'PACKAGE BODY', " +
			"                       'UNDEFINED'), " +
			"       s.line " +
			"  FROM sys.obj$ o, sys.source$ s, sys.user$ u " +
			" WHERE o.obj# = s.obj# " +
			"   AND o.owner# = u.user# " +
			"   AND o.type# in (7, 8, 9, 11, 13, 14) " +
			"   AND UPPER(u.name) = '" + schema.toUpperCase() + "' " +
			"   AND UPPER(o.name) = '" + fullName[0].toUpperCase() + "' " +
			" ORDER BY o.type#, s.line";
		String procSrc = getResults(query);
		if (procSrc == null  ||  procSrc.trim().length() == 0) {
			procSrc = getResults(queryOld);
		}
		scriptSB.append(procSrc);
		return scriptSB.toString();
	}
	
	/**
	 * Given an SQL query, execute it and retrieve the results, row-by-row
	 * 
	 * @param query SQL query
	 * @return first column of the results, with the lines concatenated
	 */
	private String getResults(String query) {
		StringBuffer resSB = new StringBuffer(1024);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = dbInfoModel.getConnection().createStatement();
			rset = stmt.executeQuery(query);
			while (rset.next()) {
				String text = rset.getString(1);
				resSB.append(text);
				if (! (text.endsWith("\n")  ||  text.endsWith("\r"))) {
					resSB.append("\n");
				}
			}
		} catch (SQLException sqlex) {
//			System.out.println("SQLEX(OracleGenerator.getResults(): " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (SQLException ex) { }
			try { if (stmt != null) stmt.close(); } catch (SQLException ex) { }
		}
		return resSB.toString();
	}
}
