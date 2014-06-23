package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Function" data, which are the
 * functions.
 * 
 * @author wjohnson000
 *
 */
public class FunctionDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public FunctionDataType() { }

	/**
	 * Get a name for this data type, which is "FUNCTION"
	 * @return display name
	 */
	public String getDataType() {
		return "FUNCTION";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SELECT routine_name " +
			"  FROM information_schema.routines " +
			" WHERE routine_catalog = '" + catalog + "' " +
            "   AND routine_schema = '" + schema + "' " +
			" ORDER BY routine_name ";

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
            "SELECT routine_name, data_type, routine_definition, external_language " +
            "  FROM information_schema.routines " +
            " WHERE routine_catalog = '" + catalog + "' " +
            "   AND routine_schema = '" + schema + "' " +
            "   AND routine_name = '" + entry + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE OR REPLACE FUNCTION ").append(qualifiedName(schema, entry)).append("()");
		if (rows.size() > 0) {
	        String[] row = rows.get(0);
	        scriptSB.append(" RETURNS ").append(row[1]);
	        scriptSB.append(" AS \n$BODY$");
	        scriptSB.append(row[2]);
	        scriptSB.append("$BODY$\nLANGUAGE ").append(row[3]).append(";");
		}
		scriptSB.append("\n");

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
