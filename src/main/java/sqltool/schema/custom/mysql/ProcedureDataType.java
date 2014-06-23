package sqltool.schema.custom.mysql;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Procedure" data, which are the
 * stored functions
 * 
 * @author wjohnson000
 *
 */
public class ProcedureDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================

	/**
	 * Default constructor
	 */
	public ProcedureDataType() { }

	/**
	 * Get a name for this data type, which is "PROCEDURE"
	 * @return display name
	 */
	public String getDataType() {
		return "PROCEDURE";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"SHOW PROCEDURE STATUS ";

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
			"SHOW PROCEDURE CODE " + catalog + "." + entry;

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
			"SHOW CREATE PROCEDURE " + catalog + "." + entry;
		List<String[]> rows = runQuery(query);
		if (rows.size() == 1) {
			return rows.get(0)[2];
		} else {
			return getSource(catalog, schema, entry);
		}
	}
	
}
