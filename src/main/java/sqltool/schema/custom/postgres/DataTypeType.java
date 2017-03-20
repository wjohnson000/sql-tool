package sqltool.schema.custom.postgres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extend {@link BaseDataType} to support "DataType" data, which are built-in and custom types.
 * 
 * @author wjohnson000
 *
 */
public class DataTypeType extends BaseDataType {

    private static class TypeData {
        String name;
        String namespace;
        String internalName;
        String size;
        String description;

        @Override
        public String toString() {
            StringBuilder buff = new StringBuilder();

            buff.append("Name = ").append(name).append('\n');
            buff.append("NSpc = ").append(namespace).append('\n');
            buff.append("Intn = ").append(internalName).append('\n');
            buff.append("Size = ").append(size).append('\n');
            buff.append("Desc = ").append(description).append('\n');

            return buff.toString();
        }
    }

    Map<String,TypeData> typeDetails = new HashMap<>();


	public DataTypeType() { }

	public String getDataType() {
		return "DATA-TYPE";
	}

	@Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDrop() {
        return false;
    }

	@Override
	public String[] getEntries(String catalog, String schema) {
		String query =
		    "SELECT  pg_catalog.format_type(typ.oid, NULL) AS name, " +
		    "        nam.nspname, " +
            "        typ.typname AS internal_name, " +
            "        CASE " +
            "            WHEN typ.typrelid != 0 THEN " +
            "                 CAST ('tuple' AS pg_catalog.text) " +
            "            WHEN typ.typlen < 0 THEN " +
            "                 CAST ('var' AS pg_catalog.text) " +
            "            ELSE " +
            "                 CAST (typ.typlen AS pg_catalog.text) " +
            "        END AS size, " +
            "        pg_catalog.obj_description (typ.oid, 'pg_type') AS description " +
            "    FROM pg_catalog.pg_type typ " +
            "    LEFT JOIN pg_catalog.pg_namespace nam " +
            "        ON nam.oid = typ.typnamespace " +
            "    WHERE (typ.typrelid = 0 " +
            "            OR (SELECT cls.relkind = 'c' " +
            "                    FROM pg_catalog.pg_class AS cls " +
            "                    WHERE cls.oid = typ.typrelid)) " +
            "        AND NOT EXISTS " +
            "            (SELECT 1 " +
            "                FROM pg_catalog.pg_type typx " +
            "                WHERE typx.oid = typ.typelem " +
            "                    AND typx.typarray = typ.oid) " +
            "        AND pg_catalog.pg_type_is_visible (typ.oid) ";

		List<String[]> rows = runQuery(query);
		for (String[] row : rows) {
			TypeData tData = new TypeData();
			tData.name = row[0];
            tData.namespace = row[1];
            tData.internalName = row[2];
            tData.size = row[3];
            tData.description = row[4];
            typeDetails.put(tData.name, tData);
		}

		return typeDetails.keySet().stream()
		    .sorted(String.CASE_INSENSITIVE_ORDER)
		    .toArray(String[]::new);
	}
	
	@Override
	public String getSource(String catalog, String schema, String entry) {
	    return (typeDetails.containsKey(entry)) ? typeDetails.get(entry).toString() : "";
	}

    @Override
    public String getCreate(String catalog, String schema, String entry) {
        return getSource(catalog, schema, entry);
    }
}
