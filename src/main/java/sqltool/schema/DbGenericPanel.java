package sqltool.schema;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import sqltool.common.FindDialog;
import sqltool.common.SqlToolkit;
import sqltool.common.db.DbInfoModel;
import sqltool.common.db.DbStructure;
import sqltool.config.UserConfig;
import sqltool.query.SqlDocument;
import sqltool.schema.script.*;
import sqltool.swing.extra.SqlTextPane;
import sqltool.table.RowTable;
import sqltool.table.RowTableHeader;
import sqltool.table.RowTableModel;
import sqltool.table.RowTableModelFactory;


/**
 * A panel that manages the standard database information, which is the
 * stuff that can be obtained from the DatabaseMetadata.
 *  
 * @author wjohnson000
 *
 */
public class DbGenericPanel extends JPanel implements Observer {

	static final long serialVersionUID = 1977394965547322501L;

	public static final String DETAIL_SCRIPT = "SCRIPT";
	public static final String DETAIL_COLUMN = "COLUMN";
	public static final String DETAIL_DATA   = "DATA";
	
	public static final String EMPTY_LIST = "-- NONE --";

	
	private boolean       firstTime = true;
	private boolean       haveData  = false;
	private String        modeKey;
	private DbInfoModel   myModel;
	private DbStructure   currDbStruct;
	private SqlDocument   sqlDoc;
	private RowTableModel dataTableModel = new RowTableModel();
	private RowTableModelFactory sqlModelFactory = new RowTableModelFactory();

	private JLabel       infoLabel = new JLabel();
	private JLabel       schemaLabel = new JLabel();
	private JLabel       catalogLabel = new JLabel();
	private JLabel       listLabel = new JLabel();
	private JLabel       detailLabel = new JLabel();
	private JLabel       radioLabel = new JLabel();
	private JComboBox    schemaListCB = new JComboBox();
	private JComboBox    catalogListCB = new JComboBox();
	private JRadioButton scriptRB = new JRadioButton();
	private JRadioButton columnRB = new JRadioButton();
	private JRadioButton dataRB = new JRadioButton();
	private JList        entryList = new JList();
	private JTable       columnTable = new JTable();
	private JTable       dataTable = new RowTable();
	private JTextPane    scriptEditor = new SqlTextPane();
	
	private ButtonGroup  viewBG = new ButtonGroup();
	private JScrollPane  listPane = new JScrollPane();
	private JScrollPane  scriptPane = new JScrollPane();
	private JScrollPane  columnPane = new JScrollPane();
	private JScrollPane  dataPane = new JScrollPane();
	private JPanel       leftPanel = new JPanel();
	private JPanel       rightPanel = new JPanel();
	private JPanel       optionPanel = new JPanel();
	private JSplitPane   entrySplitPane = new JSplitPane();


	/**
	 * You have to specify the "mode" of operation when creating this pane, which
	 * indicates what data is being displayed
	 */
	public DbGenericPanel(String mode) {
		modeKey = mode;
		String labelText = "";
		if (mode == DbInfoModel.MODE_TABLE) {
			labelText = "Table";
		} else if (mode == DbInfoModel.MODE_VIEW) {
			labelText = "View";
		} else if (mode == DbInfoModel.MODE_PROCEDURE) {
			labelText = "Procedure";
			columnRB.setEnabled(false);
			dataRB.setEnabled(false);
		} else if (mode == DbInfoModel.MODE_SYNONYM) {
			labelText= "Synonym";
			columnRB.setEnabled(false);
			dataRB.setEnabled(false);
		} else {
			modeKey = DbInfoModel.MODE_TABLE;
			labelText = "Table";
		}
		
		// See if any of the user configuration parameters have been changed
		SqlToolkit.userConfig.addObserver(this);
		checkConfig(UserConfig.FONT_QUERY_EDITOR);
		checkConfig(UserConfig.PARAM_TAB_SPACING);
		
		// Set all of the fixed text display values
		infoLabel.setText(labelText + " Definitions ...");
		schemaLabel.setText("Schema:");
		catalogLabel.setText("Catalog:");
		listLabel.setText("Entry List");
		detailLabel.setText(labelText + " Details");
		radioLabel.setText("    view:");
		
		infoLabel.setForeground(Color.blue);
		listLabel.setForeground(Color.blue);
		detailLabel.setForeground(Color.blue);
		
		// Configure the table and script editor, and hook up the (table) LIST
		// and (column) TABLE to their scroll panes
//		columnTable.setVisible(false);
		columnTable.setFont(new java.awt.Font("SansSerif", 0, 11));
		columnTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		sqlDoc = new SqlDocument();
		sqlDoc.setTextPane(scriptEditor);  // TODO see if this is necessary ...
		scriptEditor.setDocument(sqlDoc);
		scriptEditor.setEditable(false);

		dataTable.setFont(new java.awt.Font("SansSerif", 0, 11));
		dataTable.setModel(dataTableModel);

		listPane.getViewport().add(entryList, null);
		scriptPane.getViewport().add(scriptEditor, null);
		scriptPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		columnPane.getViewport().add(columnTable, null);
		columnPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		dataPane.getViewport().add(dataTable, null);
		dataPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		scriptRB.setText("script");
		columnRB.setText("columns");
		dataRB.setText("data");
		viewBG.add(scriptRB);
		viewBG.add(columnRB);
		viewBG.add(dataRB);
		scriptRB.doClick();

		catalogListCB.setMaximumRowCount(24);
		schemaListCB.setMaximumRowCount(24);

		// Set up the actions ... for combo box (list of schemas) and
		// list (list of tables), and for the radio buttons
		schemaListCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateEntryList();
			}
		});
		
		catalogListCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateEntryList();
			}
		});
		
		entryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				if (! lse.getValueIsAdjusting()) {
					updateDetailTable();
					updateDetailScript();
//					updateDetailData();
					if (dataRB.isSelected()) {
						haveData = true;
						updateDetailData();
					} else {
						haveData = false;
					}
				}
			}
		});
		
		scriptRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setDetailDisplay(DETAIL_SCRIPT);
			}
		});
		
		columnRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setDetailDisplay(DETAIL_COLUMN);
			}
		});
		
		dataRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (! haveData) {
					haveData = true;
					updateDetailData();
				}
				setDetailDisplay(DETAIL_DATA);
			}
		});

		scriptEditor.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				scriptEditor_keyReleased(e);
			}
		});
		
		// Use panels to manage the right- and left- side of the split panes and
		// add the appropriate controls
		leftPanel.setLayout(new BorderLayout(8, 10));
		leftPanel.add(listLabel, BorderLayout.NORTH);
		leftPanel.add(listPane, BorderLayout.CENTER);
		
		optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		optionPanel.add(detailLabel);
		optionPanel.add(radioLabel);
		optionPanel.add(scriptRB);
		optionPanel.add(columnRB);
		optionPanel.add(dataRB);
		rightPanel.setLayout(new BorderLayout(2, 2));
		rightPanel.add(optionPanel, BorderLayout.NORTH);
		rightPanel.add(scriptPane, BorderLayout.CENTER);
		
		entrySplitPane.setDividerLocation(220);
		entrySplitPane.add(leftPanel, JSplitPane.TOP);
		entrySplitPane.add(rightPanel, JSplitPane.BOTTOM);
		
		// Start adding the components to the main layout panel
		setLayout(new GridBagLayout());
		setLayout(new GridBagLayout());
		add(infoLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(8, 6, 0, 2), 0, 0));
		add(catalogLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(4, 6, 0, 2), 0, 0));
		add(catalogListCB, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(4, 6, 0, 2), 0, 0));
		add(schemaLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(8, 6, 0, 2), 0, 0));
		add(schemaListCB, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(8, 6, 0, 2), 0, 0));
		add(entrySplitPane, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(4, 6, 0, 2), 0, 0));
		
		columnTable.setTableHeader(new RowTableHeader(columnTable.getTableHeader().getColumnModel()));
		updateEntryList();
	}
	
	
	/** The underlying model has changed ... update the view.  This includes setting
	 * the labels for the "catalog" and "schema" terms, populating the list of
	 * catalogs and schemas, enabling them if they are non-empty, and pre-selecting
	 * the current schema as the one to display.
	 * 
	 * @param aModel class which contains DB information
	 */
	public void setModel(DbInfoModel aModel) {
		myModel = aModel;
		
		catalogLabel.setText(myModel.getCatalogTerm());
		schemaLabel.setText(myModel.getSchemaTerm());
		
		catalogListCB.setModel(new DefaultComboBoxModel(myModel.getCatalogList()));
		schemaListCB.setModel(new DefaultComboBoxModel(myModel.getSchemaList()));
		
		catalogListCB.setEnabled(catalogListCB.getItemCount() > 0);
		schemaListCB.setEnabled(schemaListCB.getItemCount() > 0);

		if (schemaListCB.isEnabled()) {
			String curSchema = myModel.getDbUser();
			schemaListCB.setSelectedItem(curSchema);
		} else if (catalogListCB.isEnabled()) {
			String curSchema = myModel.getDbUser();
			catalogListCB.setSelectedItem(curSchema);
		}

		dataTable.setModel(new RowTableModel());
		updateEntryList();
	}
	
	
	/**
	 * A new "Schema" value has been selected ... respond by setting the appropriate
	 * list based on the schema and the mode
	 */
	private void updateEntryList() {
		String catalog = (String)catalogListCB.getSelectedItem();
		String schema = (String)schemaListCB.getSelectedItem();
		if (catalog == null  &&  schema == null) {
			return;
		}

		Cursor saveCursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		currDbStruct = myModel.getDbStructure(catalog, schema, modeKey);
		if (currDbStruct == null  ||  currDbStruct.getNameCount() == 0) {
			listLabel.setText(("Entry List  (count=0)"));
			entryList.setListData(new String[] { EMPTY_LIST });
		} else {
			listLabel.setText(("Entry List  (count=" + currDbStruct.getNameCount() + ")"));
			entryList.setListData(currDbStruct.getNames());
		}
		updateDetailTable();
		updateDetailScript();
		if (dataRB.isSelected()) {
			haveData = true;
			updateDetailData();
		} else {
			haveData = false;
		}
		setCursor(saveCursor);
	}


	/**
	 * A new entry has been selected ... update the "SCRIPT" view option
	 */
	private void updateDetailScript() {
		String script = "";
		ScriptGenerator scGen = getScriptGenerator();
		
		// If we have a *real* entry name, set up the model data
		String entryName = (String)entryList.getSelectedValue();
		if (currDbStruct == null  ||  entryName == null  ||  entryName.equalsIgnoreCase(EMPTY_LIST)) {
			script = "";
		} else {
			Cursor saveCursor = getCursor();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			String catalog = (String)catalogListCB.getSelectedItem();
			String schema = (String)schemaListCB.getSelectedItem();
			myModel.populateDetails(catalog, schema, modeKey, entryName);

			if (modeKey == DbInfoModel.MODE_TABLE) {
				script = scGen.getTableDef(catalog, schema, entryName);
			} else if (modeKey == DbInfoModel.MODE_VIEW) {
				script = scGen.getViewDef(catalog, schema, entryName);
			} else if (modeKey == DbInfoModel.MODE_PROCEDURE) {
				script = scGen.getProcedureDef(catalog, schema, entryName);
			} else if (modeKey == DbInfoModel.MODE_SYNONYM) {
				script = scGen.getSynonymDef(catalog, schema, entryName);
			} else {
				script = scGen.getTableDef(catalog, schema, entryName);
			}
			setCursor(saveCursor);
		}
		sqlDoc.startBulkLoad();
		scriptEditor.setText(script);
		sqlDoc.finishBulkLoad();
		scriptEditor.setCaretPosition(0);
	}


	/**
	 * A new entry has been selected ... update the "COLUMN" view option
	 */
	private void updateDetailTable() {
		String     entryName = (String)entryList.getSelectedValue();
		int[]      colWidth = new int[0];
		String[]   colNames;  // = currDbStruct.columns;
		String[][] tblData  = new String[0][0];
		
		colNames = new String[] {
				"ORDINAL_POSITION", "COLUMN_NAME", "TYPE_NAME",
				"COLUMN_SIZE", "DECIMAL_DIGITS", "IS_NULLABLE"
		};
		
		// Set up the preferred column widths (or retrieve them if previously set ...)
		if (firstTime) {
			colWidth = new int[colNames.length];
			for (int col=0;  col<colWidth.length;  col++) {
				colWidth[col] = colNames[col].length();
			}
		} else {
			TableColumnModel tcm = columnTable.getColumnModel();
			int colCnt = tcm.getColumnCount();
			colWidth = new int[colCnt];
			for (int i = 0; i < colCnt; i++) {
				TableColumn tc = tcm.getColumn(i);
				colWidth[i] = tc.getPreferredWidth();
			}
		}
		
		// If we have a *real* entry name, set up the model data
		if (! (entryName == null  ||  currDbStruct == null  ||
				EMPTY_LIST.equalsIgnoreCase(entryName))) {
			String catalog = (String)catalogListCB.getSelectedItem();
			String schema = (String)schemaListCB.getSelectedItem();
			myModel.populateDetails(catalog, schema, modeKey, entryName);
			List<Map<String,String>> detailList = currDbStruct.getEntry(entryName);
			if (detailList != null) {
				tblData = new String[detailList.size()][colNames.length];
				for (int row=0;  row<detailList.size();  row++) {
					Map<String,String> hmTemp = detailList.get(row);
					for (int col=0;  col<colNames.length;  col++) {
						tblData[row][col] = hmTemp.get(colNames[col]);
						if (tblData[row][col] != null  &&  tblData[row][col].length() > colWidth[col]) {
							colWidth[col] = tblData[row][col].length();
						}
					}
				}
			}
		}
		
		// Create and set the new table model
		DefaultTableModel dtm = new DefaultTableModel(tblData, colNames);
		columnTable.setModel(dtm);
		
		// Play around with the column widths for fun
		TableColumnModel tcm = columnTable.getColumnModel();
		int colCnt = tcm.getColumnCount();
		for (int i = 0; i < colCnt; i++) {
			TableColumn tc = tcm.getColumn(i);
			int cWid = (firstTime) ? Math.min((colWidth[i] + 2) * 5, 400) : colWidth[i];
			tc.setPreferredWidth(cWid);
		}
		
		firstTime = false;
	}


	/**
	 * A new entry has been selected ... update the "DATA" view option
	 */
	private void updateDetailData() {
		String entryName = (String)entryList.getSelectedValue();
		String query = "Select * from " + entryName;
		if (! (entryName == null  ||  currDbStruct == null  ||
				EMPTY_LIST.equalsIgnoreCase(entryName))) {
			Cursor saveCursor = getCursor();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			dataTableModel = sqlModelFactory.createModelData(myModel.getConnection(), query, null, false, 2000);
			
			dataTable.setModel(dataTableModel);

			// Adjust the column widths when the data is retrieved
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					while (sqlModelFactory.isActive()) {
						try { Thread.sleep(250); } catch (Exception ex) {}
					}

					// Do a little fun re-sizing of the table columns
					TableColumnModel tcm = dataTable.getColumnModel();
					int colCnt = tcm.getColumnCount();
					for (int i=0;  i<colCnt;  i++) {
						TableColumn tc = tcm.getColumn(i);
						int colWidth = dataTableModel.getCharacterWidth(i);
						tc.setPreferredWidth(Math.min( (colWidth + 2) * 5, 400));
					}
				}
			});

			setCursor(saveCursor);
		}
	}


	/**
	 * Show the underlying data depending on how the user wants to see it
	 * 
	 * @param type view (SCRIPT, COLUMN, DATA) that is to be displayed
	 */
	private void setDetailDisplay(String type) {
		if (DETAIL_SCRIPT.equalsIgnoreCase(type)) {
			rightPanel.remove(columnPane);
			rightPanel.remove(dataPane);
			rightPanel.add(scriptPane, BorderLayout.CENTER);
		} else if (DETAIL_COLUMN.equalsIgnoreCase(type)) {
			rightPanel.remove(scriptPane);
			rightPanel.remove(dataPane);
			rightPanel.add(columnPane, BorderLayout.CENTER);
		} else {
			rightPanel.remove(scriptPane);
			rightPanel.remove(columnPane);
			rightPanel.add(dataPane, BorderLayout.CENTER);
		}
		rightPanel.setVisible(false);  // This is done to force a re-draw to show the new data
		rightPanel.setVisible(true);   // <same as previous statement>
	}


	/**
	 * Get notified whenever the user config changes
	 */
	public void update(Observable source, Object arg) {
		if (source == SqlToolkit.userConfig) {
			checkConfig((String)arg);
		}
	}
	
	/** 
	 * This method is registered as a listener to be called whenever the user
	 * configuration is changed
	 * 
	 * @param alias indicated what has been changed
	 */
	private void checkConfig(String alias) {
		if (UserConfig.FONT_QUERY_EDITOR.equals(alias)) {
			Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_EDITOR);
			if (newFont != null) {
				scriptEditor.setFont(newFont);
			}
		} else if (UserConfig.FONT_QUERY_RESULT.equals(alias)) {
			Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_RESULT);
			if (newFont != null) {
				dataTable.setFont(newFont);
			}
		} else if (UserConfig.PARAM_TAB_SPACING.equals(alias)) {
			FontMetrics fm = scriptEditor.getFontMetrics(scriptEditor.getFont());
			int tabSize = SqlToolkit.userConfig.getTabSpacing() *
			fm.stringWidth("X");
			
			TabStop[] tsList = new TabStop[10];
			for (int i=0;  i<tsList.length;  i++) {
				tsList[i] = new TabStop((i+1) * tabSize);
			}
			TabSet tabSet = new TabSet(tsList);
			
			Style curStyle = scriptEditor.getLogicalStyle();
			StyleConstants.setTabSet(curStyle, tabSet);
			scriptEditor.setLogicalStyle(curStyle);
			((SqlTextPane)scriptEditor).adjustTabSpacing();
		}
	}


	/**
	 * We cheat--just a little--and display some Oracle-specific stuff based on
	 * the "OracleGenerator" if we are talking to an Oracle database; otherwise
	 * use the "DefaultGenerator" which can't show any details
	 * 
	 * @return
	 */
	private ScriptGenerator getScriptGenerator() {
		ScriptGenerator scGen = null;
		if (myModel != null  &&  myModel.getDriverName().toLowerCase().indexOf("oracle") > -1) {
			scGen = new OracleGenerator();
		} else {
			scGen = new DefaultScriptGenerator();
		}
		scGen.setDbInfoModel(myModel);
		return scGen;
	}


	/**
	 * Keystroke of "CTRL-F" will bring up the "FIND" dialog
	 * @param e
	 */
	void scriptEditor_keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F  &&  e.getModifiers() == KeyEvent.CTRL_MASK) {
			showFindDialog();
		}
	}


	/**
	 * Display the FIND dialog for doing text search/replace operations
	 */
	private void showFindDialog() {
		FindDialog findDialog = SqlToolkit.getFindDialog(this);
		findDialog.setTarget(scriptEditor);
		findDialog.setVisible(true);
	}

}
