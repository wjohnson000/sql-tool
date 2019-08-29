package sqltool.schema.custom.postgres;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "View" data, which are the
 * views.
 * 
 * @author wjohnson000
 *
 */
public class ViewDefintionType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public ViewDefintionType() { }

	/**
	 * Get a name for this data type, which is "TABLE"
	 * @return display name
	 */
	public String getDataType() {
		return "VIEW DEF.";
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
			"  FROM information_schema.views " +
            catalogAndSchema("table_catalog", catalog, "table_schema", schema) +
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
			"SELECT view_definition " +
			"  FROM information_schema.views " +
            catalogAndSchema("table_catalog", catalog, "table_schema", schema) +
			"   AND table_name = '" + entry + "' ";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("VIEW DEFINITION " + qualifiedName(schema, entry) + " (\n");
		String viewDef = rows.get(0)[0];
		scriptSB.append(prettifySql(viewDef));
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
	
	private String prettifySql(String sql) {
	    StringBuilder buff = new StringBuilder();
	    int parenCnt = 0;
	    for (int i=0;  i<sql.length();  i++) {
	        char ch = sql.charAt(i);
	        if (ch == '(') {
	            parenCnt++;
	        } else if (ch == ')') {
	            parenCnt--;
	        }

	        buff.append(ch);
	        if (parenCnt == 0  &&  ch == ',') {
	            buff.append("\n");
	        }
	    }
	    return buff.toString();
	}
}
