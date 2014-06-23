package sqltool.common.db;

import java.sql.Connection;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 * Manage a bunch of custom data for a specific database connection
 * @author wjohnson000
 *
 */
public class DbCustomModel {
	/** Database connection */
	Connection conn;

	/** All custom elements supported by a specific database */
	String[] dataTypeName;

	/** */
	Map<String,DbRawDataType> registeredDataTypes;

	/** Cache any sort of stuff that needs to be cached */
	Map<String,Object> objectCache;
	

	/**
	 * Constructor takes a JDBC connection
	 * @param conn
	 */
	public DbCustomModel(Connection conn) {
		registeredDataTypes = new LinkedHashMap<String,DbRawDataType>();
		dataTypeName = new String[0];
		resetCache();
		this.conn = conn;
	}

	/**
	 * Clean the cache, forcing us to re-read everything from the database
	 */
	public void resetCache() {
		objectCache = new LinkedHashMap<String,Object>();
	}

	/**
	 * Return a list of the "dataType" names that are available from this model
	 * @return
	 */
	public String[] getDataTypeNames() {
		return dataTypeName;
	}

	/**
	 * Add another "RawDataType" generator to the list of things we can do, and
	 * re-generate the list of dataType names available ...
	 * @param dataType
	 */
	public void registerDataType(DbRawDataType dataType) {
		registeredDataTypes.put(dataType.getDataType(), dataType);
		dataType.setConnection(conn);
		dataTypeName = (String[])registeredDataTypes.keySet().toArray(new String[registeredDataTypes.size()]);
	}

	/**
	 * Determine if we can get the "CREATE" sql for a given dataType
	 */
	public boolean canCreate(String datatypeName) {
		DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
		return (rdt == null) ? false : rdt.canCreate();
	}

	/**
	 * Determine if we can get the "DROP" sql for a given dataType
	 */
	public boolean canDrop(String datatypeName) {
		DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
		return (rdt == null) ? false : rdt.canDrop();
	}

	/**
	 * Get a list of all entries (names) given a catalog (or schema) and dataType
	 */
	public String[] getEntries(String catalog, String schema, String datatypeName) {
		String key = getKey(catalog, schema, datatypeName, "all", "list");
		String[] res = (String[])objectCache.get(key);
		if (res == null) {
			DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
			res = rdt.getEntries(catalog, schema);
			objectCache.put(key, res);
		}
		return res;
	}

	/**
	 * Get the SOURCE for an entry given a catalog (or schema) and dataType
	 */
	public String getSource(String catalog, String schema, String datatypeName, String entry) {
		String key = getKey(catalog, schema, datatypeName, entry, "source");
		String res = (String)objectCache.get(key);
		if (res == null) {
			DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
			res = rdt.getSource(catalog, schema, entry);
			objectCache.put(key, res);
		}
		return res;
	}

	/**
	 * Get the CREATE sql for an entry given a catalog (or schema) and dataType
	 */
	public String getCreate(String catalog, String schema, String datatypeName, String entry) {
		String res = "";
		if (canCreate(datatypeName)) {
			String key = getKey(catalog, schema, datatypeName, entry, "create");
			res = (String)objectCache.get(key);
			if (res == null) {
				DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
				res = rdt.getCreate(catalog, schema, entry);
				objectCache.put(key, res);
			}
		}
		return res;
	}

	/**
	 * Get the DROP sql for an entry given a catalog (or schema) and dataType
	 */
	public String getDrop(String catalog, String schema, String datatypeName, String entry) {
		String res = "";
		if (canCreate(datatypeName)) {
			String key = getKey(catalog, schema, datatypeName, entry, "drop");
			res = (String)objectCache.get(key);
			if (res == null) {
				DbRawDataType rdt = (DbRawDataType)registeredDataTypes.get(datatypeName);
				res = rdt.getDrop(catalog, schema, entry);
				objectCache.put(key, res);
			}
		}
		return res;
	}

	/**
	 * Generate a key that can be used to get objects from the cache ...
	 */
	private String getKey(String catalog, String schema, String datatypeName, String entryName, String action) {
		return catalog + "." + schema + "." + datatypeName + "." + entryName + "." + action;
	}
}

