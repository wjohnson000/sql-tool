package sqltool.common;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import sqltool.config.UserConfig;


public class AppLogger implements Observer {

//	===========================================================================
//	Create the log4j environment and get the application logger
//	===========================================================================
	private static Logger appLogger = null;
	private static Properties log4jProps = new Properties();

	static {
		System.out.println("URL: " + AppLogger.class.getResource("log4j.properties"));
		ResourceBundle log4jBundle = ResourceBundle.getBundle("sqltool/common/log4j");
		System.out.println("BND: " + log4jBundle);
		for (Enumeration<String> enumx=log4jBundle.getKeys();  enumx.hasMoreElements(); ) {
			String key = enumx.nextElement();
			String val = log4jBundle.getString(key);
			log4jProps.put(key, val);
		}
		System.out.println("log4j: " + log4jProps);
	}

	/**
	 * Read the log file and programmatically set up the logging 
	 * @param logFileName
	 */
	private static void SetupLogging(String logFileName) {
		System.out.println("Log file name: " + logFileName);
		appLogger = null;
		if (logFileName != null  &&  logFileName.trim().length() > 0) {
			log4jProps.put("log4j.appender.sqltool.File", logFileName);
			try {
				PropertyConfigurator.configure(log4jProps);
				appLogger = Logger.getLogger(AppLogger.class);
			} catch (Exception ex) {
				System.out.println("EX: " + ex);
				appLogger = null;
			}
		}
	}


//	===========================================================================
//	Flags that control whether debug or info stuff is turned on
//	===========================================================================
	private boolean doDebug = false;
	private boolean doInfo  = false;


	/**
	 * Constructor ... register ourselves as a listener to user-config
	 * actions ...
	 */
	protected AppLogger() {
		SqlToolkit.userConfig.addObserver(this);
		checkConfig(UserConfig.PARAM_LOG_FILE);
		checkConfig(UserConfig.PARAM_LOG_LEVEL);
	}


	/**
	 * Put junk into the log file ...
	 * @param text stuff to be logged (as INFO)
	 * @return this logger, so we can do multiple "appends" if needed
	 */
	public AppLogger logInfo(String text) {
		if (doInfo  &&  appLogger != null) {
			appLogger.info(text);
		}
		return this;
	}
	

	/**
	 * Put junk into the log file ...
	 * @param text stuff to be logged (as FATAL)
	 * @return this logger, so we can do multiple "appends" if needed
	 */
	public AppLogger logFatal(String text) {
		if (appLogger != null) {
			appLogger.fatal(text);
		}
		return this;
	}
	

	/**
	 * Put junk into the log file ...
	 * @param text stuff to be logged (as DEBUG)
	 * @return this logger, so we can do multiple "appends" if needed
	 */
	public AppLogger logDebug(String text) {
		if (doDebug  &&  appLogger != null) {
			appLogger.debug(text);
		}
		return this;
	}


	/**
	 * If the user has changed the debug level, sit up and take notice
	 */
	public void update(Observable source, Object arg) {
		if (source == SqlToolkit.userConfig) {
			checkConfig((String)arg);
		}
	}

	/**
	 * Update the logging if the user has change the log configuration
	 * @param alias
	 */
	private void checkConfig(String alias) {
		if (UserConfig.PARAM_LOG_LEVEL.equals(alias)) {
			String logLevel = SqlToolkit.userConfig.getLogLevel();
			if ("INFO".equals(logLevel)) {
				doInfo  = true;
			} else if ("DEBUG".equals(logLevel)) {
				doDebug = true;
				doInfo  = true;
			}
		} else if (UserConfig.PARAM_LOG_FILE.equals(alias)) {
			String logFile = SqlToolkit.userConfig.getLogFile();
			SetupLogging(logFile);
		}
	}

}
