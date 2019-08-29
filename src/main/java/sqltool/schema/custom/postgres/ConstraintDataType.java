package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Function" data, which are the
 * functions.
 * 
 * @author wjohnson000
 *
 */
public class ConstraintDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public ConstraintDataType() { }

	/**
	 * Get a name for this data type, which is "FUNCTION"
	 * @return display name
	 */
	public String getDataType() {
		return "CONSTRAINT";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
            "SELECT tc.constraint_name, " +
            "       tc.table_name, " +
            "       tc.constraint_type, " +
            "       pg_catalog.pg_get_constraintdef(r.oid, TRUE) AS condef " +
            "  FROM information_schema.table_constraints AS tc " +
            "  JOIN pg_catalog.pg_constraint AS r ON r.conname = tc.constraint_name " +
            catalogAndSchema("tc.constraint_catalog", catalog, "tc.constraint_schema", schema) +
            " ORDER BY tc.constraint_name ASC ";

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
            "SELECT tc.constraint_name, " +
            "       tc.table_name, " +
            "       tc.constraint_type, " +
            "       pg_catalog.pg_get_constraintdef(r.oid, TRUE) AS condef " +
            "  FROM information_schema.table_constraints AS tc " +
            "  JOIN pg_catalog.pg_constraint AS r ON r.conname = tc.constraint_name " +
            catalogAndSchema("tc.constraint_catalog", catalog, "tc.constraint_schema", schema) +
            "   AND tc.constraint_name = '" + entry + "' " +
            " ORDER BY tc.constraint_name ASC ";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());

		if (rows.size() > 0) {
	        String[] row = rows.get(0);
            scriptSB.append("ALTER TABLE ").append(row[1]).append(" ADD CONSTRAINT ").append(row[0]);
	        String constraintDef = row[3];
	        if (constraintDef.contains("::character varying")) {
	            constraintDef = constraintDef.replaceAll("::text", "");
	            constraintDef = constraintDef.replaceAll("::character varying", "");
	            constraintDef = constraintDef.replaceAll("\\= ANY \\(ARRAY\\[", "IN (");
	            constraintDef = constraintDef.replaceAll("\\]\\[\\]", "");
	        }
	        scriptSB.append(" " + constraintDef);
		}
		scriptSB.append(";");

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
