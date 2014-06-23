package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Trigger" data, which are triggers
 * that get fired when specific conditions are met.
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
		String query =
			"Select trigger_name " +
			"  From sys.ALL_TRIGGERS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			" Order By trigger_name";

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
			"Select column_name, column_usage " +
			"  From sys.ALL_TRIGGER_COLS " +
			" Where upper(trigger_owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(trigger_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("Columns for trigger " + qualifiedName(schema, entry) + ":\n");
		for (int i=0;  i<rows.size();  i++) {
			String[] row = rows.get(i);
			scriptSB.append("   ");
			scriptSB.append(row[0]);
			scriptSB.append("  [" + row[1] + "]\n");
		}
		scriptSB.append("\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"Select description, trigger_body " +
			"  From sys.ALL_TRIGGERS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(trigger_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(1000);
		scriptSB.append("CREATE TRIGGER \n");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append(row[0]);
			scriptSB.append("\n");
			scriptSB.append(row[1]);
		} else {
			scriptSB.append("Unknown ...");
		}
		scriptSB.append("\n;");

		return scriptSB.toString();
	}

}
