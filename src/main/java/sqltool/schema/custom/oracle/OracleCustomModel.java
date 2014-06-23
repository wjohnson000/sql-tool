package sqltool.schema.custom.oracle;

import java.sql.Connection;
import sqltool.common.db.DbCustomModel;


/**
 * Extend {@link DbCustomModel} by listing all of the supported Oracle
 * custom types.
 * 
 * @author wjohnson000
 *
 */
public class OracleCustomModel extends DbCustomModel {

	/**
	 * Constructor, which requires a database connection
	 * @param conn database connection
	 */
	public OracleCustomModel(Connection conn) {
		super(conn);
		registerDataTypes();
	}

	/**
	 * Explicitly register all of the new Oracle-specific data-types 
	 */
	private void registerDataTypes() {
		this.registerDataType(new TableDataType());
		this.registerDataType(new ViewDataType());
		this.registerDataType(new SequenceDataType());
		this.registerDataType(new SynonymDataType());
		this.registerDataType(new PackageDataType(PackageDataType.TYPE_PACKAGE));
		this.registerDataType(new PackageDataType(PackageDataType.TYPE_PACKAGE_BODY));
		this.registerDataType(new FunctionDataType());
		this.registerDataType(new TriggerDataType());
		this.registerDataType(new ObjectTypeDataType());
		this.registerDataType(new TableTypeDataType());
		this.registerDataType(new DbLinkDataType());
	}
}
