package sqltool.schema.custom.mysql;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Trigger" view, which displays a
 * the database triggers
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
		String query = "SHOW TRIGGERS " + " FROM " + catalog;

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
		String query =
			"SHOW TRIGGERS " + " FROM " + catalog + " LIKE '" + entry + "'";

		int nameLen = 0;
		List<String[]> rows = runQuery(query, true);

		// Calculate the length of the longest column name
		for (String colName : rows.get(0)) {
			nameLen = Math.max(nameLen, colName.length());
		}

		// Format the data
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		for (int i=0;  i<rows.get(0).length;  i++) {
			String key = rows.get(0)[i];
			scriptSB.append(key);
			for (int j=key.length();  j<= nameLen;  j++) {
				scriptSB.append(' ');
			}
			scriptSB.append(" = ");

			if (rows.size() > 1) {
				String val = rows.get(1)[i];
				scriptSB.append(val);
			}

			scriptSB.append('\n');
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
