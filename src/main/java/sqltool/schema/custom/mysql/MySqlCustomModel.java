package sqltool.schema.custom.mysql;

import java.sql.Connection;
import sqltool.common.db.DbCustomModel;


/**
 * Extend {@link DbCustomModel} by listing all of the supported Oracle
 * custom types.
 * 
 * @author wjohnson000
 *
 */
public class MySqlCustomModel extends DbCustomModel {

	/**
	 * Constructor, which requires a database connection
	 * @param conn database connection
	 */
	public MySqlCustomModel(Connection conn) {
		super(conn);
		registerDataTypes();
	}

	/**
	 * Explicitly register all of the new Oracle-specific data-types 
	 */
	private void registerDataTypes() {
		this.registerDataType(new TableDataType());
		this.registerDataType(new ViewDataType());
		this.registerDataType(new ProcedureDataType());
		this.registerDataType(new FunctionDataType());
		this.registerDataType(new TriggerDataType());
		this.registerDataType(new StatusVariables());
	}
}
