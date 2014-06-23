package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Table" data, which are the
 * tables.
 * 
 * @author wjohnson000
 *
 */
public class TableDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public TableDataType() { }

	/**
	 * Get a name for this data type, which is "TABLE"
	 * @return display name
	 */
	public String getDataType() {
		return "TABLE";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"Select table_name " +
			"  From sys.ALL_TABLES " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			" Order By table_name";

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
//			"Select column_name, data_type, data_length, " +
			"Select column_name, data_type, char_length as data_length, " +
			"       data_precision, data_scale, nullable " +
			"  From sys.ALL_TAB_COLUMNS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(table_name) = '" + entry.toUpperCase() + "'" +
			" Order By column_id";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE TABLE " + qualifiedName(schema, entry) + " (\n");
		writeColumnDef(rows, scriptSB);
		scriptSB.append("\n);\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		return getSource(catalog, schema, entry);
	}
	
}
