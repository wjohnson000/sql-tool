package sqltool.config;

import xmlutil.*;


/**
 * User-configuration, read in at start-up, modifiable, saved when the
 * application shuts down
 * 
 * @author wjohnson000
 *
 */
public class UserConfig extends BaseConfig {

	// TODO: change these to type "enum"?
	// Constants to indicate what changed ...
	public static final String PARAM_TAB_SPACING  = "tab.spacing";
	public static final String PARAM_CLICK_COUNT  = "click.count";
	public static final String PARAM_FIELD_DELIM  = "field.delimiter";
	public static final String PARAM_FIELD_QUOTE  = "field.quote";
	public static final String PARAM_SQL_DELIM    = "sql.delimiter";
    public static final String PARAM_BODY_DELIM   = "body.delimiter";
	public static final String PARAM_LOG_LEVEL    = "log.level";
	public static final String PARAM_LOG_FILE     = "log.file";

	public static final String FONT_QUERY_EDITOR  = "query.editor.font";
	public static final String FONT_QUERY_RESULT  = "query.result.font";
	public static final String FONT_SCHEMA_EDITOR = "schema.editor.font";
	public static final String FONT_SCHEMA_RESULT = "schema.result.font";
	
	
	/**
	 * Read in the parameter (configuration) values, and then set up any
	 * default values for undefined parameters.
	 */
	public UserConfig() {
		super();
		if (getTabSpacing() == 0) {
			setTabSpacing(8);
		}
		if (getClickCount() == 0) {
			setClickCount(2);
		}
		if (getFieldDelim() == null) {
			setFieldDelim(",");
		}
		if (getFieldQuote() == null) {
			setFieldQuote("\"");
		}
		if (getSqlDelim() == null) {
			setSqlDelim(";");
		}
		if (getBodyDelim() == null) {
		    setBodyDelim("$BODY$");
		}
		if (getLogLevel() == null) {
			setLogLevel("OFF");
		}
		if (getLogFile() == null) {
			setLogFile("");
		}
	}
	
	/**
	 * This method must be defined in each sub-class, and must return
	 * the configuration file name (NAME ONLY; the default path must
	 * be System.property("user.home").
	 *
	 * @return configuration file name
	 */
	public String getConfigFileName() {
		return ".sql-tool-config.xml";
	}
	
	
	/**
	 * Manage the default spacing for a TAB, default to 8 spaces
	 * @param alias
	 */
	public int getClickCount() {
		return getIntValue(PARAM_CLICK_COUNT);
	}
	
	public void setClickCount(int val) {
		val = Math.max(val, 1);
		val = Math.min(val, 2);
		setIntValue(PARAM_CLICK_COUNT, val);
		notifyAll(PARAM_CLICK_COUNT);
	}
	
	/**
	 * Manage the default spacing for a TAB, default to 8 spaces
	 * @param alias
	 */
	public int getTabSpacing() {
		return getIntValue(PARAM_TAB_SPACING);
	}
	
	public void setTabSpacing(int val) {
		setIntValue(PARAM_TAB_SPACING, val);
		notifyAll(PARAM_TAB_SPACING);
	}
	
	/**
	 * Manage the default field delimiter when saving results
	 * @param alias
	 */
	public String getFieldDelim() {
		return getStringValue(PARAM_FIELD_DELIM);
	}
	
	public void setFieldDelim(String val) {
		setStringValue(PARAM_FIELD_DELIM, val);
		notifyAll(PARAM_FIELD_DELIM);
	}
	
	/**
	 * Manage the default [and optional] quote character
	 * @param alias
	 */
	public String getFieldQuote() {
		String quote = getStringValue(PARAM_FIELD_QUOTE);
		if (quote == null) {
			quote = "";
		}
		return quote;
	}
	
	public void setFieldQuote(String val) {
		setStringValue(PARAM_FIELD_QUOTE, val);
		notifyAll(PARAM_FIELD_QUOTE);
	}
	
	/**
	 * Manage the default command delimiter when executing multiple
	 * statements
	 * @param alias
	 */
	public String getSqlDelim() {
		return getStringValue(PARAM_SQL_DELIM);
	}
	
	public void setSqlDelim(String val) {
		setStringValue(PARAM_SQL_DELIM, val);
		notifyAll(PARAM_SQL_DELIM);
	}
    
    /**
     * Manage the default body delimiter when creating a stored procedure
     * which may contain nested "sqlDelim" characters
     * @param alias
     */
    public String getBodyDelim() {
        return getStringValue(PARAM_BODY_DELIM);
    }
    
    public void setBodyDelim(String val) {
        setStringValue(PARAM_BODY_DELIM, val);
        notifyAll(PARAM_BODY_DELIM);
    }
	
	/**
	 * Manage the log level (OFF, INFO or DEBUG)
	 * @param alias
	 */
	public String getLogLevel() {
		return getStringValue(PARAM_LOG_LEVEL);
	}
	
	public void setLogLevel(String val) {
		setStringValue(PARAM_LOG_LEVEL, val);
		notifyAll(PARAM_LOG_LEVEL);
	}
	
	/**
	 * Manage the log level (OFF, INFO or DEBUG)
	 * @param alias
	 */
	public String getLogFile() {
		return getStringValue(PARAM_LOG_FILE);
	}
	
	public void setLogFile(String val) {
		setStringValue(PARAM_LOG_FILE, val);
		notifyAll(PARAM_LOG_FILE);
	}

}
