package sqltool.query;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;

import sqltool.common.SqlToolkit;


/**
 * StyledDocument that knows how to "speak" and "parse" SQL, allowing for
 * context-sensitive text high-lighting.
 * @author wjohnson000
 *
 */
public class SqlDocument extends DefaultStyledDocument {
	
	static final long serialVersionUID = 8306837094897482045L;


//	=============================================================================
//	DEFAULT keywords and function names for SQL
//	=============================================================================
	static final String[] DEFAULT_KEYWORD_LIST = {
		"ABSOLUTE",  "ACTION",  "ADD",  "ALL",  "ALLOCATE",  "ALTER",
		"AND",  "ANY",  "ARE",  "AS",  "ASC",  "ASSERTION",  "AT",
		"AUTHORIZATION",  "AVG",  "BEGIN",  "BETWEEN",  "BIT",  "BOTH",
		"BY",  "CASCADE",  "CASCADED",  "CASE",  "CAST",  "CATALOG",
		"CHAR",  "CHARACTER",  "CHECK",  "CLOSE",  "COALESCE",  "COLLATE",
		"COLLATION",  "COLUMN",  "COMMIT",  "CONNECT",  "CONNECTION",
		"CONSTRAINT",  "CONSTRAINTS",  "CONTINUE",  "CORRESPONDING",
		"CREATE",  "CROSS",  "CURRENT",  "CURRENT_USER",  "CURSOR",  "DATE",
		"DAY",  "DEALLOCATE",  "DEC",  "DECIMAL",  "DECLARE",  "DEFAULT",
		"DEFERRABLE",  "DEFERRED",  "DELETE",  "DESC",  "DESCRIBE",
		"DESCRIPTOR",  "DIAGNOSTICS",  "DISCONNECT",  "DISTINCT",  "DOMAIN",
		"DOUBLE",  "DROP",  "ELSE",  "END",  "END-EXEC",  "ESCAPE",
		"EXCEPT",  "EXCEPTION",  "EXEC",  "EXECUTE",  "EXISTS",
		"EXTERNAL",  "FALSE",  "FETCH",  "FIRST",  "FLOAT",  "FOR",
		"FOREIGN",  "FOUND",  "FROM",  "FULL",  "GET",  "GLOBAL",  "GO",
		"GOTO",  "GRANT",  "GROUP",  "HAVING",  "HOUR",  "IDENTITY",
		"IMMEDIATE",  "IN",  "INDICATOR",  "INITIALLY",  "INNER",
		"INPUT",  "INSENSITIVE",  "INSERT",  "INT",  "INTEGER",
		"INTERSECT",  "INTERVAL",  "INTO",  "IS",  "ISOLATION",  "JOIN",
		"KEY",  "LANGUAGE",  "LAST",  "LEADING",  "LEFT",  "LEVEL",
		"LIKE",  "LOCAL",  "MATCH",  "MAX",  "MIN",  "MINUTE",  "MODULE",
		"MONTH",  "NAMES",  "NATIONAL",  "NATURAL",  "NCHAR",  "NEXT",
		"NO",  "NOT",  "NULL",  "NULLIF",  "NUMERIC",  "OF",  "ON",
		"ONLY",  "OPEN",  "OPTION",  "OR",  "ORDER",  "OUTER",  "OUTPUT",
		"OVERLAPS",  "PAD",  "PARTIAL",  "PRECISION",  "PREPARE",
		"PRESERVE",  "PRIMARY",  "PRIOR",  "PRIVILEGES",  "PROCEDURE",
		"PUBLIC",  "READ",  "REAL",  "REFERENCES",  "RELATIVE",
		"RESTRICT",  "REVOKE",  "RIGHT",  "ROLLBACK",  "ROWS",  "SCHEMA",
		"SCROLL",  "SECOND",  "SECTION",  "SELECT",  "SESSION",
		"SESSION_USER",  "SET",  "SIZE",  "SMALLINT",  "SOME",  "SPACE",
		"SQL",  "SQLCODE",  "SQLERROR",  "SQLSTATE",  "SUM",
		"SYSTEM_USER",  "TABLE",  "TEMPORARY",  "THEN",  "TIME",
		"TIMESTAMP",  "TIMEZONE_HOUR",  "TIMEZONE_MINUTE",  "TO",
		"TRAILING",  "TRANSACTION",  "TRANSLATION",  "TRUE",  "UNION",
		"UNIQUE",  "UNKNOWN",  "UPDATE",  "USAGE",  "USER",  "USING",
		"VALUE",  "VALUES",  "VARCHAR",  "VARYING",  "VIEW",  "WHEN",
		"WHENEVER",  "WHERE",  "WITH",  "WORK",  "WRITE",  "YEAR",
		"ZONE"  };

	
	static final String[] DEFAULT_FUNCTION_LIST = {
		"ABS",  "AVG",  "BIT_LENGTH",  "CEIL",  "CHARACTER_LENGTH",
		"CHAR_LENGTH",  "CONVERT",  "COUNT",  "CURRENT_DATE",  "CURRENT_TIME",
		"CURRENT_TIMESTAMP","DECODE",  "EXTRACT",  "FLOOR",  "INSTR",  "ISNULL",
		"LEFT",  "LENGTH",  "LOWER",  "LPAD",  "LTRIM",  "MAX",  "MIN",  "MOD",
		"NVL",  "POSITION",  "REPLACE",  "RIGHT",  "ROUND",  "RPAD",  "RTRIM",
		"SQRT",  "SUBSTR",  "SUBSTRING",  "SUM",  "TO_CHAR",  "TO_DATE",
		"TRANSLATE",  "TRIM",  "TRUNC",  "UPPER",  };


//	=============================================================================
//	Helper enumeration and class, to assist with the SQL parsing effort
//	=============================================================================
	enum TextType {
		COMMENT, KEYWORD, FUNCTION, NUMERIC, CHARACTER, WHITE_SPACE, OTHER
	};

	class SqlChunk {
		int      startPos;
		int      length;
		TextType type;

		public SqlChunk(int startPos, int length, TextType type) {
			this.startPos = startPos;
			this.length   = length;
			this.type     = type;
		}
	}

//	=============================================================================
//	Instance variables:
//  -- lineCount: number of lines in this document
//  -- maxLine: longest line in the text
//	-- keywordList: list of keywords associated with the current database
//	-- functionList: list of function names associated with the current database
//	-- styleMap: associate position in text with a particular style
//	=============================================================================
	private int       lineCount = 1;
	private boolean   adjustDone = true;
	private boolean   bulkLoad   = false;
	private String    maxLine = "";
	private String[]  keywordList;
	private String[]  functionList;
	private ArrayList<SqlChunk> chunks = new ArrayList<SqlChunk>(10);


	/**
	 * Constructor sets up the default styles to use in "coloring" the SQL
	 *  -- RED for text [constant] strings
	 *  -- GREEN for anything in a comment
	 *  -- BLUE for keywords
	 *  -- BLUE/BOLD for function names
	 *  -- BLACK for anything else
	 */
	public SqlDocument() {
		Style redStyle = addStyle("Red", null);
		StyleConstants.setForeground(redStyle, Color.red);
		
		Style greenStyle = addStyle("Green", null);
		StyleConstants.setForeground(greenStyle, new Color(47, 155, 47));
		
		Style blueStyle = addStyle("Blue", null);
		StyleConstants.setForeground(blueStyle, Color.blue);
		
		Style blueBoldStyle = addStyle("BlueBold", blueStyle);
		StyleConstants.setBold(blueBoldStyle, true);
		
		Style blackStyle = addStyle("Black", null);
		StyleConstants.setForeground(blackStyle, Color.black);
		
		Style magentaStyle = addStyle("Magenta", null);
		StyleConstants.setForeground(magentaStyle, new Color(150, 35, 230));  // Color.magenta);

		Style currentQueryStyle = addStyle("Current", null);
		StyleConstants.setBackground(currentQueryStyle, new Color(255, 255, 204));
		
		setDefaultKeywords();
	}
	

	/**
	 * Method that handles insertion of new text: call the parent method
	 * to handle the actual insertion, and then make the colors pretty
	 * 
	 * @param offs offset into current text where new text is added
	 * @param str new text to insert
	 * @param as Attributes associated with the new text
	 * 
	 */
	public void insertString(int offs, String str, AttributeSet as)
			throws BadLocationException {
		super.insertString(offs, str, as);
		setPrettyColors();
	}
	

	/**
	 * Method that handles deleting of old text: call the parent method
	 * to handle the actual deletion, and then make the colors pretty
	 * 
	 * @param offs offset into current text where new text is added
	 * @param str new text to insert
	 * 
	 */
	public void remove(int offs, int len)
			throws BadLocationException {
		super.remove(offs, len);
		setPrettyColors();
	}
	

	/**
	 * Use the default set of keywords
	 */
	public void setDefaultKeywords() {
		keywordList  = DEFAULT_KEYWORD_LIST;
		functionList = DEFAULT_FUNCTION_LIST;
	}
	

	/**
	 * Apply a new set of keywords, as defined by the database ...
	 *
	 * @param addList Array of keywords to apply
	 */
	public void addKeywords(String[] addList) {
		if (addList != null  &&  addList.length > 0) {
			String[] temp = new String[keywordList.length + addList.length];
			for (int i=0;  i<keywordList.length;  i++) {
				temp[i] = keywordList[i];
			}
			for (int i=0;  i<addList.length;  i++) {
				temp[i+keywordList.length] = addList[i];
			}
			keywordList = temp;
			setPrettyColors();
		}
	}


	/**
	 * Apply a new set of functions, as defined by the database ...
	 *
	 * @param addList Array of function names to apply
	 */
	public void addFunctions(String[] addList) {
		if (addList != null  &&  addList.length > 0) {
			String[] temp = new String[functionList.length + addList.length];
			for (int i=0;  i<functionList.length;  i++) {
				temp[i] = functionList[i];
			}
			for (int i=0;  i<addList.length;  i++) {
				temp[i+functionList.length] = addList[i];
			}
			functionList = temp;
			setPrettyColors();
		}
	}
	

	/**
	 * Check to see if the document is in a "safe" state for determining the
	 * line count and longest line.  A "safe" state is anything other than
	 * when we are in the midst of processing (coloring) the text.
	 * 
	 * @return TRUE if we are "safe", FALSE otherwise
 	*/
	public boolean isSafeToProcess() {
		return adjustDone;
	}


	/**
	 * get the number of lines of text in this document.  Currently a "\n" or
	 * "\r" is considered as a line break character. 
	 * 
	 * @return the number of lines of text in the document.
	 */
	public int getLineCount() {
		return lineCount;
	}


	/**
	 * Get the longest line of text, based strictly on number of characters,
	 * not counting the "\n" or "\r" character.  The containing component can
	 * then determine the length of the line based on its font characteristics.
	 * 
	 * @return the longest line of text in the document.
	 */
	public String getLongestLine() {
		return maxLine;
	}


	/**
	 * If we are potentially setting a large amount of text and it may come
	 * across in several chunks, we want to turn off processing of the
	 * colors until everything is done.  This sets a flag that will prevent
	 * the text from being styled.  The user needs to call the companion
	 * method, "finishBulkLoad()" once all of the text is set.
	 * 
	 *  SqlDocument myDoc = new SqlDocument();
	 * 	myDoc.startBulkLoad();
	 *	myDocEditor.setText(someText); // a JTextPane containing the doc
	 *  myDoc.finishBulkLoad();
	 */
	public void startBulkLoad() {
		bulkLoad = true;
	}


	/**
	 * Flag the document that the application is done setting the text
	 * for this document, and re-color the text.
	 */
	public void finishBulkLoad() {
		bulkLoad = false;
		setPrettyColors();
	}


	/**
	 * Return the query given a text index and a delimiter ...
	 */
	public String getQueryAtIndex(int ndx, String delim) {

		StringBuffer query = new StringBuffer(512);
		Style comment = getStyle("Green");
		boolean onlyOne = true;

		try {
			char[] text = this.getText(0, getLength()).trim().toCharArray();
			for (int pos1=0;  pos1<text.length;  pos1++) {
				Style tmpStyle = getStyleAtPos(pos1);
				if (tmpStyle == null  ||  tmpStyle != comment) {
					query.append(text[pos1]);
				}
				if (pos1 > ndx  &&  query.toString().trim().length() == 0) {
					return query.toString().trim();
				} else if (query.indexOf(delim) > 0) {
					if (ndx <= pos1+delim.length()) {
						return query.toString().trim();
					} else if (onlyOne  &&  pos1 == text.length-1) {
						return query.toString().trim();
					} else {
						onlyOne = false;
						query = new StringBuffer(512);
					}
				}
			}
		} catch (Exception ex) {
		}

		return query.toString();
	}


	/**
	 * Add color to the text:
	 *   -- RED for strings
	 *   -- GREEN for comments
	 *   -- PURPLE-ish for numeric constants
	 *   -- BLUE for keywords
	 *   -- BLUE-BOLD for function names
	 * 
	 * If we are in the midst of a bulk load, don't do anything.  The application is
	 * responsible for telling us when the bulk load is done, at which point we'll
	 * color the text.
	 */
	private void setPrettyColors() {
		if (bulkLoad  ||  ! isSafeToProcess()) {
			return;
		}

		SqlToolkit.appLogger.logDebug(">>>Enter 'setPrettyColors' ... " + Thread.currentThread());
		long nnow = System.currentTimeMillis();
		adjustDone = false;
		lineCount = 1;
		maxLine  = "";
		chunks   = new ArrayList<SqlChunk>(100);

		// Turn the text into a character array ... just for fun ...
		char[] text = new char[0];
		try {
			text = this.getText(0, getLength()).toCharArray();
		} catch (Exception ex) { }

		// Pass one:
		//   count the number of lines & calculate the length of the longest one
		StringBuffer curLine = new StringBuffer(120);
		for (int pos1=0;  pos1<text.length;  pos1++) {
			boolean endOfLine = text[pos1] == '\n' || text[pos1] == '\r';
			boolean endOfText = (pos1 == text.length - 1);

			// Calculate the longest line in the text
			if (endOfLine  ||  endOfText) {
				if (endOfLine) {
					lineCount++;
				}
				if (curLine.length() > maxLine.length()) {
					maxLine = curLine.toString();
				}
				curLine = new StringBuffer(120);
			} else {
				curLine.append(text[pos1]);
			}
		}

		// Pass two:
		//   chunk of the text and set the pretty colors
		setCharacterAttributes(0, getLength(), getStyle("Black"), true);
		int startPos = 0;
		int tokenLen = 0;
		while (startPos < text.length) {
			tokenLen = this.getCommentLen(text, startPos);
			if (tokenLen > 0) {
				chunks.add(new SqlChunk(startPos, tokenLen, TextType.COMMENT));
			} else {
				tokenLen = this.getWhitespaceLen(text, startPos);
				if (tokenLen > 0) {
					chunks.add(new SqlChunk(startPos, tokenLen, TextType.WHITE_SPACE));
				} else {
					tokenLen = this.getStringLen(text, startPos);
					if (tokenLen > 0) {
						chunks.add(new SqlChunk(startPos, tokenLen, TextType.CHARACTER));
					} else {
						tokenLen = this.getAlphaNumericLen(text, startPos);
						if (tokenLen > 0) {
							String token = String.valueOf(text, startPos, tokenLen);
							if (isAKeyword(token, 0)) {
								chunks.add(new SqlChunk(startPos, tokenLen, TextType.KEYWORD));
							} else if (isAFunction(token, 0)) {
								chunks.add(new SqlChunk(startPos, tokenLen, TextType.FUNCTION));
							} else if (isANumber(token, 0)) {
								chunks.add(new SqlChunk(startPos, tokenLen, TextType.NUMERIC));
							} else {
								chunks.add(new SqlChunk(startPos, tokenLen, TextType.OTHER));
							}
						} else {
							tokenLen = 1;
							chunks.add(new SqlChunk(startPos, 1, TextType.OTHER));
						}
					}
				}
			}
			startPos += tokenLen;
		}

		// We are *really* done, so fire an event to notify our listeners that
		// it's safe to do any subsequent processing.
		try {
			this.writeLock();
			SqlToolkit.appLogger.logDebug("...Check1 'setPrettyColors' ... " + (System.currentTimeMillis()-nnow) + " ms");
			nnow = System.currentTimeMillis();

			applyStyle();
			SqlToolkit.appLogger.logDebug("...Check2 'setPrettyColors' ... " + (System.currentTimeMillis()-nnow) + " ms");
			nnow = System.currentTimeMillis();
		} finally {
			adjustDone = true;
			this.fireChangedUpdate(new DefaultDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE));
			this.writeUnlock();
		}
		
		SqlToolkit.appLogger.logDebug("<<<Exit 'setPrettyColors' ... " + (System.currentTimeMillis()-nnow) + " ms");
	}


	/**
	 * Pull a "comment" from the beginning of the text, if present
	 */
	private int getCommentLen(char[] text, int startPos) {
		// Check for single-line comment [starts with "--" and runs to end-of-line]
		int commentLen = 0;
		if (startPos+2 <= text.length) {
			if (text[startPos] == '-'  &&  text[startPos+1] == '-') {
				commentLen = 2;
				boolean again = true;
				for (int i=startPos+2;  again && i<text.length;  i++) {
					if (text[i] == '\n'  ||  text[i] == '\r') {
						again = false;
					} else {
						commentLen++;
					}
				}

			// Check for multi-line comment [starts with "/*" and ends with "*/"]
			} else if (text[startPos] == '/'  &&  text[startPos+1] == '*') {
				boolean again = true;
				char    prev = 'Z';
				commentLen = 2;
				for (int i=startPos+2;  again && i<text.length;  i++) {
					commentLen++;
					if (prev == '*'  &&  text[i] == '/') {
						again = false;
					}
					prev = text[i];
				}
			}
		}

		// Return the comment length ... if present
		return commentLen;
	}


	/**
	 * Pull the "whitespace" from the beginning of the text, if present
	 */
	private int getWhitespaceLen(char[] text, int startPos) {
		int whitespaceLen = 0;
		if (startPos < text.length) {
			if (isWhitespace(text[startPos])) {
				boolean again = true;
				for (int i=startPos;  again && i<text.length;  i++) {
					if (isWhitespace(text[i])) {
						whitespaceLen++;
					} else {
						again = false;
					}
				}
			}
		}

		// Return whitespace length ... if present
		return whitespaceLen;
	}


	/**
	 * Pull a "string" from the beginning of the text, if present
	 */
	private int getStringLen(char[] text, int startPos) {
		int stringLen = 0;
		if (startPos < text.length) {
			if (text[startPos] == '"'  ||  text[startPos] == '\'') {
				boolean again = true;
				for (int i=startPos;  again && i<text.length;  i++) {
					stringLen++;
					if (i> startPos  &&  text[i] == text[startPos]) {
						again = false;
					}
				}
				// If we are at the end of the text but have no trailing
				// quote, insert a "virtual" quote so the high-lighting
				// works correctly.
				if (again) stringLen++;
			}
		}

		// Return string length, if present
		return stringLen;
	}


	/**
	 * Pull an alpha-numeric chunk o' characters from the beginning of the
	 * text, if present; this could be a table name, field name, reserved
	 * word, etc ...
	 */
	private int getAlphaNumericLen(char[] text, int startPos) {
		int anLen = 0;
		if (startPos < text.length) {
			if (this.isAlphaNumeric(text[startPos])) {
				boolean again = true;
				for (int i=startPos;  again && i<text.length;  i++) {
					if (this.isAlphaNumeric(text[i])) {
						anLen++;
					} else {
						again = false;
					}
				}
			}
		}

		// Return alpha-numeric length, if present
		return anLen;
	}


	/**
	 * Once we've processed the entire document, apply the styles that we saved
	 */
	private void applyStyle() {
		for (SqlChunk chunk : chunks) {
			Style style = null;
			int startPos = chunk.startPos;
			int length   = chunk.length;

			if (chunk.type == TextType.CHARACTER) {
				startPos++;
				length -= 2;
				style = getStyle("Red");
			} else if (chunk.type == TextType.COMMENT) {
				style = getStyle("Green");
			} else if (chunk.type == TextType.FUNCTION) {
				style = getStyle("BlueBold");
			} else if (chunk.type == TextType.KEYWORD) {
				style = getStyle("Blue");
			} else if (chunk.type == TextType.NUMERIC) {
				style = getStyle("Magenta");
			}
			if (style != null) {
				setCharacterAttributes(startPos, length, style, true);
			}
		}
	}
	
	private Style getStyleAtPos(int pos) {
		for (int i=0;  i<chunks.size();  i++) {
			SqlChunk chunk = (SqlChunk)chunks.get(i);
			if (pos >= chunk.startPos  &&  pos < chunk.startPos+chunk.length) {
				Style style = null;

				if (chunk.type == TextType.CHARACTER) {
					style = getStyle("Red");
				} else if (chunk.type == TextType.COMMENT) {
					style = getStyle("Green");
				} else if (chunk.type == TextType.FUNCTION) {
					style = getStyle("BlueBold");
				} else if (chunk.type == TextType.KEYWORD) {
					style = getStyle("Blue");
				} else if (chunk.type == TextType.NUMERIC) {
					style = getStyle("Magenta");
				}
				return style;
			}
		}
		return null;
	}

	/**
	 * Convenience method to determine if a character is alphabetic (A-Z,
	 * ignoring case)
	 * 
	 * @param ch character to test
	 * @return TRUE of the character is alpha, FALSE otherwise
	 */
	private boolean isAlpha(char ch) {
		return (ch >= 'a' && ch <= 'z')  || (ch >= 'A' && ch <= 'Z')  || ch == '_';
	}
	

	/**
	 * Convenience method to determine if a character is numeric (0-9)
	 * 
	 * @param ch character to test
	 * @return TRUE of the character is numeric, FALSE otherwise
	 */
	private boolean isNumeric(char ch) {
		return (ch >= '0' && ch <= '9');
	}


	/**
	 * Convenience method to determine if a character is alphanumeric
	 * 
	 * @param ch character to test
	 * @return TRUE of the character is alpha or numeric, FALSE otherwise
	 */
	private boolean isAlphaNumeric(char ch) {
		return (isAlpha(ch) || isNumeric(ch));
	}
	

	/**
	 * Convenience method to determine if a character is whitespace
	 * 
	 * @param ch character to test
	 * @return TRUE of the character is "white space", FALSE otherwise
	 */
	private boolean isWhitespace(char ch) {
		return Character.isWhitespace(ch);
	}


	/**
	 * Convenience method to determine if a word is in the list of keywords
	 * for the current database.
	 * 
	 * @param word String of characters to test
	 * @param ignore number of characters at the end to ignore (potential trailing junk)
	 * @return TRUE of the word is a keyword, FALSE otherwise
	 */
	private boolean isAKeyword(String word, int ignore) {
		String temp = word.substring(0, word.length()-ignore).trim().toUpperCase();
		for (int i=0;  i<keywordList.length;  i++) {
			if (keywordList[i].equals(temp)) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Convenience method to determine if a word is in the list of known
	 * functions for the current database.
	 * 
	 * @param word String of characters to test
	 * @param ignore number of characters at the end to ignore (potential trailing junk)
	 * @return TRUE of the word is a keyword, FALSE otherwise
	 */
	private boolean isAFunction(String word, int ignore) {
		String temp = word.substring(0, word.length()-ignore).trim().toUpperCase();
		for (int i=0;  i<functionList.length;  i++) {
			if (functionList[i].equals(temp)) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Convenience method to determine if a word is a number (integer, float, etc)
	 * 
	 * @param word String of characters to test
	 * @param ignore number of characters at the end to ignore (potential trailing junk)
	 * @return TRUE of the word is all numeric, FALSE otherwise
	 */
	@SuppressWarnings("unused")
	private boolean isANumber(String word, int ignore) {
		boolean isaNum = false;
		String tWord = word.substring(0, word.length()-ignore).trim();
		try {
			double tDbl  = Double.parseDouble(tWord);
			isaNum = true;
		} catch (Exception ex) {
		}
		return isaNum;
	}
}
