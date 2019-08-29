package sqltool.common.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sqltool.common.SqlToolkit;


/**
 * Information specific to an individual schema, including table, view,
 * synonym and procedure information
 */
class SchemaStructure {
	DbStructure tableStruct   = null;
	DbStructure viewStruct    = null;
	DbStructure synonymStruct = null;
	DbStructure procedureStruct = null;
}


/**
 * Cache all of the information for specific Database objects:  a table,
 * view, synonym or procedure.  DatabaseMetaData is used to extract the
 * actual information.
 * 
 * @author wjohnson000
 *
 */
public class DbInfoModel {

	// TODO: change these to "enum" type
	public static final String MODE_TABLE     = "TABLE";
	public static final String MODE_VIEW      = "VIEW";
	public static final String MODE_SYNONYM   = "SYNONYM";
	public static final String MODE_PROCEDURE = "PROCEDURE";

	/** Empty lists that are "place-holders" for results of failed operations */
	public static final List<Map<String,String>> INVALID_PRIMARY_KEY = new ArrayList<Map<String,String>>();
	public static final List<Map<String,String>> INVALID_INDEX       = new ArrayList<Map<String,String>>();
	public static final List<Map<String,String>> INVALID_PRIVILEGE   = new ArrayList<Map<String,String>>();
	
	private Connection conn;
	
	private boolean  isValid;
	private String   dbName;
	private String   dbVersion;
	private String   dbDriver;
	private String   dbURL;
	private String   dbUser;
	private String   catalogTerm = "catalog";
	private String   schemaTerm = "schema";
	
	private String[] keywordList;
	private String[] numericFuncList;
	private String[] stringFuncList;
	private String[] systemFuncList;
	private String[] dateTimeFuncList;
	private String[] schemaList;
	private String[] catalogList;

	private Map<String,List<Map<String,String>>> schemaDetails;
	private Map<String,SchemaStructure> schemaStructure;
	private DbCustomModel customModel;
	
	
	// Simple GET-ter methods for each piece of data being managed
	public boolean  isValid()             { return isValid; }
	public String   getDbName()           { return dbName; }
	public String   getDbVersion()        { return dbVersion; }
	public String   getDriverName()       { return dbDriver; }
	public String   getDbURL()            { return dbURL; }
	public String   getDbUser()           { return dbUser; }
	public String   getCatalogTerm()      { return catalogTerm; }
	public String   getSchemaTerm()       { return schemaTerm; }
	public String[] getKeywordList()      { return keywordList; }
	public String[] getNumericFuncList()  { return numericFuncList; }
	public String[] getStringFuncList()   { return stringFuncList; }
	public String[] getSystemFuncList()   { return systemFuncList; }
	public String[] getDateTimeFuncList() { return dateTimeFuncList; }
	public String[] getSchemaList()       { return schemaList; }
	public String[] getCatalogList()      { return catalogList; }
	public Connection getConnection()     { return conn; }
	public DbCustomModel getCustomModel() { return customModel; }


	public DbInfoModel(Connection conn) {
		refreshData(conn);
	}
	
	/**
	 * If we have reason to believe the database structure has changed underneath us,
	 * this allows us to force a refresh of the data
	 */
	public void refreshData(Connection conn) {
		SqlToolkit.appLogger.logDebug("DbInfoModel.refreshData: " + conn);
		isValid     = false;
		this.conn   = conn;
		dbName      = "";
		dbVersion   = "";
		dbDriver    = "";
		dbURL       = "";
		dbUser      = "";
		keywordList      = new String[0];
		numericFuncList  = new String[0];
		stringFuncList   = new String[0];
		systemFuncList   = new String[0];
		dateTimeFuncList = new String[0];
		schemaList       = new String[0];
		catalogList      = new String[0];
		schemaDetails    = new TreeMap<String,List<Map<String,String>>>();
		schemaStructure  = new TreeMap<String,SchemaStructure>();
		customModel      = null;
		
		if (conn != null) {
			retrieveDbIdentity();
			if (isValid) {
				retrieveStringLists();
				retrieveSchemaList();
				retrieveCatalogList();
				if (schemaList.length == 1  &&  schemaList[0].trim().isEmpty()) {
				    schemaList = catalogList;
				}
				customModel = CustomModelFactory.getCustomModel(conn);
			}
		}
	}
	

	
	
	/**
	 * Retrieve data based on the schema name and key (TABLE, VIEW or SYNONYM);
	 * we rely on lazy-instantiation to ensure that we only get what we need
	 */
	public DbStructure getDbStructure(String catalog, String schema, String mode) {
		if (! isValid) {
			return null;
		}
		String key = "key." + catalog + "." + schema;
		SqlToolkit.appLogger.logDebug("   DbInfoModel, structure for: " + key + "." + mode);
		
		SchemaStructure schemaStruct = schemaStructure.get(key);
		if (schemaStruct == null) {
			schemaStruct = new SchemaStructure();
			schemaStructure.put(key, schemaStruct);
		}
		
		DbStructure dbStruct = null;
		if (MODE_TABLE.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.tableStruct;
			if (dbStruct == null) {
				dbStruct = new DbTableSet(conn, catalog, schema, mode);
				schemaStruct.tableStruct = dbStruct;
			}
		} else if (MODE_VIEW.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.viewStruct;
			if (dbStruct == null) {
				dbStruct = new DbTableSet(conn, catalog, schema, mode);
				schemaStruct.viewStruct = dbStruct;
			}
		} else if (MODE_SYNONYM.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.synonymStruct;
			if (dbStruct == null) {
				dbStruct = new DbTableSet(conn, catalog, schema, mode);
				schemaStruct.synonymStruct = dbStruct;
			}
		} else if (MODE_PROCEDURE.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.procedureStruct;
			if (dbStruct == null) {
				dbStruct = new DbProcedureSet(conn, catalog, schema);
				schemaStruct.procedureStruct = dbStruct;
			}
		}
		
		return dbStruct;
	}
	
	
	/**
	 * Populate data based on the schema name and key (TABLE, VIEW or SYNONYM)
	 * and an entry name (i.e., table name); this "lazy instantiation" assures
	 * us that we don't retrieve the details until we need them ...
	 */
	public void populateDetails(String catalog, String schema, String mode, String entry) {
		if (! isValid) {
			return;
		}
		String key = "key." + catalog + "." + schema;
		SqlToolkit.appLogger.logDebug("   DbInfoModel, details for: " + key + "." + mode);

		SchemaStructure schemaStruct = schemaStructure.get(key);
		if (schemaStruct == null) {
			return;
		}
		
		DbStructure dbStruct = null;
		if (MODE_TABLE.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.tableStruct;
		} else if (MODE_VIEW.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.viewStruct;
		} else if (MODE_SYNONYM.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.synonymStruct;
		} else if (MODE_PROCEDURE.equalsIgnoreCase(mode)) {
			dbStruct = schemaStruct.procedureStruct;
		}
		
		if (dbStruct != null) {
			dbStruct.populateEntry(conn, catalog, schema, entry);
		}
	}
	
	
	/**
	 * Retrieve a list of primary keys for a given catalog, schema, table
	 * @param rset
	 * @return
	 */
	public List<Map<String,String>> getPrimaryKeys(String catalog, String schema, String entry) {
		String key = "key." + catalog + "." + schema + "." + entry + ".pk";
		SqlToolkit.appLogger.logDebug("   DbInfoModel, primary-key for: " + key);

		List<Map<String,String>> res = schemaDetails.get(key);
		if (res == null) {
			ResultSet rset = null;
			try {
				DatabaseMetaData dbmd = conn.getMetaData();
				rset = dbmd.getPrimaryKeys(catalog, schema, entry);
				res = extractResults(rset);
			} catch (SQLException sqlex) {
				res = INVALID_PRIMARY_KEY;
			} finally {
				try { if (rset != null) rset.close(); } catch (Exception ex) { }
			}
			schemaDetails.put(key, res);
		}
		return res;
	}
	
	
	/**
	 * Retrieve a list of index definitions for a given catalog, schema, table/view
	 * @param rset
	 * @return
	 */
	public List<Map<String,String>> getIndexInfo(String catalog, String schema, String entry) {
		String key = "key." + catalog + "." + schema + "." + entry + ".index";
		SqlToolkit.appLogger.logDebug("   DbInfoModel, index for: " + key);

		List<Map<String,String>> res = schemaDetails.get(key);
		if (res == null) {
			ResultSet rset = null;
			try {
				DatabaseMetaData dbmd = conn.getMetaData();
				rset = dbmd.getIndexInfo(catalog, schema, entry, false, true);
				res = extractResults(rset);
			} catch (SQLException sqlex) {
				res = INVALID_INDEX;
			} finally {
				try { if (rset != null) rset.close(); } catch (Exception ex) { }
			}
			schemaDetails.put(key, res);
		}
		return res;
	}
	
	
	/**
	 * Retrieve a list of privileges for a given catalog, schema, table/view
	 * @param rset
	 * @return
	 */
	public List<Map<String,String>> getTablePrivilege(String catalog, String schema, String entry) {
		String key = "key." + catalog + "." + schema + "." + entry + ".priv";
		SqlToolkit.appLogger.logDebug("   DbInfoModel, privilege for: " + key);

		List<Map<String,String>> res = schemaDetails.get(key);
		if (res == null) {
			ResultSet rset = null;
			try {
				DatabaseMetaData dbmd = conn.getMetaData();
				rset = dbmd.getTablePrivileges(catalog, schema, entry);
				res = extractResults(rset);
			} catch (SQLException sqlex) {
				res = INVALID_PRIVILEGE;
			} finally {
				try { if (rset != null) rset.close(); } catch (Exception ex) { }
			}
			schemaDetails.put(key, res);
		}
		return res;
	}

	/**
	 * Retrieve flag indicating whether or not we have a "Custom" model
	 */
	public boolean isCustomModelDefined() {
		return (customModel != null);
	}

	/**
	 * Retrieve basic identify information about the database ...
	 */
	private void retrieveDbIdentity() {
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			dbName    = dbmd.getDatabaseProductName();
			dbVersion = dbmd.getDatabaseProductVersion();
			dbDriver  = dbmd.getDriverName() + " [" + dbmd.getDriverVersion() + "]";
			dbURL     = dbmd.getURL();
			dbUser    = dbmd.getUserName();
			catalogTerm = dbmd.getCatalogTerm();
			schemaTerm  = dbmd.getSchemaTerm();
			isValid   = true;
			if (catalogTerm == null  ||  catalogTerm.trim().length() == 0) {
				catalogTerm = "catalog";
			}
			if (schemaTerm == null  ||  schemaTerm.trim().length() == 0) {
				schemaTerm = "schema";
			}
			SqlToolkit.appLogger.logDebug("   DbInfoModel, dbName: " + dbName);
			SqlToolkit.appLogger.logDebug("   DbInfoModel, dbURL:  " + dbURL);
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   DbInfoModel.retrieveDbIdentity.EX: " + sqlex);
		}
	}
	
	
	/**
	 * Set all of the lists (function names, keywords, etc) that are defined in terms
	 * of a comma-separated list of values
	 */
	private void retrieveStringLists() {
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			keywordList = setListValues(dbmd.getSQLKeywords());
			numericFuncList = setListValues(dbmd.getNumericFunctions());
			stringFuncList = setListValues(dbmd.getStringFunctions());
			systemFuncList = setListValues(dbmd.getSystemFunctions());
			dateTimeFuncList = setListValues(dbmd.getTimeDateFunctions());
			SqlToolkit.appLogger.logDebug("   DbInfoModel, string lists retrieved ...");
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   DbInfoModel.retrieveStringLists.EX: " + sqlex);
		}
	}
	
	/**
	 * Split the incoming string into values based on a comma-separated input
	 */
	private String[] setListValues(String value) {
		return (value == null) ? new String[] {"<none>"} : value.replaceAll(" ", "").split(",");
	}
	
	/**
	 * Get a list of the schema names
	 */
	private void retrieveSchemaList() {
		ResultSet rset = null;
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			rset = dbmd.getSchemas();
			List<String> alTemp = new ArrayList<String>(10);
			while (rset.next()) {
				alTemp.add(rset.getString(1));
			}
			schemaList = alTemp.toArray(new String[alTemp.size()]);
			SqlToolkit.appLogger.logDebug("   DbInfoModel, schema list: " + schemaList.length);
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   DbInfoModel.retrieveSchemaList.EX: " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (Exception ex) { }
		}
	}
	
	
	/**
	 * Get a list of the catalog names
	 */
	private void retrieveCatalogList() {
		ResultSet rset = null;
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			rset = dbmd.getCatalogs();
			List<String> alTemp = new ArrayList<String>(10);
			while (rset.next()) {
				alTemp.add(rset.getString(1));
			}
			catalogList = alTemp.toArray(new String[alTemp.size()]);
			SqlToolkit.appLogger.logDebug("   DbInfoModel, catalog list: " + catalogList.length);
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   DbInfoModel.retrieveCatalogList.EX: " + sqlex);
		} finally {
			try { if (rset != null) rset.close(); } catch (Exception ex) { }
		}
	}
	
	/**
	 * Retrieve the details of some on-demand request, given the results
	 * of the DataBaseMetaData query
	 * 
	 * @param rset The "ResultSet" returned from the query
	 * @return a List of "Map" instances that contain all of the
	 * meta data
	 */
	private List<Map<String,String>> extractResults(ResultSet rset) {
		List<Map<String,String>> detailList = new ArrayList<Map<String,String>>(10);
		try {
			ResultSetMetaData rsmd = rset.getMetaData();
			String[] colName = new String[rsmd.getColumnCount()];
			for (int i=0;  i<colName.length;  i++) {
				colName[i] = rsmd.getColumnName(i+1);
			}
			
			// Get all column data for this entry
			while (rset.next()) {
				HashMap<String,String> hmTemp = new HashMap<String,String>();
				for (int i=0;  i<colName.length;  i++) {
					String key = colName[i];
					String val = rset.getString(key);
					hmTemp.put(key, val);
				}
				detailList.add(hmTemp);
			}
		} catch (SQLException sqlex) {
			System.out.println("DbInfoModel.I.EX: " + sqlex);
		}
		return detailList;
	}
}
