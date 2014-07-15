package sqltool.swing.extra;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//import sqltool.query.LineNumberBorder;
import sqltool.common.SqlToolkit;
import sqltool.query.LineNumberDocument;
import sqltool.query.SqlDocument;


/**
 * This class only accepts documents of type "SqlDocument" and implements
 * smart horizontal scrolling, a feature not readily available with a
 * regular TextPane.
 * 
 * @author wjohnson000
 */
public class SqlTextPane extends JTextPane {

	static final long serialVersionUID = -6563550305027630696L;

	int    docLineCount  = 0;
	String docLongestLine = "";
	int prefSizeWidth = 0;
	JTextPane rowPane = null;

	/**
	 * Default constructor remains unchanged ...
	 */
	public SqlTextPane() {
		super();
	}

	/**
	 * New constructor takes a 'SqlDocument' instance; we need to add
	 * the document listeners to determine when the scroll bars should
	 * appear
	 * 
	 * @param doc sqltool.query.SqlDocument instance to be displayed in
	 *            this pane
	 */
	public SqlTextPane(SqlDocument doc) {
		super(doc);
		doc.setTextPane(this);
		addDocumentListeners();
	}

	/**
	 * 
	 */
	public JTextPane getRowPane() {
		return rowPane;
	}


	/**
	 * Override the "setDocument()" method to ensure that what we get is
	 * a 'SqlDocument' instance.  If it isn't, we throw it away and
	 * create a new default document of the appropriate type.  Then add
	 * the document listeners to determine when the scroll bars should
	 * appear
	 * 
	 * @param doc sqltool.query.SqlDocument instance to be displayed in
	 *            this pane
	 */
	public void setDocument(Document doc) {
		if (! (doc instanceof SqlDocument)) {
			doc = new SqlDocument();
		}
		super.setDocument(doc);
		addDocumentListeners();
	}

	/**
	 * Override the "replaceSelection(String content)" method.  This will
	 * allow us to ensure that we restore the current text location
	 */
	public void replaceSelection(String content) {
		// Save the current cursor and replace it with the WAIT cursor
//		Cursor saveCursor = this.getCursor();
//		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		// Call the parent method to do the actual work
		super.replaceSelection(content);

		// Get the vertical scroll bar and restore the value (scroll
		// location); it needs to be done after all other updates are
		// complete, so use "SwingUtilities.invokeLater(...)"
//		JScrollPane scrollPane = getScrollPane();
//		if (scrollPane != null) {
//			final javax.swing.JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
//			if (scrollBar != null) {
//				final int val = scrollBar.getValue();
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						scrollBar.setValue(val);
//					}
//				});
//			}
//		}

		// Restore the cursor
//		setCursor(saveCursor);
	}

	/**
	 * Override the "getScrollableTracksViewportWidth()" method.  Usually this
	 * returns true when the pane sits inside of a scrollable pane--regardless
	 * of how long the text is--leading to text that's always wrapped.  We
	 * check the viewport size against the preferred width: if the former is
	 * larger then we don't want scrolling; if the former is smaller then we
	 * do want scrolling.
	 */
	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			if (getParent().getWidth() > prefSizeWidth) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Override the "setFont()" method so we can apply the same font to the line
	 * number pane.  We want to force it to update even though the text hasn't
	 * changed.
	 * 
	 * @param font the new font being applied to the main document pane.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (rowPane != null) {
			rowPane.setFont(font);
			updateDocumentSize(true);
		}
		JScrollPane scrollPane = getScrollPane();
		if (scrollPane != null) {
			scrollPane.getVerticalScrollBar().setUnitIncrement(14);
		}
	}

	/**
	 * Create a "setTabSpacing" method so we can re-calculate the width of the
	 * largest line.  We can get the new tab spacing so we don't need it
	 * to be passed in.
	 */
	public void adjustTabSpacing() {
		updateDocumentSize(true);
	}

	/**
	 * Create an instance of a anonymous listener to process all document
	 * updates.  We don't want to force an update, so pass "false" as the
	 * parameter to the method looking for the updates.  NOTE:  the
	 * 'sqltool.query.SqlDocument' class will set a flag when it's done
	 * calculating the number of lines and the longest line, and will then
	 * re-issues a "CHANGE" document event.  So that's the only one we'll
	 * listen for.
	 */
	private void addDocumentListeners() {
		getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent de) {
				updateDocumentSize(false);
			}
			public void insertUpdate(DocumentEvent de) {
			}
			public void removeUpdate(DocumentEvent de) {
			}
		});
	}

	/**
	 * A convenience method to get the "JScrollPane" in which this pane
	 * resides.
	 */
	private JScrollPane getScrollPane() {
		Component par = getParent();
		while (par != null) {
			if (par instanceof JScrollPane) {
				return (JScrollPane)par;
			}
			par = par.getParent();
		}
		return null;
	}

	/**
	 * All document listener methods funnel in here.  We know that our
	 * document is of type 'sqltool.query.SqlDocument'.
	 * 
	 * Use the "getLineCount()" method to get the number of lines in
	 * the document and update the line-number pane if necessary.
	 * 
	 * Use the "getLongestLine() method to get the longest line in the
	 * document.  If a change has occurred replace all TABs with four
	 * spaces [sorta' random choice], add three characters of padding
	 * just to be safe, and calculate the length of that line based on
	 * the current font metrics.  That now becomes the preferred width.
	 */
	private void updateDocumentSize(boolean force) {
		SqlDocument sqlDoc = (SqlDocument)getDocument();
		if (sqlDoc != null  &&  sqlDoc.isSafeToProcess()) {
			// If there is a change to the line count, update the "ROW" header
			// pane which contains the line numbers
			int lineCount = sqlDoc.getLineCount();
			if (lineCount != docLineCount) {
				if (rowPane == null  &&  getParent() != null  &&
						getParent().getParent() != null  &&
						getParent().getParent() instanceof JScrollPane) {
					createRowPane();
				}
				if (rowPane != null) {
					docLineCount = lineCount;
					rowPane.setText(getLineNumberText());
				}
			}

			// If there is a change to the maximum line, set the preferred
			// size which will help control whether we need to have scrollbars
			// or not
			String maxLine = sqlDoc.getLongestLine();
			FontMetrics fm = getFontMetrics(getFont());
			if (fm != null  &&  maxLine != null  &&  maxLine.length() > 0  &&
					(force  ||  maxLine.length() != docLongestLine.length())) {
				docLongestLine = maxLine;
				String padding = "";
				for (int i=0;  i<SqlToolkit.userConfig.getTabSpacing();  i++) {
					padding += " ";
				}
				int newPrefWidth = fm.stringWidth(docLongestLine.replaceAll("\t", padding)+"PADD");
				if (newPrefWidth != prefSizeWidth) {
					prefSizeWidth = newPrefWidth;
					setPreferredSize(new Dimension(prefSizeWidth, 1000));
				}
			}
		}
	}

	/**
	 * This gets called once, only after the main document has been set and this
	 * element has been added to a JScrollPane.
	 */
	private void createRowPane() {
		LineNumberDocument rowDoc = new LineNumberDocument();
		rowPane = new JTextPane();
		rowPane.setDocument(rowDoc);
		rowPane.setFont(getFont());
		rowPane.setEnabled(false);
		rowPane.setBackground(new Color(240, 240, 240));
		((JScrollPane)getParent().getParent()).setRowHeaderView(rowPane);
	}

	/**
	 * Generate the contents of the line number pane.  Use a minimum of three
	 * characters to display line number, with no maximum.
	 * @return
	 */
	private String getLineNumberText() {
		StringBuffer sb = new StringBuffer(docLineCount*5);
		int len = (""+docLineCount).length();
		len = Math.max(len, 3);
		for (int i=1;  i<=docLineCount;  i++) {
			String lineNum = "" + i;
			sb.append(" ");
			for (int j=lineNum.length();  j<len;  j++) {
				sb.append("0");
			}
			sb.append(lineNum);
			sb.append(" ");
			if (i < docLineCount) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
