package sqltool.favorites;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import sqltool.common.SqlToolkit;
import sqltool.config.UserConfig;
import sqltool.query.SqlDocument;
import sqltool.swing.extra.SqlTextPane;


/**
 * Dialog pane for editing a "FavoriteSql" instance.
 * @author wjohnson000
 *
 */
public class SqlEditorUI extends JDialog {

	private static final long serialVersionUID = -4288944797982838269L;

	protected boolean   isOK = true;
	private FavoriteSql model = null;
	private SqlDocument sqlDoc = null;
	
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JButton acceptBtn = new JButton();
	private JButton cancelBtn = new JButton();
	private JTextPane sqlEditor = new SqlTextPane();
	private JPanel jPanel1 = new JPanel();
	private JTextField aliasTF = new JTextField();
	private JLabel jLabel2 = new JLabel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	

	/**
	 * Default constructor, which creates a non-modal dialog
	 */
	public SqlEditorUI() {
		this(null, "SQL Editor", false);
	}

	/**
	 * Constructor for a dialog panel
	 * @param frame parent frame
	 * @param title title of the frame
	 * @param modal dialog's modality
	 */
	public SqlEditorUI(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			isOK = true;
			this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			
			sqlDoc = new SqlDocument();
            sqlDoc.setTextPane(sqlEditor);  // TODO see if this is necessary ...
			sqlEditor.setDocument(sqlDoc);
			checkConfig(UserConfig.FONT_QUERY_EDITOR);
			checkConfig(UserConfig.PARAM_TAB_SPACING);
			pack();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create all of the UI elements
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		this.getContentPane().setLayout(gridBagLayout1);
		acceptBtn.setMargin(new Insets(0, 4, 0, 4));
		acceptBtn.setText("accept");
		acceptBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptBtn_actionPerformed(e);
			}
		});
		cancelBtn.setMargin(new Insets(0, 4, 0, 4));
		cancelBtn.setText("cancel");
		cancelBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelBtn_actionPerformed(e);
			}
		});
		sqlEditor.setText(" ");
		jScrollPane1.setMinimumSize(new Dimension(440, 320));
		jScrollPane1.setPreferredSize(new Dimension(440, 320));
		jLabel2.setText("Alias:");
		aliasTF.setColumns(40);
		jPanel1.setLayout(gridBagLayout2);
		this.getContentPane().add(jPanel1, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
		this.getContentPane().add(jScrollPane1, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 8, 6, 8), 0, 0));
		jScrollPane1.getViewport().add(sqlEditor, null);
		this.getContentPane().add(acceptBtn, new GridBagConstraints(0, 2, 1, 1, 0.5, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 8, 8), 0, 0));
		this.getContentPane().add(cancelBtn, new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 8, 8, 2), 0, 0));
		jPanel1.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(aliasTF, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 0), 0, 0));
	}
	
	/**
	 * When the user selects "ACCEPT", save the new SQL and alias
	 * @param e event
	 */
	void acceptBtn_actionPerformed(ActionEvent e) {
		isOK = true;
		model.sql = sqlEditor.getText();
		model.alias = aliasTF.getText();
		this.dispose();
	}
	
	/**
	 * When the user selects "CANCEL", ignore any SQL or alias changes
	 * @param e event
	 */
	void cancelBtn_actionPerformed(ActionEvent e) {
		isOK = false;
		this.dispose();
	}
	
	/**
	 * Set the item to be edited
	 * @param favSql "FavoriteSql" instance
	 */
	void setModel(FavoriteSql favSql) {
		model = favSql;
		aliasTF.setText(favSql.getAlias());
		sqlDoc.startBulkLoad();
		sqlEditor.setText(favSql.getSql());
		sqlDoc.finishBulkLoad();
		sqlEditor.setCaretPosition(0);
	}

	/**
	 * Update the font and tab-stops if the user has modified them via the
	 * configuration editor
	 * @param alias value that was updated
	 */
	private void checkConfig(String alias) {
		if (UserConfig.FONT_QUERY_EDITOR.equals(alias)) {
			Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_EDITOR);
			if (newFont != null) {
				sqlEditor.setFont(newFont);
			}
		} else if (UserConfig.PARAM_TAB_SPACING.equals(alias)) {
			FontMetrics fm = sqlEditor.getFontMetrics(sqlEditor.getFont());
			int tabSize = SqlToolkit.userConfig.getTabSpacing() *
			fm.stringWidth("X");
			
			TabStop[] tsList = new TabStop[10];
			for (int i=0;  i<tsList.length;  i++) {
				tsList[i] = new TabStop((i+1) * tabSize);
			}
			TabSet tabSet = new TabSet(tsList);
			
			Style curStyle = sqlEditor.getLogicalStyle();
			StyleConstants.setTabSet(curStyle, tabSet);
			sqlEditor.setLogicalStyle(curStyle);
		}
	}
	
}