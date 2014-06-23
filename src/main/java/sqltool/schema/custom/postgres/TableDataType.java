package sqltool.schema.custom.postgres;

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
			"SELECT table_name " +
			"  FROM information_schema.tables " +
			" WHERE table_catalog = '" + catalog + "' " +
            "   AND table_schema = '" + schema + "' " +
			"   AND table_type = 'BASE TABLE' " +
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
            " WHERE table_catalog = '" + catalog + "' " +
            "   AND table_schema = '" + schema + "' " +
			"   AND table_name = '" + entry + "' " +
			" ORDER BY ordinal_position ";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE TABLE " + qualifiedName(schema, entry) + " (\n");
		writeColumnDef(rows, scriptSB);

		// Add the PRIMARY KEY constraints, if any -- the results of the query
		// to get constraints will consist of the constraint name, table name,
		// constraint type and the fully-formed constraint definition
		boolean first = true;
		rows = getConstraints(catalog, schema, entry);
		for (String[] row : rows) {
		    if ("PRIMARY KEY".equals(row[2])) {
                scriptSB.append(",");
                if (first) {
                    first = false;
                    scriptSB.append("\n");
                }
		        scriptSB.append("\n   CONSTRAINT ").append(row[0]);
                scriptSB.append(" ").append(row[3]);
		    }
		}

        // Add the FOREIGN KEY, CHECK, UNIQUE constraints, if any
        for (String[] row : rows) {
            if (! "PRIMARY KEY".equals(row[2])) {
                scriptSB.append(",");
                if (first) {
                    first = false;
                    scriptSB.append("\n");
                }
                scriptSB.append("\n   CONSTRAINT ").append(row[0]);
                String constraintDef = row[3];
                if (constraintDef.contains("::character varying")) {
                    constraintDef = constraintDef.replaceAll("::text", "");
                    constraintDef = constraintDef.replaceAll("::character varying", "");
                    constraintDef = constraintDef.replaceAll("\\= ANY \\(ARRAY\\[", "IN (");
                    constraintDef = constraintDef.replaceAll("\\]\\[\\]", "");
                }
                scriptSB.append(" ").append(constraintDef);
            }
        }
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

	/**
	 * Return all constraints for a given catalog + schema + entry.  We're primarily
	 * interested in the PRIMARY KEY and FOREIGN KEY constraints.
	 * 
	 * @param catalog catalog name
	 * @param schema schema name
	 * @param entry entry [table] name
	 * @return rows of data with constraint name, type, table name, column name,
	 *         position, referenced schema, referenced table, referenced column.
	 */
	private List<String[]> getConstraints(String catalog, String schema, String entry) {
	    String query =
	        "SELECT tc.constraint_name, " +
	        "       tc.table_name, " +
	        "       tc.constraint_type, " +
	        "       pg_catalog.pg_get_constraintdef(r.oid, TRUE) AS condef " +
	        "  FROM information_schema.table_constraints AS tc " +
	        "  JOIN pg_catalog.pg_constraint AS r ON r.conname = tc.constraint_name " +
	        " WHERE tc.constraint_catalog = '" + catalog + "' " +
	        "   AND tc.constraint_schema = '" + schema + "' " +
	        "   AND tc.table_name = '" + entry + "' " +
	        " ORDER BY tc.constraint_name ASC ";
	    return runQuery(query);
	}
}
