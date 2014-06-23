package sqltool.schema;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import sqltool.common.FindDialog;
import sqltool.common.MenuHandler;
import sqltool.common.MenuManager;
import sqltool.common.SqlToolkit;
import sqltool.common.db.DbCustomModel;
import sqltool.common.db.DbInfoModel;
import sqltool.config.UserConfig;
import sqltool.query.SqlDocument;
import sqltool.swing.extra.SqlTextPane;


/**
 * Panel that manages all of the "custom" stuff about a particular database,
 * i.e., stuff that doesn't come from the standard DatabaseMetadata.  It in
 * turn relies on a "DbInfoModel" to provide data.
 * 
 * @author wjohnson000
 *
 */
public class DbCustomPanel extends JPanel implements Observer, MenuHandler {

    private static final long serialVersionUID = 5103070642214618710L;

    //	============================================================================
    //	Define menu option names ...
    //	============================================================================
    static final String MENU_EXPORT_SCHEMA  = "export schema";

    public static final String DETAIL_SOURCE = "SCRIPT";
    public static final String DETAIL_CREATE = "COLUMN";
    public static final String DETAIL_DROP   = "DATA";

    public static final String EMPTY_LIST = "-- NONE --";

    private SqlDocument   sqlDoc;
    private DbInfoModel   myModel;

    private JLabel       infoLabel = new JLabel();
    private JLabel       schemaLabel = new JLabel();
    private JLabel       catalogLabel = new JLabel();
    private JLabel       datatypeLabel = new JLabel();
    private JLabel       listLabel = new JLabel();
    private JLabel       detailLabel = new JLabel();
    private JLabel       radioLabel = new JLabel();
    private JComboBox    schemaListCB = new JComboBox();
    private JComboBox    catalogListCB = new JComboBox();
    private JComboBox    datatypeListCB = new JComboBox();
    private JRadioButton sourceRB = new JRadioButton();
    private JRadioButton createRB = new JRadioButton();
    private JRadioButton dropRB = new JRadioButton();
    private JList        entryList = new JList();
    private JTextPane    scriptEditor = new SqlTextPane();

    private ButtonGroup  viewBG = new ButtonGroup();
    private JScrollPane  listPane = new JScrollPane();
    private JScrollPane  scriptPane = new JScrollPane();
    private JPanel       leftPanel = new JPanel();
    private JPanel       rightPanel = new JPanel();
    private JPanel       optionPanel = new JPanel();
    private JSplitPane   entrySplitPane = new JSplitPane();


    /**
     * Constructor, which creates and lays out all of the UI elements
     */
    public DbCustomPanel() {
        final MenuHandler mHandler = this;
        String labelText = "Database ...";

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

        sqlDoc = new SqlDocument();
        scriptEditor.setDocument(sqlDoc);
        scriptEditor.setEditable(false);

        listPane.getViewport().add(entryList, null);
        scriptPane.getViewport().add(scriptEditor, null);
        scriptPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        sourceRB.setText("source");
        createRB.setText("create");
        dropRB.setText("drop");
        viewBG.add(sourceRB);
        viewBG.add(createRB);
        viewBG.add(dropRB);
        sourceRB.doClick();

        catalogListCB.setMaximumRowCount(24);
        schemaListCB.setMaximumRowCount(24);
        datatypeListCB.setMaximumRowCount(24);

        // Set up the actions ... for combo boxex (list of schemas, catalogs or
        // data types) and list (list of tables), and for the radio buttons
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

        datatypeListCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateEntryList();
            }
        });

        entryList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
                if (! lse.getValueIsAdjusting()) {
                    updateDetailScript();
                }
            }
        });
        entryList.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                SqlToolkit.menuManager.setCurrentOwner(mHandler);
            }
        });
        entryList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e);
            }
        });

        sourceRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setDetailDisplay(DETAIL_SOURCE);
            }
        });

        createRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setDetailDisplay(DETAIL_CREATE);
            }
        });

        dropRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setDetailDisplay(DETAIL_DROP);
            }
        });

        scriptEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                scriptEditor_keyReleased(e);
            }
        });
        scriptEditor.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                SqlToolkit.menuManager.setCurrentOwner(mHandler);
            }
        });
        scriptEditor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e);
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
        optionPanel.add(sourceRB);
        optionPanel.add(createRB);
        optionPanel.add(dropRB);
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
        add(datatypeLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(8, 6, 0, 2), 0, 0));
        add(datatypeListCB, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(8, 6, 0, 2), 0, 0));
        add(entrySplitPane, new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(4, 6, 0, 2), 0, 0));

        buildOptionPopupMenu();
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

        DbCustomModel dbCustom = myModel.getCustomModel();
        if (dbCustom != null) {
            datatypeListCB.setModel(new DefaultComboBoxModel(dbCustom.getDataTypeNames()));
            datatypeListCB.setEnabled(datatypeListCB.getItemCount() > 0);
        }

        updateEntryList();
    }

    /* (non-Javadoc)
     * @see sqltool.common.MenuHandler#handleMenuEvent(java.awt.event.ActionEvent)
     */
    @Override
    public void handleMenuEvent(ActionEvent ae) {
        if (ae.getSource() instanceof JMenuItem) {
            JMenuItem mi = (JMenuItem)ae.getSource();
            if (mi.getText().equalsIgnoreCase(MENU_EXPORT_SCHEMA)) {
                exportSchema();
            }
        }
    }

    /* (non-Javadoc)
     * @see sqltool.common.MenuHandler#becomeActive()
     */
    @Override
    public void becomeActive() {
        SqlToolkit.menuManager.setCurrentOwner(this);
    }

    /**
     *  Build the pop-up menu for saving the table data ...
     */
    protected void buildOptionPopupMenu() {
        SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_EXPORT_SCHEMA);
    }

    /**
     * A new "Schema" value has been selected ... respond by setting the appropriate
     * list based on the schema and the mode
     */
    private void updateEntryList() {
        String catalog = (String)catalogListCB.getSelectedItem();
        String schema = (String)schemaListCB.getSelectedItem();
        String dataType = (String)datatypeListCB.getSelectedItem();
        if ((catalog == null  &&  schema == null)  || dataType == null) {
            return;
        }

        DbCustomModel dbCustom = myModel.getCustomModel();
        if (dbCustom != null) {
            String[] entries = dbCustom.getEntries(catalog, schema, dataType);
            if (entries == null  ||  entries.length == 0) {
                listLabel.setText(("Entry List  (count=0)"));
                entryList.setListData(new String[] { EMPTY_LIST });
            } else {
                listLabel.setText(("Entry List  (count=" + entries.length + ")"));
                entryList.setListData(entries);
            }

            // See if the "create" and "drop" radio-buttons should be disabled
            createRB.setEnabled(dbCustom.canCreate(dataType));
            dropRB.setEnabled(dbCustom.canDrop(dataType));
            if (createRB.isSelected()  &&  ! createRB.isEnabled()) {
                sourceRB.setSelected(true);
            } else if (dropRB.isSelected()  &&  ! dropRB.isEnabled()) {
                sourceRB.setSelected(true);
            }
        }
    }


    /**
     * A new entry has been selected ... update the "SCRIPT" view option
     */
    private void updateDetailScript() {
        if (sourceRB.isSelected()) {
            setDetailDisplay(DETAIL_SOURCE);
        } else if (createRB.isSelected()) {
            setDetailDisplay(DETAIL_CREATE);
        } else if (dropRB.isSelected()) {
            setDetailDisplay(DETAIL_DROP);
        } else {
            setDetailDisplay(DETAIL_SOURCE);
        }
    }


    /**
     * Show the underlying data depending on how the user wants to see it
     * 
     * @param type view (SOURCE, CREATE, DROP) that is to be displayed
     */
    private void setDetailDisplay(String type) {
        String catalog = (String)catalogListCB.getSelectedItem();
        String schema = (String)schemaListCB.getSelectedItem();
        String dataType = (String)datatypeListCB.getSelectedItem();
        String entryName = (String)entryList.getSelectedValue();

        DbCustomModel dbCustom = myModel.getCustomModel();
        if (dbCustom == null  ||  entryName == null  ||  entryName.equalsIgnoreCase(EMPTY_LIST)) {
            scriptEditor.setText("");
        } else {
            String script = "";
            sqlDoc.startBulkLoad();	
            if (DETAIL_SOURCE.equalsIgnoreCase(type)) {
                script = dbCustom.getSource(catalog, schema, dataType, entryName);
            } else if (DETAIL_CREATE.equalsIgnoreCase(type)) {
                script = dbCustom.getCreate(catalog, schema, dataType, entryName);
            } else {
                script = dbCustom.getDrop(catalog, schema, dataType, entryName);
            }
            scriptEditor.setText(script);
            sqlDoc.finishBulkLoad();
            scriptEditor.setCaretPosition(0);
        }
        //		rightPanel.setVisible(false);  // This is done to force a re-draw to show the new data
        //		rightPanel.setVisible(true);   // <same as previous statement>
    }


    /**
     * Get notified whenever the user config changes
     */
    public void update(Observable source, Object arg) {
        if (source == SqlToolkit.userConfig) {
            checkConfig(arg==null ? "" : arg.toString());
        }
    }

    /** 
     * This method is registered as a listener to be called whenever the user
     * configuration is changed.  Update the "script editor" object with the
     * new font or the new tab-stop setting
     * 
     * @param alias indicated what has been changed
     */
    private void checkConfig(String alias) {
        if (UserConfig.FONT_QUERY_EDITOR.equals(alias)) {
            Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_EDITOR);
            if (newFont != null) {
                scriptEditor.setFont(newFont);
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

    /**
     * Display the pop-up menu in the correct location.  Different OS-es have
     * pop-up triggers with either mouse-pressed or mouse-released, so we
     * must check both events
     */
    private void checkOptionMenu(MouseEvent e) {
        SqlToolkit.menuManager.setCurrentOwner(this);
        JPopupMenu optionMenu = SqlToolkit.menuManager.getPopup();
        if (e.isPopupTrigger()  &&  optionMenu != null) {
            optionMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Export all data from the selected table
     */
    private void exportSchema() {
        String catalog = (String)catalogListCB.getSelectedItem();
        String schema = (String)schemaListCB.getSelectedItem();
        DbCustomModel dbCustom = myModel.getCustomModel();

        PrintWriter pw = null;
        try {
            // Bring up a file dialog to get the file path
            String path = SqlToolkit.getFilePath(this, "Choose File", "", JFileChooser.SAVE_DIALOG, "Save");
            if (path != null  &&  dbCustom != null) {
                pw = new PrintWriter(new File(path), "UTF-8");
                pw.println("CATALOG: " + catalog);
                pw.println("SCHEMA: " + schema);
                for (int i=0;  i<datatypeListCB.getItemCount();  i++) {
                    String datatype = (String)datatypeListCB.getItemAt(i);
                    String[] entries = dbCustom.getEntries(catalog, schema, datatype);
                    pw.println();
                    pw.println("==========================================================================");
                    pw.println("TYPE: " + datatype + ", COUNT: " + (entries == null ? 0 : entries.length));
                    pw.println("==========================================================================");
                    for (String entry : entries) {
                        String source = dbCustom.getSource(catalog, schema, datatype, entry);
                        pw.println();
                        pw.println(">> " + entry + " <<");
                        pw.println();
                        pw.println(source);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("EX: " + ex);
        } finally {
            if (pw != null) try { pw.close(); } catch (Exception ex) { }
        }
    }
}
