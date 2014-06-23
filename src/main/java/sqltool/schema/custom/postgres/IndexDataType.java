package sqltool.schema.custom.postgres;

import java.util.TreeMap;
import java.util.List;
import java.util.Map;


/**
 * Extend {@link BaseDataType} to support "Index" data, which are the
 * indexes.
 * 
 * @author wjohnson000
 *
 */
public class IndexDataType extends BaseDataType {

//	===========================================================================
//	instance variable(s)  ...  cache the results
//	===========================================================================

    Map<String,Map<String,String>> index2Table = new TreeMap<String,Map<String,String>>();
    Map<String,Map<String,String>> index2Def   = new TreeMap<String,Map<String,String>>();

	/**
	 * Default constructor
	 */
	public IndexDataType() { }

	/**
	 * Get a name for this data type, which is "TABLE"
	 * @return display name
	 */
	public String getDataType() {
		return "INDEX";
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getEntries(java.lang.String, java.lang.String)
	 */
	@Override
	public String[] getEntries(String catalog, String schema) {
	    if (index2Table.size() == 0) {
	        this.retrieveIndexData();
	    }

	    Map<String,String> tMap = index2Table.get(schema);
	    if (tMap == null) {
	        return new String[] { };
	    } else {
	        return tMap.keySet().toArray(new String[0]);
	    }
	}
	
	/* (non-Javadoc)
	 * @see sqltool.schema.custom.oracle.BaseDataType#getSource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getSource(String catalog, String schema, String entry) {
	    if (index2Table.size() == 0) {
	        this.retrieveIndexData();
	    }

	    Map<String,String> tMap = index2Def.get(schema);
	    if (tMap == null) {
	        return "";
	    } else {
	        return (! tMap.containsKey(entry)) ? "" : tMap.get(entry);
	    }
	}

	/* (non-Javadoc)
	 * @see sqltool.common.db.DbRawDataType#getCreate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getCreate(String catalog, String schema, String entry) {
        return getSource(catalog, schema, entry);
	}

	private void retrieveIndexData() {
	    String query =
	        "SELECT n.nspname AS schema, " +
	        "       t.relname AS table, " +
	        "       c.relname AS index, " +
	        "       pg_get_indexdef(indexrelid) as def " +
	        "  FROM pg_catalog.pg_class c " +
	        "       JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
	        "       JOIN pg_catalog.pg_index i ON i.indexrelid = c.oid " +
	        "       JOIN pg_catalog.pg_class t ON i.indrelid   = t.oid " +
	        " WHERE c.relkind = 'i' ";

	    List<String[]> rows = runQuery(query);
	    for (String[] row : rows) {
	        Map<String,String> tMap = index2Table.get(row[0]);
	        if (tMap == null) {
	            tMap = new TreeMap<String,String>();
	            index2Table.put(row[0], tMap);
	        }
	        tMap.put(row[2], row[1]);

	        tMap = index2Def.get(row[0]);
	        if (tMap == null) {
	            tMap = new TreeMap<String,String>();
	            index2Def.put(row[0], tMap);
	        }
	        tMap.put(row[2], row[3]);
	    }
	}
}
