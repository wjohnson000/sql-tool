package sqltool.schema.custom.oracle;

import java.util.List;


/**
 * Extend {@link BaseDataType} to support "Package" data, which are packages
 * and package bodies.
 * 
 * @author wjohnson000
 *
 */
public class PackageDataType extends BaseDataType {

//	===========================================================================
//	static variable(s)  ...  constants
//	===========================================================================
	public static final String TYPE_PACKAGE      = "PACKAGE";
	public static final String TYPE_PACKAGE_BODY = "PACKAGE BODY";


//	===========================================================================
//	instance variable(s)  ...  "PACKAGE" or "PACKAGE BODY"
//	===========================================================================
	private String pkgType;


	/**
	 * Constructor takes the type, indicating whether we care about the
	 * package definition, or the package body itself
	 * @param type one of PackageDataType.TYPE_PACKAGE or PackageDataType.TYPE_PACKAGE_BODY
	 */
	public PackageDataType(String type) {
		if (type.equalsIgnoreCase(TYPE_PACKAGE_BODY)) {
			pkgType = TYPE_PACKAGE_BODY;
		} else {
			pkgType = TYPE_PACKAGE;
		}
	}

	/**
	 * Retrieve the type of object we are managing, package or package body
	 * @return type
	 */
	public String getDataType() {
		return pkgType;
	}


	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
			"Select object_name " +
			"  From sys.ALL_OBJECTS " +
			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
			"   And upper(object_type) = '" + getDataType() + "'" +
			" Order By object_name";

		List<String[]> rows = runQuery(query);
		String[] entries = new String[rows.size()];
		for (int i=0;  i<entries.length;  i++) {
			String[] row = rows.get(i);
			entries[i] = row[0];
		}
		return entries;
	}
	

	/**
	 * Retrieve the source for this table, initially consisting of the table definition
	 * only, w/out indexes, triggers or other constraints
	 */
//	public String getSource(String catalog, String schema, String entry) {
//		String res = super.getSource(catalog, schema, entry);
//		if (res != null  &&  res.length() > 0) {
//			return res;
//		}
//
//		String query =
//			"Select attr_name, attr_type_name, length, " +
//			"       precision, scale, 'Y' " +
//			"  From sys.ALL_TYPE_ATTRS " +
//			" Where upper(owner) = '" + schema.toUpperCase() + "'" +
//			"   And upper(type_name) = '" + entry.toUpperCase() + "'";
//
//		List rows = runQuery(query);
//		StringBuffer scriptSB = new StringBuffer(44 * rows.size());
//		scriptSB.append("CREATE TYPE " + qualifiedName(schema, entry) + " AS OBJECT (\n");
//		writeColumnDef(rows, scriptSB);
//		scriptSB.append("\n);\n");
//
//		return scriptSB.toString();
//	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
		String source = getSource(catalog, schema, entry);
		
		StringBuffer scriptSB = new StringBuffer(source.length() + 120);
		scriptSB.append(DISCLAIMER);
		scriptSB.append("CREATE or REPLACE " + getDataType() + " " + qualifiedName(schema, entry) + "\n");
		scriptSB.append("AS\n\n");
		scriptSB.append(source);

		return scriptSB.toString();
	}

	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getDrop(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getDrop(String catalog, String schema, String entry) {
		return DISCLAIMER + "DROP " + getDataType() + " " + qualifiedName(schema, entry) + ";";
	}

}
