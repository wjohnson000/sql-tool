package sqltool.schema.script;

import sqltool.common.db.DbInfoModel;


/**
 * Interface that defines operations that need to be supported in the
 * standard schema view.
 * 
 * @author wjohnson000
 *
 */
public interface ScriptGenerator {
	/**
	 * Set the DbInfoModel, from which all table, view, procedure and synonym
	 * data can be retrieved
	 * @param dbInfoModel the DbInfoModel instance
	 */
	public void   setDbInfoModel(DbInfoModel dbInfoModel);

	/**
	 * Generate DDL that can be used to create a table
	 * @param catalog catalog name, or null
	 * @param schema schema name, or numm
	 * @param entryName table name
	 * @return DDL to create the table
	 */
	public String getTableDef(String catalog, String schema, String entryName);

	/**
	 * Generate DDL that can be used to create a view
	 * @param catalog catalog name, or null
	 * @param schema schema name, or numm
	 * @param entryName table name
	 * @return DDL to create the vier
	 */
	public String getViewDef(String catalog, String schema, String entryName);

	/**
	 * Generate DDL that can be used to create a procedure
	 * @param catalog catalog name, or null
	 * @param schema schema name, or numm
	 * @param entryName table name
	 * @return DDL to create the procedure
	 */
	public String getProcedureDef(String catalog, String schema, String entryName);

	/**
	 * Generate DDL that can be used to create a synonym
	 * @param catalog catalog name, or null
	 * @param schema schema name, or numm
	 * @param entryName table name
	 * @return DDL to create the synonym
	 */
	public String getSynonymDef(String catalog, String schema, String entryName);
}
