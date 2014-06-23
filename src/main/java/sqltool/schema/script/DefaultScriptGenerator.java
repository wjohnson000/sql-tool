package sqltool.schema.script;

import java.util.List;
import java.util.Map;

import sqltool.common.SqlToolkit;
import sqltool.common.db.DbInfoModel;
import sqltool.common.db.DbStructure;


/**
 * Implements the {@link ScriptGenerator} interface with a basic set
 * standard SQL commands.
 * 
 * @author wjohnson000
 *
 */
public class DefaultScriptGenerator implements ScriptGenerator {
	DbInfoModel dbInfoModel;
	
	/**
	 * Set the "DbInfoModel" which caches lots of meta-data and has lots of
	 * lovely methods to extract even more.
	 * 
	 * @param dbInfoModel "DbInfoModel" instance for this go
	 */
	public void   setDbInfoModel(DbInfoModel dbInfoModel) {
		this.dbInfoModel = dbInfoModel;
	}
	
	
	/**
	 * Given a catalog, schema and a table name, generate the SQL script that defines
	 * the table and would allow you to re-build the table, including the columns,
	 * primary keys, indexes and privileges.
	 * 
	 * @param catalog catalog name
	 * @param schema  schema name
	 * @param entryName table name
	 * 
	 * @return SQL that can be used to generate the table
	 */
	@Override
	public String getTableDef(String catalog, String schema, String entryName) {
		SqlToolkit.appLogger.logDebug("DefaultGenerator.getTableDef: " + catalog + "." + schema + "." + entryName);

		StringBuffer scriptSB = new StringBuffer(getHeader("table", entryName));
		
		dbInfoModel.populateDetails(catalog, schema, DbInfoModel.MODE_TABLE, entryName);
		DbStructure currDbStruct = dbInfoModel.getDbStructure(catalog, schema, DbInfoModel.MODE_TABLE);
		if (currDbStruct != null) {
			List<Map<String,String>> detailList = currDbStruct.getEntry(entryName);
			List<Map<String,String>> pkList = dbInfoModel.getPrimaryKeys(catalog, schema, entryName);
			List<Map<String,String>> ixList = dbInfoModel.getIndexInfo(catalog, schema, entryName);
			List<Map<String,String>> tpList = dbInfoModel.getTablePrivilege(catalog, schema, entryName);
			
			scriptSB.append("\n");
			scriptSB.append("-- TRUNCATE TABLE " + entryName + "\n");
			scriptSB.append("\n");
			scriptSB.append("CREATE TABLE " + entryName + "\n");
			scriptSB.append("(\n");
			writeColumn(detailList, scriptSB);
			writePrimaryKey(pkList, scriptSB);
			scriptSB.append("\n);");
			writeIndex(ixList, scriptSB);
			writeTablePrivilege(tpList, scriptSB);
		}
		return scriptSB.toString();
	}
	
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
		String res = getHeader("view", entryName);
		return res;
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
		String res = getHeader("procedure", entryName);
		return res;
	}
	
	/**
	 * Given a catalog, schema and a synonym name, generate the SQL script that
	 * defines the synonym.  The default generator does a decent job of guessing
	 * the result.
	 * 
	 * @param catalog catalog name
	 * @param schema  schema name
	 * @param entryName synonym name
	 * 
	 * @return SQL that can be used to describe the view
	 */
	@Override
	public String getSynonymDef(String catalog, String schema, String entryName) {
		StringBuffer scriptSB = new StringBuffer(getHeader("synonym", entryName));
		scriptSB.append("\n\n");
		scriptSB.append("CREATE synonym ");
		scriptSB.append(entryName);
		scriptSB.append(" ");
		scriptSB.append(schema);
		scriptSB.append(".");
		scriptSB.append(entryName);
		scriptSB.append(";\n");
		return scriptSB.toString();
	}
	
	/**
	 * Given a type (TABLE, VIEW, etc.) and an entry name, generate some generic
	 * comments that will appear at the beginning of every entry
	 * 
	 * @param type object type (TABLE, VIEW, PROCEDURE or SYNONYM)
	 * @param entryName object name
	 * 
	 * @return SQL comment that can be used as a generic header
	 */
	protected String getHeader(String type, String entryName) {
		StringBuffer scriptSB = new StringBuffer(512);
		scriptSB.append("-- This is an auto-generated script to create the ");
		scriptSB.append(type);
		scriptSB.append(" object as\n");
		scriptSB.append("-- it is currently defined in the database.  There is no guarantee\n");
		scriptSB.append("-- that you won't have to 'tweak' it slightly to re-create the actual\n");
		scriptSB.append("-- definition.  But it's a start ...\n");
		scriptSB.append("\n");
		scriptSB.append("-- DROP ");
		scriptSB.append(type.toUpperCase());
		scriptSB.append(" ");
		scriptSB.append(entryName);
		scriptSB.append(";\n");
		return scriptSB.toString();
	}
	
	/**
	 * Output the column definitions for a table
	 * 
	 * @param colDefs List of column definitions, each entry being a Map that
	 *        contains the information for a column
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	private void writeColumn(List<Map<String,String>> colDefs, StringBuffer scriptSB) {
		if (colDefs == null) {
			scriptSB.append("Can't get the column definitions ...\n");
			return;
		}

		int cLen = 0;
		int tLen = 14;
		for (int row=0;  row<colDefs.size();  row++) {
			Map<String,String> hmTemp = colDefs.get(row);
			String colName = hmTemp.get("COLUMN_NAME");
			cLen = Math.max(cLen, colName.length());
		}
		
		for (int row=0;  row<colDefs.size();  row++) {
			Map<String,String> hmTemp = colDefs.get(row);
			String colName = hmTemp.get("COLUMN_NAME");
			String typName = hmTemp.get("TYPE_NAME");
			String colSize = hmTemp.get("COLUMN_SIZE");
			String decDigs = hmTemp.get("DECIMAL_DIGITS");
			String isNull  = hmTemp.get("IS_NULLABLE");
			
			scriptSB.append("   " + colName);
			for (int pad=0;  pad<cLen-colName.length()+3;  pad++) {
				scriptSB.append(" ");
			}
			int xLen1 = scriptSB.length();
			scriptSB.append(typName + "(");
			scriptSB.append(colSize);
			if (decDigs != null  &&  decDigs.length() > 0  &&  ! decDigs.equals("0")) {
				scriptSB.append("," + decDigs);
			}
			scriptSB.append(")");
			if ("NO".equals(isNull)) {
				int xLen2 = scriptSB.length();
				for (int pad=0;  pad<tLen-(xLen2-xLen1)+3;  pad++) {
					scriptSB.append(" ");
				}
				scriptSB.append("NOT NULL");
			}
			if (row < colDefs.size()-1) {
				scriptSB.append(",\n");
			}
		}
	}
	
	/**
	 * Output the primary key definitions for a table, or an appropriate
	 * comment if we weren't able to get the primary key definitions
	 * 
	 * @param colDefs List of primary key definitions, each entry being a
	 *        Map that contains the information for a key entry
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	private void writePrimaryKey(List<Map<String,String>> colDefs, StringBuffer scriptSB) {
		for (int row=0;  row<colDefs.size();  row++) {
			Map<String,String> hmTemp = colDefs.get(row);
			if (row == 0) {
				scriptSB.append(",\n   PRIMARY KEY (");
			} else  { //if (row < colDefs.size()-1) {
				scriptSB.append(", ");
			}
			scriptSB.append(hmTemp.get("COLUMN_NAME"));
		}
		if (colDefs.size() > 0) {
			scriptSB.append(")");
		} else if (colDefs == DbInfoModel.INVALID_PRIMARY_KEY) {
			scriptSB.append("\n\n-- Unable to retrieve list of table primary keys");
		}
	}
	
	/**
	 * Output the index definition(s) for a table, or an appropriate
	 * comment if we weren't able to get the index definitions 
	 * @param colDefs List of index definitions, each entry being a
	 *        Map that contains the information for an index (one
	 *        entry per field per primary key)
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	private void writeIndex(List<Map<String,String>> colDefs, StringBuffer scriptSB) {
		String prevIdx = null;
		for (int row=0;  row<colDefs.size();  row++) {
			Map<String,String> hmTemp = colDefs.get(row);
			String tblName = hmTemp.get("TABLE_NAME");
			String nonUniq = hmTemp.get("NON_UNIQUE");
			String idxName = hmTemp.get("INDEX_NAME");
			String colName = hmTemp.get("COLUMN_NAME");
			if (idxName != null) {
				if (! idxName.equals(prevIdx)) {
					if (prevIdx != null) {
						scriptSB.append(");");
					}
					scriptSB.append("\n\nCREATE ");
					if ("0".equals(nonUniq)) {
						scriptSB.append("UNIQUE ");
					}
					scriptSB.append("INDEX ");
					scriptSB.append(idxName);
					scriptSB.append(" ON ");
					scriptSB.append(tblName);
					scriptSB.append(" (");
					scriptSB.append(colName);
					prevIdx = idxName;
				} else {
					scriptSB.append(", ");
					scriptSB.append(colName);
				}
			}
		}
		if (prevIdx != null) {
			scriptSB.append(");");
		} else if (colDefs == DbInfoModel.INVALID_INDEX) {
			scriptSB.append("\n\n-- Unable to retrieve list of table indexes");
		}
	}
	
	/**
	 * Output the privilege definition(s) for a table, or an appropriate
	 * comment if we weren't able to get the privilege definitions 
	 * @param colDefs List of privilege definitions, each entry being a
	 *        Map that contains the information for a single "grant"
	 * @param scriptSB the "StringBuffer" that takes the results
	 */
	private void writeTablePrivilege(List<Map<String,String>> colDefs, StringBuffer scriptSB) {
		for (int row=0;  row<colDefs.size();  row++) {
			Map<String,String> hmTemp = colDefs.get(row);
			String tblName = hmTemp.get("TABLE_NAME");
			String prvlege = hmTemp.get("PRIVILEGE");
			String grantee = hmTemp.get("GRANTEE");
			String isGrant = hmTemp.get("IS_GRANTABLE");
			scriptSB.append("\n\nGRANT ");
			scriptSB.append(prvlege);
			scriptSB.append(" ON ");
			scriptSB.append(tblName);
			scriptSB.append(" TO ");
			scriptSB.append(grantee);
			if ("YES".equalsIgnoreCase(isGrant)) {
				scriptSB.append(" WITH GRANT OPTION");
			}
			scriptSB.append(";");
		}
		if (colDefs == DbInfoModel.INVALID_PRIVILEGE) {
			scriptSB.append("\n\n-- Unable to retrieve list of table privileges");
		}
	}
}
