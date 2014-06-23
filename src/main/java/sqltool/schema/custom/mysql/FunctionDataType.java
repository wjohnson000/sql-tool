package sqltool.schema.custom.mysql;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Function" data, which are the
 * stored functions
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
			"SHOW FUNCTION STATUS ";

		// Count how many functions are in the current catalog
		List<String[]> rows = runQuery(query);
		int count = 0;
		for (String[] row : rows) {
			if (row.length > 2  &&  row[0].equalsIgnoreCase(catalog)) {
				count++;
			}
		}

		// Second pass: add the names of the entries
		String[] entries = new String[count];
		count = 0;
		for (String[] row : rows) {
			if (row.length > 2  &&  row[0].equalsIgnoreCase(catalog)) {
				entries[count++] = row[1];
			}
		}

		return entries;
	}
	
	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getSource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getSource(String catalog, String schema, String entry) {
		String query =
			"SHOW FUNCTION CODE " + catalog + "." + entry;

		List<String[]> rows = runQuery(query);
		if (rows.size() == 0) {
			return getCreate(catalog, schema, entry);
		} else {
			StringBuffer scriptSB = new StringBuffer(1024);
			for (String[] row : rows) {
				scriptSB.append(row[0]).append("\n");
			}
			return scriptSB.toString();
		}
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"SHOW CREATE FUNCTION " + catalog + "." + entry;
		List<String[]> rows = runQuery(query);
		if (rows.size() == 1) {
			return rows.get(0)[2];
		} else {
			return getSource(catalog, schema, entry);
		}
	}
	
}
