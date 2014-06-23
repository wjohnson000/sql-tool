package sqltool.schema;

import java.awt.*;
import javax.swing.*;

import sqltool.common.db.DbInfoModel;


/**
 * Simple panel that displays basic data about a database connection
 * @author wjohnson000
 *
 */
public class InfoPanel extends JPanel {

	private static final long serialVersionUID = -1915227540765449132L;

	private static final Rectangle TOP_OF_PANE = new Rectangle(0, 0, 1, 1);
	
	private JTextArea   textArea   = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane();
	private DbInfoModel myModel    = null;

	/**
	 * Create the panel and lay out the UI components
	 */
	public InfoPanel() {
		setLayout(new BorderLayout());
		textArea.setText("<no messages>");
		textArea.setDisabledTextColor(Color.darkGray);
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Monospaced", 0, 12));
		scrollPane.getViewport().add(textArea, null);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Set a new model when a new database is selected
	 * @param aModel new database model
	 */
	public void setModel(DbInfoModel aModel) {
		myModel = aModel;
		textArea.setText("Database Metadata Details ...\n=============================");
		if (myModel == null  ||  ! aModel.isValid()) {
			textArea.append("\n\nUnable to connect to database for meta-data ...");
			return;
		}
		
		String[] schemaList = myModel.getSchemaList();
		textArea.append("\n       Name: " + myModel.getDbName());
		textArea.append("\n    Version: " + prettify(myModel.getDbVersion(), 13));
		textArea.append("\n     Driver: " + myModel.getDriverName());
		textArea.append("\n        URL: " + myModel.getDbURL());
		textArea.append("\n       User: " + myModel.getDbUser());
		textArea.append("\n     Schema: " + "count="+ schemaList.length);
		for (int i=0;  i<schemaList.length;  i++) {
			textArea.append("\n             " + schemaList[i]);
		}
		textArea.setCaretPosition(0);
		textArea.scrollRectToVisible(TOP_OF_PANE);
	}
	
	/**
	 * Little used method to make some text look a little prettier
	 * @param text text to munge
	 * @param indent number of spaces to indent
	 * @return formatted string
	 */
	private String prettify(String text, int indent) {
		StringBuffer sb = new StringBuffer("\n");
		for (int i=0;  i<indent;  i++) {
			sb.append(" ");
		}
		return (text == null) ? "" : text.replaceAll("\\n", sb.toString());
	}
}
