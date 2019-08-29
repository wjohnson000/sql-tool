package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "SEQUENCE" data, which are the
 * Sequences.
 * 
 * @author wjohnson000
 *
 */
public class SequenceDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public SequenceDataType() { }

	/**
	 * Get a name for this data type, which is "Sequence"
	 * @return display name
	 */
	public String getDataType() {
		return "SEQUENCE";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SELECT sequence_name " +
			"  FROM information_schema.sequences " +
			catalogAndSchema("sequence_catalog", catalog, "sequence_schema", schema) +
			" ORDER BY sequence_name ";

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
            "SELECT sequence_name, data_type, start_value, increment, maximum_value, cycle_option " +
            "  FROM information_schema.sequences " +
            catalogAndSchema("sequence_catalog", catalog, "sequence_schema", schema) +
            "   AND sequence_name = '" + entry + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("SEQUENCE ").append(qualifiedName(schema, entry)).append(":\n");
		if (rows.size() > 0) {
	        String[] row = rows.get(0);
	        scriptSB.append("   DataType: ").append(row[1]).append("\n");
            scriptSB.append("  StartValu: ").append(row[2]).append("\n");
            scriptSB.append("  Increment: ").append(row[3]).append("\n");
            scriptSB.append("   MaxValue: ").append(row[4]).append("\n");
            scriptSB.append("   CanCycle: ").append(row[5]);
		}
		scriptSB.append(";\n");

		return scriptSB.toString();
	}

    /* (non-Javadoc)
     * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getCreate(String catalog, String schema, String entry) {
        String res = super.getSource(catalog, schema, entry);
        if (res != null  &&  res.length() > 0) {
            return res;
        }

        String query =
            "SELECT sequence_name, start_value, increment, maximum_value " +
            "  FROM information_schema.sequences " +
            catalogAndSchema("sequence_catalog", catalog, "sequence_schema", schema) +
            "   AND sequence_name = '" + entry + "'";

        List<String[]> rows = runQuery(query);
        StringBuffer scriptSB = new StringBuffer(44 * rows.size());
        scriptSB.append("CREATE SEQUENCE ").append(qualifiedName(schema, entry));
        if (rows.size() > 0) {
            String[] row = rows.get(0);
            scriptSB.append(" INCREMENT BY ").append(row[2]);
            scriptSB.append(" START WITH ").append(row[1]);
            if (row[3] == null  ||  row[3].length() > 12) {
                scriptSB.append(" NO MAXVALUE");
            } else {
                scriptSB.append(" MAXVALUE ").append(row[3]);
            }
        }
        scriptSB.append(";\n");

        return scriptSB.toString();
    }
}
