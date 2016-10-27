package sqltool.schema.custom.generic;

import java.sql.Connection;

import sqltool.common.db.DbCustomModel;

public class GenericCustomModel extends DbCustomModel {

    public GenericCustomModel(Connection conn) {
        super(conn);
        registerDataTypes();
    }

    private void registerDataTypes() {
        this.registerDataType(new TableDataType());
        this.registerDataType(new ViewDataType());
        this.registerDataType(new ProcedureDataType());
        this.registerDataType(new SynonymDataType());
        this.registerDataType(new KeywordType());
    }
}
