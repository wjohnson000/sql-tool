package sqltool.schema.custom.generic;

import sqltool.common.db.DbInfoCache;

public class KeywordType extends BaseDataType {

    @Override
    public String getDataType() {
        return "KEYWORD";
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
        if (myModel == null) {
            myModel = DbInfoCache.GetInfoModel(conn);
        }
        return myModel.getKeywordList();
    }

    @Override
    public String getCreate(String catalog, String schema, String entry) {
        return "";
    }
}
