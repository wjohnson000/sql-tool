package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Synonym" data, which are the
 * database synonyms.
 * 
 * @author wjohnson000
 *
 */
public class SynonymDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public SynonymDataType() { }

	/**
	 * Get a name for this data type, which is "SYNONYM"
	 * @return display name
	 */
	public String getDataType() {
		return "SYNONYM";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"Select synonym_name " +
			"  From sys.ALL_SYNONYMS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
//			"    Or upper(owner) = '" + PUBLIC_PUBLIC + "'" +
			" Order By synonym_name";

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
			"Select synonym_name, table_owner, table_name " +
			"  From sys.ALL_SYNONYMS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(synonym_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(256);
		scriptSB.append("CREATE SYNONYM " + qualifiedName(schema, entry) + " FOR ");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append(row[1]);
			scriptSB.append(".");
			scriptSB.append(row[2]);
			scriptSB.append(";\n");
		} else {
			scriptSB.append("Unknown ...");
		}

		return DISCLAIMER + scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		return getSource(catalog, schema, entry);
	}

	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getDrop(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getDrop(String catalog, String schema, String entry) {
		return DISCLAIMER + "DROP " + getDataType() + " " + qualifiedName(schema, entry) + ";";
	}

}
