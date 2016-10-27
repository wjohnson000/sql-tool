package sqltool.schema.custom.generic;

import java.sql.Connection;

import sqltool.common.db.DbInfoCache;
import sqltool.common.db.DbInfoModel;
import sqltool.common.db.DbRawDataType;
import sqltool.common.db.DbStructure;
import sqltool.schema.script.DefaultScriptGenerator;
import sqltool.schema.script.ScriptGenerator;

public abstract class BaseDataType implements DbRawDataType {

//  ===========================================================================
//  static stuff ... constants, if you will ...
//  ===========================================================================
    protected static final String DISCLAIMER =
        "-- This is an auto-generated script to create the DB object as \n" +
        "-- it is currently defined in the database.  There is no guarantee \n" +
        "-- that you won't have to 'tweak' it slightly to re-create the actual \n" +
        "-- definition.  But it's a start ...\n" +
        "\n";

    protected static final String EMPTY_LIST = "-- NONE --";

//  ===========================================================================
//  instance variable(s)
//  ===========================================================================
    Connection   conn;
    DbInfoModel  myModel;

    @Override
    public boolean canCreate() {
        return true;
    }

    @Override
    public boolean canDrop() {
        return true;
    }

    @Override
    public String[] getEntries(String catalog, String schema) {
        if (myModel == null) {
            myModel = DbInfoCache.GetInfoModel(conn);
        }
        DbStructure currDbStruct = myModel.getDbStructure(catalog, schema, getDataType());
        if (currDbStruct == null  ||  currDbStruct.getNameCount() == 0) {
            return new String[] { EMPTY_LIST };
        } else {
            return currDbStruct.getNames();
        }
    }

    @Override
    public String getSource(String catalog, String schema, String entry) {
        String script = "";
        DbStructure currDbStruct = myModel.getDbStructure(catalog, schema, getDataType());
        if (currDbStruct != null  &&  entry != null  &&  ! entry.equalsIgnoreCase(EMPTY_LIST)) {
            ScriptGenerator scGen = new DefaultScriptGenerator();
            scGen.setDbInfoModel(myModel);
            myModel.populateDetails(catalog, schema, getDataType(), entry);
            script = scGen.getTableDef(catalog, schema, entry);
        }

        return script;
    }

    @Override
    public String getCreate(String catalog, String schema, String entry) {
        return getSource(catalog, schema, entry);
    }

    @Override
    public String getDrop(String catalog, String schema, String entry) {
        return DISCLAIMER + "DROP " + getDataType() + " " + qualifiedName(schema, entry) + ";";
    }

    @Override
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    /**
     * Create a fully-qualified entry name, either "SCHEMA.ENTRY" or "ENTRY"
     * @param scheme schema name
     * @param entry entry name
     */
    protected String qualifiedName(String schema, String entry) {
        return (schema == null ? "" : (schema + ".")) + entry;
    }
}
