package sqltool.common.db;

import java.sql.Connection;


/**
 * Contract for interacting with any sort of custom data type in a database.
 * 
 * @author wjohnson000
 *
 */
public interface DbRawDataType {

	/**
	 * @return A "String" representation/label for this data element
	 */
	public String   getDataType();

	/**
	 * @return Whether or not a new instance of this type of element can
	 *         be created
	 */
	public boolean  canCreate();

	/**
	 * @return Whether or not an existing instance of this type of element
	 *         can be deleted
	 */
	public boolean  canDrop();

	/**
	 * Return a list of entries of this type in the current database
	 * @param catalog active catalog/database
	 * @param schema active schema
	 * @return list of entries
	 */
	public String[] getEntries(String catalog, String schema);

	/**
	 * Return the source definition for a specific entry
	 * @param catalog active catalog/database
	 * @param schema active schema
	 * @param entry specific entry
	 * @return source definition
	 */
	public String   getSource(String catalog, String schema, String entry);

	/**
	 * Return a string that represents the SQL to create a new entry
	 * @param catalog active catalog/database
	 * @param schema active schema
	 * @param entry specific entry
	 * @return create SQL
	 */
	public String   getCreate(String catalog, String schema, String entry);

	/**
	 * Return a string that represents the SQL to drop an existing entry
	 * @param catalog active catalog/database
	 * @param schema active schema
	 * @param entry specific entry
	 * @return drop SQL
	 */
	public String   getDrop(String catalog, String schema, String entry);

	/**
	 * Set the database connection
	 * @param conn database connection
	 */
	public void     setConnection(Connection conn);
}
