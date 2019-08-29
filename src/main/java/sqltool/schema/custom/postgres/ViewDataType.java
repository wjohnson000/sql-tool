package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "View" data, which are the
 * views.
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
	 * Get a name for this data type, which is "TABLE"
	 * @return display name
	 */
	public String getDataType() {
		return "VIEW";
	}

    /**
     * Don't allow the creation of a VIEW via this mechanism
     * 
     * @return FALSE
     */
	@Override
    public boolean canCreate() {
        return false;
    }

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SELECT table_name " +
			"  FROM information_schema.tables " +
			catalogAndSchema("table_catalog", catalog, "table_schema", schema) +
			"   AND table_type = 'VIEW' " +
			" ORDER BY table_name ";

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
			"SELECT ordinal_position, column_name, data_type, udt_name," +
			"       is_nullable, character_maximum_length, numeric_precision " +
			"  FROM information_schema.columns " +
            catalogAndSchema("table_catalog", catalog, "table_schema", schema) +
			"   AND table_name = '" + entry + "' " +
			" ORDER BY ordinal_position ";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("VIEW COLUMNS " + qualifiedName(schema, entry) + " (\n");
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
