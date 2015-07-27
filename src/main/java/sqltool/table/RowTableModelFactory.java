package sqltool.table;


import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.UUID;

import sqltool.common.SqlToolkit;


/**
 * Given a query -- or multiple queries -- hit the database, retrieve the
 * results, and create a {@link RowTableModel} from the results.
 */
public class RowTableModelFactory implements Runnable {
	
	/** Look for NBS since they aren't recognized by most database engines */
	static final char NON_BREAKING_SPACE = 160;

	/** Map between JDBC types and DDL types */
	static java.util.Map<Integer,String> JDBCType;
	
	static {
		JDBCType = new java.util.HashMap<Integer,String>(100);
		JDBCType.put(new Integer(Types.BIT),           "BIT");
		JDBCType.put(new Integer(Types.TINYINT),       "TINYINT");
		JDBCType.put(new Integer(Types.BIGINT),        "BIGINT");
		JDBCType.put(new Integer(Types.LONGVARBINARY), "LONGVARBINARY");
		JDBCType.put(new Integer(Types.VARBINARY),     "VARBINARY");
		JDBCType.put(new Integer(Types.BINARY),        "BINARY");
		JDBCType.put(new Integer(Types.LONGVARCHAR),   "LONGVARCHAR");
		JDBCType.put(new Integer(Types.NULL),          "NULL");
		JDBCType.put(new Integer(Types.CHAR),          "CHAR");
		JDBCType.put(new Integer(Types.NUMERIC),       "NUMERIC");
		JDBCType.put(new Integer(Types.DECIMAL),       "DECIMAL");
		JDBCType.put(new Integer(Types.INTEGER),       "INTEGER");
		JDBCType.put(new Integer(Types.SMALLINT),      "SMALLINT");
		JDBCType.put(new Integer(Types.FLOAT),         "FLOAT");
		JDBCType.put(new Integer(Types.REAL),          "REAL");
		JDBCType.put(new Integer(Types.DOUBLE),        "DOUBLE");
		JDBCType.put(new Integer(Types.VARCHAR),       "VARCHAR");
		JDBCType.put(new Integer(Types.BOOLEAN),       "BOOLEAN");
		JDBCType.put(new Integer(Types.DATALINK),      "DATALINK");
		JDBCType.put(new Integer(Types.DATE),          "DATE");
		JDBCType.put(new Integer(Types.TIME),          "TIME");
		JDBCType.put(new Integer(Types.TIMESTAMP),     "TIMESTAMP");
		JDBCType.put(new Integer(Types.OTHER),         "OTHER");
		JDBCType.put(new Integer(Types.JAVA_OBJECT),   "JAVA_OBJECT");
		JDBCType.put(new Integer(Types.DISTINCT),      "DISTINCT");
		JDBCType.put(new Integer(Types.STRUCT),        "STRUCT");
		JDBCType.put(new Integer(Types.ARRAY),         "ARRAY");
		JDBCType.put(new Integer(Types.BLOB),          "BLOB");
		JDBCType.put(new Integer(Types.CLOB),          "CLOB");
		JDBCType.put(new Integer(Types.REF),           "REF");
	}
	
//	=============================================================================
//	I N S T A N C E    V A R I A B L E S
//	-- beActive: Flag that indicates if we should be retrieving the next
//	   row of data or not:  this will allow an application to
//	   stop the retrieval in cases of large or slow queries
//	-- isActive: Flag indicates whether the thread is still running or if
//	   we have finished
//	-- stopNow:  Flag that indicates that we should stop RIGHT NOW and
//	   not give the user an opportunity to re-start the row
//	   data retrieval
//	-- isMulti:  TRUE if there are multiple results, FALSE otherwise
//  -- rowCount: number of rows returned so far
//  -- rowLimit: maximum number of rows to return (0=no limit)
//	-- query:    SQL query string
//	-- message:  Exception message, or other database warning
//	-- conn:     Database connection
//	-- sqlModel: SimpleTableModel instance that is being created
//	=============================================================================
	private boolean isActive = false;
	private boolean beActive = false;
	private boolean stopNow  = false;
	private boolean isMulti  = false;
	private int     rowCount = 0;
	private int     rowLimit = 0;
	private String query     = null;
	private String message   = null;
	private String errorMessage = null;
	private String sqlDelim  = null;
	private Connection conn  = null;
	private RowTableModel      sqlModel = null;
	
	/**
	 * Constructor don't do nothing other than set the initial state ...
	 */
	public RowTableModelFactory() {
		stopNow  = false;
		beActive = false;
	}
	
	
	/**
	 * Retrieve flag indicating if the query is still running, or if the
	 * query has finished
	 * @return TRUE if the query is still running; FALSE otherwise
	 */
	public boolean isActive()   {
		return isActive;
	}

	/**
	 * Retrieve the error/warning/result message from the query, if any
	 * @return database error/warning/result
	 */
	public String  getMessage() {
		return message;
	}

	/**
	 * Retrieve the error message from the query, if any
	 * @return database error
	 */
	public String  getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Request a "halt" on the database query; the query can later be re-started
	 */
	public void halt() {
		SqlToolkit.appLogger.logDebug("RowTableModelFactory.HALT: " + this);

		beActive = false;
		if (sqlModel != null) {
			sqlModel.canSort = true;
		}
	}

	/**
	 * Request a "re-start" on the database query
	 */
	public void restart() {
		SqlToolkit.appLogger.logDebug("RowTableModelFactory.RESTART: " + this);

		beActive = true;
		if (sqlModel != null) {
			sqlModel.canSort = false;
		}
	}

	/**
	 * Request a "stop" on the database query; the query can NOT be
	 * re-started
	 */
	public void getOut() {
		SqlToolkit.appLogger.logDebug("RowTableModelFactory.GETOUT: " + this);

		stopNow = true;
		if (sqlModel != null) {
			sqlModel.canSort = true;
		}
	}


	/**
	 * Create a "RowTableModel" from a query string.  The query string could contain
	 * one or more separate queries.
	 * 
	 * @param conn database connection
	 * @param query query string
	 * @param sqlDelim delimiter that separates the queries
	 * @param isMulti TRUE if there are multiple queries; FALSE otherwise
	 * @return generated {@link RowTableModel}
	 */
	public RowTableModel createModelData(Connection conn, String query, String sqlDelim, boolean isMulti) {
		return this.createModelData(conn, query, sqlDelim, isMulti, 0);
	}


	/**
	 * Create a "RowTableModel" from a query string.  The query string could contain
	 * one or more separate queries.
	 * 
	 * @param conn database connection
	 * @param query query string
	 * @param sqlDelim delimiter that separates the queries
	 * @param isMulti TRUE if there are multiple queries; FALSE otherwise
	 * @param rowLimit maximum number of rows to return, zero (0) means unlimited
	 * @return generated {@link RowTableModel}
	 */
	public RowTableModel createModelData(Connection conn, String query, String sqlDelim, boolean isMulti, int rowLimit) {
		SqlToolkit.appLogger.logDebug("RowTableModelFactory.CreateModelData: " + this);

		stopNow       = false;
		beActive      = true;
		isActive      = true;
		message       = "";
		errorMessage  = "";
		this.conn     = conn;
		this.isMulti  = isMulti;
		this.rowCount = 0;
		this.rowLimit = rowLimit;
		this.sqlDelim = sqlDelim;
		
		query = query.trim();
		this.query = query.replace(NON_BREAKING_SPACE, ' ');  // Replace non-breaking spaces [ASCII(160)]
		sqlModel   = (isMulti) ? new RowTableModelMulti() : new RowTableModel();
		Thread myThread = new Thread(this);
		myThread.start();
		
		return sqlModel;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// See if we have meta-data, which will come into play later on
		boolean dbmdOK = true;
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			dbmdOK = dbmd != null;
		} catch (SQLException sqlex) {
			dbmdOK = false;
		}
		SqlToolkit.appLogger.logDebug("    RTMF.run: " + this + ";  DbmdOK: " + dbmdOK);

		String queryAll = query;
		String queryOne = null;
		while (queryAll.length() > 0) {
			int pos  = -1;
			if (sqlDelim != null) {
				pos = queryAll.indexOf(sqlDelim);
			}
			if (pos == -1) {
				queryOne = queryAll.trim();
				queryAll = "";
			} else {
				int dLen = sqlDelim.length() + 1;
				queryOne = queryAll.substring(0, pos).trim();
				queryAll = (pos+dLen < queryAll.length()) ? queryAll.substring(pos+dLen) : "";
			}
			SqlToolkit.appLogger.logDebug("    RTMF.run: " + this + ";  QueryLen: " + queryOne.length());

			if (queryOne.length() > 0) {
				message += "\n\n\n============================================================";
				message += "\n\nQuery:";
				message += "\n\n" + queryOne;
				message += "\n\n";
				execute(queryOne, dbmdOK);
				if (! isMulti) {
					queryAll = "";
				}
			}
			SqlToolkit.appLogger.logDebug("    RTMF.again? " + this + ";  QueryAll: " + queryAll.length());
		}

		getOut();
		conn  = null;
		query = null;
		isActive = false;
		beActive = false;
		stopNow  = true;
	}


	/**
	 * Run the query, checking the return code to see if it was an update
	 * statement (returning number of rows affected) or a select (returning
	 * a ResultSet).  Collection data about the column types, but if this
	 * is a multiple query default everything to type of "java.lang.String".
	 */
	private void execute(String queryOne, boolean dbmdOK) {
		SqlToolkit.appLogger.logDebug("      RTMF.execute: " + this + ";  QueryOne: " + queryOne);

		// First part ... create a statement and try and retrieve the data
		int       updCnt = -1;
		boolean   doUpd = false;
		Statement stmt = null;
		ResultSet rset = null;
		
		try {
			// If "execute(...)" returns TRUE, this there is a result-set, meaning
			// it was a query; otherwise it's an update (delete/insert/update)
			// command and we just get the number of rows affected
			stmt = conn.createStatement();
			boolean isRSet = stmt.execute(queryOne);
			if (isRSet) {
				rset = stmt.getResultSet();
				SqlToolkit.appLogger.logDebug("      RTMF.execute: QUERY ...");
			} else {
				doUpd = true;
				updCnt = stmt.getUpdateCount();
				SqlToolkit.appLogger.logDebug("      RTMF.execute: UPDATE ... " + updCnt);
			}
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   RTMF.execute.EX: " + sqlex);
			SqlToolkit.appLogger.logFatal("               SQL: " + queryOne);
			message += sqlex.getMessage();
			errorMessage += "\n\n============================================================";
			errorMessage += "\nQuery:";
			errorMessage += "\n    " + queryOne;
			errorMessage += "\n" + sqlex.getMessage();
			return;
		} catch (NullPointerException nex) {
			SqlToolkit.appLogger.logFatal("   RTMF.execute.EX: " + nex);
			SqlToolkit.appLogger.logFatal("               SQL: " + queryOne);
			message += "Unable to create database connection -- " + nex.getMessage();
			errorMessage += "\n\n============================================================";
			errorMessage += "\nQuery:";
			errorMessage += "\n    " + queryOne;
			errorMessage += "\nUnable to create database connection -- " + nex.getMessage();
			return;
		}
		
		
		try {
			if (doUpd) {
				String[] colName = { "Rows Updated" };
				Class<?>[] colType =  { String.class };
				Object[] rowData = { new Integer(updCnt) };
				sqlModel.setColumnData(colName, colType);
				sqlModel.addRow(rowData, true);
				message += "Rows updated: " + updCnt;
			} else {
				Object thing = null;
				String[] colName = null;
				Class<?>[] colType  = null;
				
				// If we have a request to cease and desist (stopNow), then we
				// won't do anything else; if we've received a temporary halt
				// request proceed with retrieving the column data only
				if (! stopNow) {
					// Figure out how many columns of data we have
					try {
						ResultSetMetaData rsmd = rset.getMetaData();
						SqlToolkit.appLogger.logDebug("      RTMF.execute: Result column count: " + rsmd.getColumnCount());

						colName = new String[rsmd.getColumnCount()];
						colType = new Class[colName.length];
						
						// Retrieve the column names and database types; we support "Integer",
						// "Long", "Double", "TimeStamp" and "String" (everything else)
						int cNameLen = 11;
						int cTypeLen = 9;
						int cPrecLen = 9;
						int cScalLen = 5;
						int cNullLen = 8;
						for (int i=0;  i<colName.length;  i++) {
							colName[i] = rsmd.getColumnName(i+1);
							int type = rsmd.getColumnType(i+1);
							int scal = 1;
							long prec = 1;
							try {
								scal = rsmd.getScale(i+1);
								prec = rsmd.getPrecision(i+1);
							} catch (Exception ex) { }
							if (type == Types.BIGINT   ||  type == Types.INTEGER  ||
									type == Types.SMALLINT ||  type == Types.TINYINT  ||
									(type == Types.BIT  &&  prec > 1)) {
								colType[i] = Integer.class;
							} else if (type == Types.DECIMAL  ||  type == Types.DOUBLE  ||
									type == Types.FLOAT  ||  type == Types.NUMERIC  ||
									type == Types.REAL) {
								colType[i] = (scal == 0) ? BigDecimal.class : Double.class;
							} else if (type == Types.DATE  ||  type == Types.TIME  ||
									type == Types.TIMESTAMP) {
								colType[i] = Timestamp.class;
							} else if (type == Types.CHAR  ||  type == Types.LONGVARCHAR  ||
									type == Types.VARCHAR) {
								colType[i] = String.class;
							} else if (type == Types.BOOLEAN  ||  type == Types.BIT) {
								colType[i] = Boolean.class;
							} else if (type == Types.CLOB) {
								colType[i] = Clob.class;
                            } else if (type == Types.BINARY  &&  prec == 16) {
                                colType[i] = UUID.class;
							} else {
								colType[i] = Object.class;
							}

							// Pull out the column NAME, TYPE, PRECISION, SCALE and NULL-ability, checking
							// for nasty exceptions in some cases
							cNameLen = Math.max(cNameLen, (""+colName[i]).length());
							try {
								if (dbmdOK) {
									cTypeLen = Math.max(cTypeLen, (""+rsmd.getColumnTypeName(i+1)).length());
								} else {
									cTypeLen = Math.max(cTypeLen, getTypeName(type).length());
								}
							} catch (SQLException sqlex) {
								cTypeLen = Math.max(cTypeLen, getTypeName(type).length());
							}
							try {
								cPrecLen = Math.max(cPrecLen, (""+rsmd.getPrecision(i+1)).length());
							} catch (NumberFormatException nfex) {
							}
							cScalLen = Math.max(cScalLen, (""+rsmd.getScale(i+1)).length());
							cNullLen = Math.max(cNullLen, (""+rsmd.isNullable(i+1)).length());
						}

						sqlModel.setColumnData(colName, colType);
						StringBuffer sb = new StringBuffer();
						addPadding("COLUMN NAME", cNameLen+3, sb);
						addPadding("DATA TYPE", cTypeLen+3, sb);
						addPadding("PRECISION", cPrecLen+3, sb);
						addPadding("SCALE", cScalLen+3, sb);
						sb.append("NULLABLE" + "\n");
						addPadding("===========", cNameLen+3, sb);
						addPadding("=========", cTypeLen+3, sb);
						addPadding("=========", cPrecLen+3, sb);
						addPadding("=====", cScalLen+3, sb);
						sb.append("========" + "\n");

						for (int i=0;  i<colName.length;  i++) {
							addPadding(colName[i], cNameLen+3, sb);
							try {
								if (dbmdOK) {
									addPadding(rsmd.getColumnTypeName(i+1), cTypeLen+3, sb);
								} else {
									addPadding(getTypeName(rsmd.getColumnType(i+1)), cTypeLen+3, sb);
								}
							} catch (SQLException sqlex) {
								addPadding(getTypeName(rsmd.getColumnType(i+1)), cTypeLen+3, sb);
							}
							try {
								addPadding(""+rsmd.getPrecision(i+1), cPrecLen+3, sb);
							} catch (NumberFormatException nfex) {
								addPadding("???", cPrecLen+3, sb);
							}
							addPadding(""+rsmd.getScale(i+1), cScalLen+3, sb);
							sb.append(""+rsmd.isNullable(i+1) + "\n");
						}
						message += sb.toString();
					} catch (SQLException sqlexx) {
						message += "Column Definition ex: " + sqlexx.getMessage();
					}
				}
				SqlToolkit.appLogger.logDebug("      RTMF.execute: Meta-data processing complete ... " + this);

				// Keep retrieving records unless we receive a command to cease and
				// desist (stopNow) or a request to pause temporarily (beActive)
				boolean hasMore = true;
				int irow = 0;
				while (! stopNow  &&  hasMore) {
					if (beActive) {
						if (rset.next()) {
							Object[] row = new Object[colName.length];
							for (int i = 0; i < row.length; i++) {
								SqlToolkit.appLogger.logDebug("i: " + i + " --> " + colType[i]);
								if (isMulti) {
									row[i] = ("" + rset.getString(i + 1)).trim();
								} else if (colType[i] == Integer.class) {
									row[i] = new Integer(rset.getInt(i + 1));
								} else if (colType[i] == Long.class) {
									row[i] = new Long(rset.getLong(i + 1));
								} else if (colType[i] == Double.class) {
									row[i] = new Double(rset.getDouble(i + 1));
								} else if (colType[i] == Timestamp.class) {
									row[i] = rset.getTimestamp(i + 1);
								} else if (colType[i] == String.class) {
									row[i] = ("" + rset.getString(i + 1)).trim();
								} else if (colType[i] == BigDecimal.class) {
									row[i] = rset.getBigDecimal(i + 1);
								} else if (colType[i] == Boolean.class) {
									row[i] = new Boolean(rset.getBoolean(i + 1));
								} else if (colType[i] == Clob.class) {
									row[i] = rset.getClob(i + 1);
                                } else if (colType[i] == UUID.class) {
                                    byte[] bytes = rset.getBytes(i + 1);
                                    row[i] = UUID.nameUUIDFromBytes(bytes);
								} else if (colType[i] == Object.class) {
									thing = rset.getObject(i + 1);
									row[i] = (thing == null) ? null : ("Class: " + thing.getClass().getName());
									thing = null;
								} else {
									row[i] = ("" + rset.getString(i + 1)).trim();
								}
								if (rset.wasNull()) {
									row[i] = null;   
								}
							}
							sqlModel.addRow(row, true);

							// If we've reached the maximum number of rows we want/need, force
							// a stop in the action and return
							rowCount++;
							if (rowLimit > 0  &&  rowCount >= rowLimit) {
								this.getOut();
							}
						} else {
							hasMore = false;
						}
					} else {
						try { Thread.sleep(250); } catch (Exception ex2) { }
					}
					irow++;
				}
				SqlToolkit.appLogger.logDebug("      RTMF.execute: Row data retrieval complete, rows: " + sqlModel.getRowCount());

			}
		} catch (SQLException sqlex) {
			SqlToolkit.appLogger.logFatal("   RTMF.execute.SQLEX: " + sqlex);
			SqlToolkit.appLogger.logFatal("                Query: " + queryOne);
			message += "\n\n" + sqlex.getMessage();
			errorMessage += "\n\n============================================================";
			errorMessage += "\nQuery:";
			errorMessage += "\n    " + queryOne;
			errorMessage += "\n" + sqlex.getMessage();
		} catch (NullPointerException nex) {
			SqlToolkit.appLogger.logFatal("   RTMF.execute.NULLEX: " + nex);
			SqlToolkit.appLogger.logFatal("                 Query: " + queryOne);
			message += "\n\n" + nex.getMessage();
			errorMessage += "\n\n============================================================";
			errorMessage += "\nQuery:";
			errorMessage += "\n    " + queryOne;
			errorMessage += "\n" + nex.getMessage();
		} finally {
//			Trying to close or move to the end of the ResultSet can take a LONG time on
//			Sybase; so we'll just close the Statement and let the underlying JDBC
//			classes clean up after us ...
//			try { if (rset != null) rset.afterLast(); } catch (Exception ex2) { }
			try { if (rset != null) rset.close(); } catch (Exception ex2) { }
			try { if (stmt != null) stmt.close(); } catch (Exception ex2) { }
		}
	}


	/**
	 * Add padding to the end of some text to make it a specific length
	 * @param text input text
	 * @param maxLen maximum length [if the input text is longer than the
	 *        maximum length, no padding will occur]
	 * @param target StringBuffer where the results will be placed
	 */
	private void addPadding(String text, int maxLen, StringBuffer target) {
		if (text == null) {
			text = "";
		}
		target.append(text);
		for (int i=0;  i<maxLen-text.length();  i++) {
			target.append(" ");
		}
	}


	/**
	 * Return a "human-readable" datatype based on the JDBC type
	 * @param type JDBC type
	 * @return human-readable data type
	 */
	private String getTypeName(int type) {
		String name = JDBCType.get(new Integer(type));
		return (name == null) ? "UNKNOWN" : name;
	}
}