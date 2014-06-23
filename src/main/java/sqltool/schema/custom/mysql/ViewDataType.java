package sqltool.schema.custom.mysql;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "View" data, which are the
 * Views.
 * 
 * @author wjohnson000
 *
 */
public class ViewDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public ViewDataType() { }

	/**
	 * Get a name for this data type, which is "View"
	 * @return display name
	 */
	public String getDataType() {
		return "VIEW";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SHOW FULL TABLES " +
			" FROM " + catalog + 
			" WHERE TABLE_TYPE = 'VIEW' ";

		List<String[]> rows = runQuery(query);
		String[] entries = new String[rows.size()];
		for (int i=0;  i<entries.length;  i++) {
			String[] row = rows.get(i);
			entries[i] = row[0];
		}
		return entries;
	}
	
	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getSource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getSource(String catalog, String schema, String entry) {
		String res = super.getSource(catalog, schema, entry);
		if (res != null  &&  res.length() > 0) {
			return res;
		}

		String query =
			"SHOW COLUMNS FROM " + entry + " IN " + catalog;

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE VIEW " + qualifiedName(schema, entry) + " (\n");
		writeColumnDef(rows, scriptSB);
		scriptSB.append("\n);\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"SHOW CREATE VIEW " + catalog + "." + entry;
		List<String[]> rows = runQuery(query);
		if (rows.size() == 1) {
			return rows.get(0)[1];
		} else {
			return getSource(catalog, schema, entry);
		}
	}
	
}
