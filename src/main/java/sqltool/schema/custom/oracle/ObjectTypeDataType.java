package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Object Type" data, which are custom
 * data types.
 * 
 * @author wjohnson000
 *
 */
public class ObjectTypeDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  none
//	===========================================================================


	/**
	 * Default constructor
	 */
	public ObjectTypeDataType() { }

	/**
	 * Get a name for this data type, which is "OBJECT TYPE"
	 * @return display name
	 */
	public String getDataType() {
		return "OBJECT TYPE";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"Select type_name " +
			"  From sys.ALL_TYPES " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(typecode) = 'OBJECT'" +
			" Order By type_name";

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
		String res = super.getSource(catalog, schema, entry, "TYPE");
		if (res != null  &&  res.length() > 0) {
			return res;
		}

		String query =
			"Select attr_name, attr_type_name, length, " +
			"       precision, scale, 'Y' " +
			"  From sys.ALL_TYPE_ATTRS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(type_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE TYPE " + qualifiedName(schema, entry) + " AS OBJECT (\n");
		writeColumnDef(rows, scriptSB);
		scriptSB.append("\n);\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String query =
			"Select attr_name, attr_type_name, length, " +
			"       precision, scale, 'Y' " +
			"  From sys.ALL_TYPE_ATTRS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(type_name) = '" + entry.toUpperCase() + "'";

		List<String[]> rows = runQuery(query);
		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
		scriptSB.append("CREATE TYPE " + qualifiedName(schema, entry) + " AS OBJECT (\n");
		writeColumnDef(rows, scriptSB);
		scriptSB.append("\n);\n");

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getDrop(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getDrop(String catalog, String schema, String entry) {
		return DISCLAIMER + "DROP TYPE " + qualifiedName(schema, entry) + ";";
	}

}
