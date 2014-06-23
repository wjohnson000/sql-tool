package sqltool.schema.custom.postgres;

import java.sql.Connection;
import sqltool.common.db.DbCustomModel;


/**
 * Extend {@link DbCustomModel} by listing all of the supported Postgres
 * custom types.
 * 
 * @author wjohnson000
 *
 */
public class PostgresCustomModel extends DbCustomModel {

	/**
	 * Constructor, which requires a database connection
	 * @param conn database connection
	 */
	public PostgresCustomModel(Connection conn) {
		super(conn);
		registerDataTypes();
	}

	/**
	 * Explicitly register all of the new Postgres-specific data-types 
	 */
	private void registerDataTypes() {
		this.registerDataType(new TableDataType());
        this.registerDataType(new ConstraintDataType());
        this.registerDataType(new ViewDataType());
        this.registerDataType(new ViewDefintionType());
        this.registerDataType(new SequenceDataType());
        this.registerDataType(new FunctionDataType());
        this.registerDataType(new TriggerDataType());
        this.registerDataType(new IndexDataType());
	}
}
