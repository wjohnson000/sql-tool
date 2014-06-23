package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "View" data, which are custom
 * views, materialized or not.
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
	 * Get a name for this data type, which is "VIEW"
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
			"Select view_name " +
			"  From sys.ALL_VIEWS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			" Order By view_name";

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
			"Select column_name, data_type, data_length, " +
			"       data_precision, data_scale, nullable " +
			"  From sys.ALL_TAB_COLUMNS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(table_name) = '" + entry.toUpperCase() + "'" +
			" Order By column_id";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("Columns for view " + qualifiedName(schema, entry) + ":\n");
		writeColumnDef(rows, scriptSB);
		scriptSB.append("\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"Select text " +
			"  From sys.ALL_VIEWS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(view_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(1000);
		scriptSB.append("CREATE VIEW " + qualifiedName(schema, entry) + " AS\n");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append(row[0]);
		} else {
			scriptSB.append("Unknown ...");
		}
		scriptSB.append("\n)\n" + ";");

		return scriptSB.toString();
	}

}
