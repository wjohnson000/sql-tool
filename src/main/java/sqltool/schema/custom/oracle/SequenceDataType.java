package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Sequence" data, which are the
 * sequences.
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
	 * Get a name for this data type, which is "SEQUENCE"
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
			"Select sequence_name " +
			"  From sys.ALL_SEQUENCES " +
			" Where upper(sequence_owner) = '" + schema.toUpperCase() + "'" +
			" Order By sequence_name";

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
			"Select sequence_owner, sequence_name, min_value, max_value, " +
			"       increment_by, cycle_flag, order_flag, cache_size, last_number " +
			"  From sys.ALL_SEQUENCES " +
			" Where upper(sequence_owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(sequence_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(1024);
		scriptSB.append("Details of sequence: " + qualifiedName(schema, entry) + ":\n");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append("      Min value: " + row[2] + "\n");
			scriptSB.append("      Max value: " + row[3] + "\n");
			scriptSB.append("   Increment by: " + row[4] + "\n");
			scriptSB.append("     Cycle flag: " + row[5] + "\n");
			scriptSB.append("     Order flag: " + row[6] + "\n");
			scriptSB.append("     Cache size: " + row[7] + "\n");
			scriptSB.append("     Last value: " + row[8] + "\n");
		} else {
			scriptSB.append("Unknown ...");
		}

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"Select sequence_owner, sequence_name, min_value, max_value, " +
			"       increment_by, cycle_flag, order_flag, cache_size, last_number " +
			"  From sys.ALL_SEQUENCES " +
			" Where upper(sequence_owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(sequence_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(1024);
		scriptSB.append("CREATE SEQUENCE " + qualifiedName(schema, entry) + "\n");
		if (rows.size() == 1) {
			String[] row = rows.get(0);
			scriptSB.append("       MinValue " + row[2] + "\n");
			if (row[3] != null  &&  row[3].length() < 16) {
				scriptSB.append("       MaxValue " + row[3] + "\n");
			} else {
				scriptSB.append("     NoMaxValue\n");
			}
			scriptSB.append("     Start With " + row[2] + "\n");
			scriptSB.append("   Increment By " + row[4] + "\n");
			if ("Y".equals(row[5])) {
				scriptSB.append("          Cycle\n");
			} else {
				scriptSB.append("        NoCycle\n");
			}
			if ("Y".equals(row[6])) {
				scriptSB.append("          Order\n");
			} else {
				scriptSB.append("        NoOrder\n");
			}
			if ("0".equalsIgnoreCase(row[7])  ||  row[7] == null) {
				scriptSB.append("        NoCache;\n");
			} else {
				scriptSB.append("          Cache " + row[7] + ";\n");
			}
		} else {
			scriptSB.append("Unknown ...");
		}

		return DISCLAIMER + scriptSB.toString();
	}
}
