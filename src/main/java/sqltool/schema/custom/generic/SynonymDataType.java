package sqltool.schema.custom.generic;

import sqltool.common.db.DbInfoModel;

public class SynonymDataType extends BaseDataType {

    @Override
    public String getDataType() {
        return DbInfoModel.MODE_SYNONYM;
    }
}
