package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Trigger" data, which are the
 * triggers.
 * 
 * @author wjohnson000
 *
 */
public class TriggerDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public TriggerDataType() { }

	/**
	 * Get a name for this data type, which is "TRIGGER"
	 * @return display name
	 */
	public String getDataType() {
		return "TRIGGER";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SELECT trigger_name " +
			"  FROM information_schema.triggers " +
			" WHERE trigger_catalog = '" + catalog + "' " +
            "   AND trigger_schema = '" + schema + "' " +
			" ORDER BY trigger_name ";

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
            "SELECT trigger_name, event_manipulation, event_object_schema, event_object_table," +
            "       action_statement, action_orientation, action_timing " +
            "  FROM information_schema.triggers " +
            " WHERE trigger_catalog = '" + catalog + "' " +
            "   AND trigger_schema = '" + schema + "' " +
            "   AND trigger_name = '" + entry + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE TRIGGER ").append(qualifiedName(schema, entry)).append(" ");
		if (rows.size() > 0) {
	        String[] row = rows.get(0);
	        scriptSB.append(row[6]);
	        scriptSB.append(" ").append(row[1]);
	        scriptSB.append(" ON ").append(qualifiedName(row[2], row[3]));
	        scriptSB.append(" FOR EACH ").append(row[5]);
	        scriptSB.append(" ").append(row[4]);
		}
		scriptSB.append(";\n");

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
