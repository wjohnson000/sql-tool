package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "DbLink" data, which are links
 * to other Oracle (?) database instances
 * 
 * @author wjohnson000
 *
 */
public class DbLinkDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public DbLinkDataType() { }

	/**
	 * Get a name for this data type, which is "DB_LINK"
	 * @return display name
	 */
	public String getDataType() {
		return "DB_LINK";
	}


	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"Select db_link " +
			"  From sys.ALL_DB_LINKS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			" Order By db_link";

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
			"Select db_link, username, host " +
			"  From sys.ALL_DB_LINKS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(db_link)  = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(256);
		scriptSB.append("CREATE DATABASE LINK " + qualifiedName(schema, entry) + " \n");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append("  CONNECT TO " + row[2] + "\n");
			scriptSB.append("  USING '" + row[1] + "';\n");
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
}
