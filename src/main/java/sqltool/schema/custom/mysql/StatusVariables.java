package sqltool.schema.custom.mysql;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Status" view, which displays a
 * bunch of system variables
 * 
 * @author wjohnson000
 *
 */
public class StatusVariables extends BaseDataType {

	private static final String SYSTEM_STATUS_ENTRY = "** System **";

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public StatusVariables() { }

	/**
	 * Get a name for this data type, which is "STATUS"
	 * @return display name
	 */
	public String getDataType() {
		return "STATUS";
	}

	/**
	 * Disable the "Create" function
	 * 
	 * @return FALSE
	 */
	public boolean canCreate() {
		return false;
	}


	/**
	 * Disable the "Drop" function
	 * 
	 * @return FALSE
	 */
	public boolean canDrop() {
		return false;
	}


	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SHOW FULL TABLES " +
			" FROM " + catalog + 
			" WHERE TABLE_TYPE = 'BASE TABLE' ";

		// Add a special "** System **" entry
		List<String[]> rows = runQuery(query);
		String[] entries = new String[rows.size()+1];
		entries[0] = SYSTEM_STATUS_ENTRY;
		for (int i=0;  i<rows.size();  i++) {
			String[] row = rows.get(i);
			entries[i+1] = row[0];
		}
		return entries;

	}

	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getSource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getSource(String catalog, String schema, String entry) {
		if (SYSTEM_STATUS_ENTRY.equalsIgnoreCase(entry)) {
			return getSourceSystem(catalog, schema, entry);
		} else {
			return this.getSourceTable(catalog, schema, entry);
		}
	}

	/**
	 * Return the system status, which will be a bunch of variable name/value
	 * pairs.
	 * @param catalog catalog name
	 * @param schema schema name
	 * @param entry
	 * @return
	 */
	private String getSourceSystem(String catalog, String schema, String entry) {
		String query = "SHOW STATUS";

		int nameLen = 0;
		List<String[]> rows = runQuery(query);

		// Calculate the length of the longest variable
		for (String[] row : rows) {
			nameLen = Math.max(nameLen, row[0].length());
		}

		// Format the data
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		for (String[] row : rows) {
			scriptSB.append(row[0]);
			for (int i=row[0].length();  i<= nameLen;  i++) {
				scriptSB.append(' ');
			}
			scriptSB.append(" = ").append(row[1]).append('\n');
		}

		return scriptSB.toString();
	}

	/**
	 * Return the table status, which will be a bunch of variable name/value
	 * pairs.
	 * @param catalog catalog name
	 * @param schema schema name
	 * @param entry table name
	 * @return
	 */
	private String getSourceTable(String catalog, String schema, String entry) {
		String query =
			"SHOW TABLE STATUS FROM " + catalog + " LIKE '" + entry + "'";

		int nameLen = 0;
		List<String[]> rows = runQuery(query, true);

		// Calculate the length of the longest variable
		for (String colName : rows.get(0)) {
			nameLen = Math.max(nameLen, colName.length());
		}

		// Format the data
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		for (int i=0;  i<rows.get(0).length;  i++) {
			String key = rows.get(0)[i];
			String val = rows.get(1)[i];
			scriptSB.append(key);
			for (int j=key.length();  j<= nameLen;  j++) {
				scriptSB.append(' ');
			}
			scriptSB.append(" = ").append(val).append('\n');
		}

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		return "";
	}
	
}
