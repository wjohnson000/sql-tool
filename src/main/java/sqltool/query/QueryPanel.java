package sqltool.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import sqltool.TabParentPanel;
import sqltool.common.ConnectionManager;
import sqltool.common.FindDialog;
import sqltool.common.MenuHandler;
import sqltool.common.MenuManager;
import sqltool.common.SqlToolkit;
import sqltool.common.db.DbInfoCache;
import sqltool.common.db.DbInfoModel;
import sqltool.config.UserConfig;
import sqltool.server.DbDefinition;
import sqltool.swing.extra.SqlTextPane;
import sqltool.swing.extra.renderer.ClobRenderer;
import sqltool.swing.extra.renderer.DecimalRenderer;
import sqltool.table.RowTable;
import sqltool.table.RowTableModel;
import sqltool.table.RowTableModelFactory;


/**
 * This is the main UI class for the query panel, which includes the SQL query, the
 * tabular results of running the query, and a message of what went right and what
 * went wrong.
 *  
 * @author wjohnson000
 *
 */
public class QueryPanel extends TabParentPanel {

    static final long serialVersionUID = 8069061588773603572L;

    //	============================================================================
    //	Define tab panel names ..,
    //	============================================================================
    private static final String QUERY_TAB   = "query";
    private static final String RESULTS_TAB = "results";
    private static final String MESSAGE_TAB = "message";
    private static final String MERGE_TAB   = "query/results";

    //	============================================================================
    //	Define menu option names ...
    //	============================================================================
    static final String MENU_SEPARATE_PANE  = "separate query/results tab";
    static final String MENU_COMBINE_PANE   = "combine query/results tab";
    static final String MENU_EDIT_COPY      = "copy  [ctrl+c]";
    static final String MENU_EDIT_PASTE     = "paste [ctrl+v]";
    static final String MENU_EDIT_SELECT    = "select all [ctrl+a]";
    static final String MENU_EDIT_FIND      = "find/replace ... [ctrl-f]";
    static final String MENU_CLEAR_QUERY    = "clear query";
    static final String MENU_RUN_QUERY      = "run query  [F7]";
    static final String MENU_RUN_MULTI      = "run multiple  [F9]";
    static final String MENU_RUN_FROM_FILE  = "run from file ...";
    static final String MENU_SAVE_QUERY     = "save query ...";
    static final String MENU_SAVE_RESULTS   = "save results ...";
    static final String MENU_ROW_SELECTION  = "row selection";
    static final String MENU_COL_SELECTION  = "column selection";
    static final String MENU_CELL_SELECTION = "cell selection";
    static final String MENU_CONN_COMMITON  = "auto-commit on";
    static final String MENU_CONN_COMMITOFF = "auto-commit off";
    static final String MENU_CONN_COMMIT    = "db commit";
    static final String MENU_CONN_ROLLBACK  = "db rollback";


    //	============================================================================
    //	Instance variables ...
    //	  currentQuery:  query number in the list of queries
    //	  panelLocationSave:  save location of divider in combined QUERY/RESULTS tab
    //	  isQueryResultsSplit:  are QUERY and RESULTS tabs combined or separate
    //	  currentTab:  what tab is currently selected
    //    prevDir:  previous directory of query file
    //	  resultTable:  tabular results of most recent query
    //	  queryHistory:  list of all queries run in this panel
    //	  sqlDoc:  sql query document, with pretty colors
    //	  resultTableModel:  model associated with the "resultTable" instance
    //	  sqlModelFactory:  factory that builds the "resultTableModel" instance
    //	============================================================================
    private int           currentQuery = 0;
    private int           paneLocationSave = 0;
    private boolean       isQueryResultsSplit = true;
    private String        currentTabName = null;
    private String        prevDir = null;
    private JTable        resultTable = new RowTable();
    private ArrayList<String> queryHistory = new ArrayList<String>(10);
    private SqlDocument   sqlDoc = null;
    private RowTableModel resultTableModel = new RowTableModel();
    private RowTableModelFactory sqlModelFactory = new RowTableModelFactory();

    //	============================================================================
    //	UI components
    //	============================================================================
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel jPanel1 = new JPanel();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JLabel jLabel1 = new JLabel();
    private JComboBox serverListCB = new JComboBox();
    private JScrollPane queryPane = new JScrollPane();
    private JScrollPane resultPane = new JScrollPane();
    private JScrollPane messagePane = new JScrollPane();
    private JTextPane queryEditor = new SqlTextPane();
    private JTextArea messageArea = new JTextArea();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel iconPanel = new JPanel();
    private JButton redBtn = new JButton();
    private JButton yellowBtn = new JButton();
    private JButton greenBtn = new JButton();
    private JPanel jPanel3 = new JPanel();
    private JButton histLastBtn = new JButton();
    private JButton histNextBtn = new JButton();
    private JButton histPrevBtn = new JButton();
    private JButton histFirstBtn = new JButton();
    private JSplitPane querySplitPane = new JSplitPane();
    private JCheckBox autoCommitCB = new JCheckBox();


    /**
     * Only constructor ... create the UI and set all default values and 
     * behaviors
     */

    public QueryPanel() {
        try {
            createUI();
            makeSeparateQueryResultTab();
            buildOptionPopupMenu();
            SqlToolkit.userConfig.addObserver(this);
            sqlDoc = new SqlDocument();
            sqlDoc.setTextPane(queryEditor);  // TODO see if this is necessary ...
            queryEditor.setDocument(sqlDoc);
            queryEditor.enableInputMethods(false);  // allow plain-text only; no fancy characters
            checkConfig(UserConfig.FONT_QUERY_EDITOR);
            checkConfig(UserConfig.FONT_QUERY_RESULT);
            checkConfig(UserConfig.PARAM_TAB_SPACING);
            updateServerList();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Create the UI by making and laying out all components
     * @throws Exception
     */
    void createUI() throws Exception {
        final MenuHandler mHandler = this;
        serverListCB.setMaximumRowCount(24);

        this.setLayout(borderLayout1);
        serverListCB.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                checkDbSelect(ae);
            }
        });


        jLabel1.setText("Database server:");
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                tabbedPane_stateChanged(e);
            }
        });

        queryEditor.setText("<enter query>");
        queryEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                queryEditor_keyReleased(e);
            }
        });
        queryEditor.setFont(new java.awt.Font("Monospaced", 0, 12));
        queryEditor.setOpaque(true);
        queryEditor.setPreferredSize(new Dimension(97, 23));
        queryEditor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e, QUERY_TAB);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e, QUERY_TAB);
            }
        });
        queryEditor.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                SqlToolkit.menuManager.setCurrentOwner(mHandler);
            }
        });
        queryEditor.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent ce) {
                checkCopyStatus(ce);
            }
        });
        queryPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        messageArea.setText("<no messages>");
        messageArea.setDisabledTextColor(Color.darkGray);
        messageArea.setEditable(false);
        messageArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e, MESSAGE_TAB);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e, MESSAGE_TAB);
            }
        });
        messageArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                SqlToolkit.menuManager.setCurrentOwner(mHandler);
            }
        });

        jPanel1.setLayout(gridBagLayout1);
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e, null);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e, null);
            }
        });
        resultTable.setFont(new java.awt.Font("SansSerif", 0, 11));
        resultTable.setModel(resultTableModel);
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e, RESULTS_TAB);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e, RESULTS_TAB);
            }
        });
        resultTable.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent fe) {
                SqlToolkit.menuManager.setCurrentOwner(mHandler);
            }
        });
        resultTable.setDefaultRenderer(double.class, new DecimalRenderer());
        resultTable.setDefaultRenderer(Double.class, new DecimalRenderer());
        resultTable.setDefaultRenderer(BigDecimal.class, new DecimalRenderer());
        resultTable.setDefaultRenderer(Clob.class, new ClobRenderer());
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int irow = resultTable.getSelectedRow();
                    int icol = resultTable.getSelectedColumn();
                    Object what = resultTable.getValueAt(irow, icol);
                    if (what != null  &&  what instanceof Clob) {
                        String line = "";
                        StringBuilder sbuf = new StringBuilder();
                        try {
                            Reader reader = ((Clob)what).getCharacterStream();
                            BufferedReader br = new BufferedReader(reader);
                            while ((line=br.readLine()) != null  &&  sbuf.length() < 255) {
                                sbuf.append(line);
                            }
                            br.close();
                        } catch(Exception ex) {
                            sbuf.append(" ... " + ex.getMessage());
                        }
                    }
                }
            }
        });

        resultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        resultPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkOptionMenu(e, RESULTS_TAB);
            }
            public void mouseReleased(MouseEvent e) {
                checkOptionMenu(e, RESULTS_TAB);
            }
        });
        greenBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("greenLightOn.gif")));
        greenBtn.setMargin(new Insets(0, 2, 0, 2));
        greenBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                greenBtn_actionPerformed(e);
            }
        });
        greenBtn.setDebugGraphicsOptions(0);
        greenBtn.setToolTipText("start query");
        greenBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("greenLightOff.gif")));
        greenBtn.setFocusPainted(false);
        greenBtn.setHorizontalTextPosition(SwingConstants.TRAILING);

        yellowBtn.setEnabled(false);
        yellowBtn.setToolTipText("pause query");
        yellowBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("yellowLightOn.gif")));
        yellowBtn.setMargin(new Insets(0, 2, 0, 2));
        yellowBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                yellowBtn_actionPerformed(e);
            }
        });
        yellowBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("yellowLightOff.gif")));
        yellowBtn.setFocusPainted(false);

        redBtn.setEnabled(false);
        redBtn.setToolTipText("halt query");
        redBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("redLightOn.gif")));
        redBtn.setMargin(new Insets(0, 2, 0, 2));
        redBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redBtn_actionPerformed(e);
            }
        });
        redBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("redLightOff.gif")));
        redBtn.setFocusPainted(false);

        histLastBtn.setEnabled(false);
        histLastBtn.setToolTipText("Last query");
        histLastBtn.setHorizontalTextPosition(SwingConstants.TRAILING);
        histLastBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("historyLastOn.gif")));
        histLastBtn.setMargin(new Insets(0, 0, 0, 0));
        histLastBtn.setText("");
        histLastBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                histLastBtn_actionPerformed(e);
            }
        });
        histLastBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("historyLastOff.gif")));
        histLastBtn.setFocusPainted(false);

        histNextBtn.setEnabled(false);
        histNextBtn.setToolTipText("Next query");
        histNextBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("historyNextOn.gif")));
        histNextBtn.setMargin(new Insets(0, 0, 0, 0));
        histNextBtn.setText("");
        histNextBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                histNextBtn_actionPerformed(e);
            }
        });
        histNextBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("historyNextOff.gif")));
        histNextBtn.setFocusPainted(false);

        histPrevBtn.setEnabled(false);
        histPrevBtn.setToolTipText("Previous query");
        histPrevBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("historyPrevOn.gif")));
        histPrevBtn.setMargin(new Insets(0, 0, 0, 0));
        histPrevBtn.setText("");
        histPrevBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                histPrevBtn_actionPerformed(e);
            }
        });
        histPrevBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("historyPrevOff.gif")));
        histPrevBtn.setFocusPainted(false);

        histFirstBtn.setEnabled(false);
        histFirstBtn.setToolTipText("First query");
        histFirstBtn.setIcon(new ImageIcon(QueryPanel.class.getResource("historyFirstOn.gif")));
        histFirstBtn.setMargin(new Insets(0, 0, 0, 0));
        histFirstBtn.setText("");
        histFirstBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                histFirstBtn_actionPerformed(e);
            }
        });
        histFirstBtn.setDisabledIcon(new ImageIcon(QueryPanel.class.getResource("historyFirstOff.gif")));
        histFirstBtn.setFocusPainted(false);

        autoCommitCB.setText("auto-commit");
        autoCommitCB.setToolTipText("Enable/disable auto-commit");
        autoCommitCB.setEnabled(true);
        autoCommitCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAutoCommitParams(autoCommitCB.isSelected(), true, false, true);
            }
        });

        jPanel3.setBorder(BorderFactory.createEtchedBorder());
        this.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 6, 0, 2), 0, 0));
        jPanel1.add(serverListCB, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 2, 5, 0), 0, 0));
        jPanel1.add(iconPanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        iconPanel.add(autoCommitCB, null);
        iconPanel.add(greenBtn, null);
        iconPanel.add(yellowBtn, null);
        iconPanel.add(redBtn, null);
        iconPanel.add(jPanel3, null);
        jPanel3.add(histFirstBtn, null);
        jPanel3.add(histPrevBtn, null);
        jPanel3.add(histNextBtn, null);
        jPanel3.add(histLastBtn, null);

        this.add(tabbedPane, BorderLayout.CENTER);
    }


    /**
     *  Set the list of servers ...
     */
    public void updateServerList() {
        DbDefinition dbTemp = (DbDefinition)serverListCB.getSelectedItem();
        serverListCB.setModel(new DefaultComboBoxModel(SqlToolkit.dbDefManager.getDbDefList().toArray()));
        if (dbTemp != null) {
            serverListCB.setSelectedItem(dbTemp);
        }
        setAutoCommitFromDB();
    }

    /**
     * Clean-up things when the panel is going away
     */
    public void pleaseCleanUp() {
        SqlToolkit.userConfig.deleteObserver(this);
        SqlToolkit.menuManager.unregisterOwner(this);

        // Help the GC process a little ...
        removeAll();
        resultTable      = null;
        queryHistory     = null;
        sqlDoc           = null;
        resultTableModel = null;
        sqlModelFactory  = null;
        serverListCB     = null;
    }

    /**
     * Convenience method for building the pop-up menu when we don't need to
     * explicitly specify which menu we're building
     */
    protected void buildOptionPopupMenu() {
        this.buildOptionPopupMenu(null);
    }


    /**
     *  Build the pop-up menu for all user options; in some cases the tab name
     *  to be built is passed to us and in other cases it's null, which means
     *  we look at the current tab and build its menu...
     */
    protected void buildOptionPopupMenu(String tabName) {
        // See if we already have a usable current menu
        if (currentTabName != null  &&  currentTabName.equalsIgnoreCase(tabName)) {
            return;
        }

        // Let the parent build what it needs
        super.buildOptionPopupMenu();

        // Figure out what menu we need to build
        if (tabName == null) {
            if (tabbedPane == null  ||  tabbedPane.getSelectedIndex() == -1) {
                return;
            } else {
                currentTabName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            }
        } else {
            currentTabName = tabName;
        }

        if (QUERY_TAB.equals(currentTabName)) {
            ButtonGroup menuBG = new ButtonGroup();
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, (isQueryResultsSplit ? MENU_COMBINE_PANE : MENU_SEPARATE_PANE));
            SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_EDIT_COPY);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_EDIT_PASTE);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_EDIT_SELECT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_EDIT_FIND);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CLEAR_QUERY);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_RUN_QUERY);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_RUN_MULTI);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_RUN_FROM_FILE);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_SAVE_QUERY);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CONN_COMMITON, menuBG, true);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CONN_COMMITOFF, menuBG, false);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CONN_COMMIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CONN_ROLLBACK);

            menuBG = new ButtonGroup();
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, (isQueryResultsSplit ? MENU_COMBINE_PANE : MENU_SEPARATE_PANE));
            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_EDIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_EDIT_COPY);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_EDIT_PASTE);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_EDIT_SELECT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_EDIT_FIND);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_EDIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_RUN_QUERY);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_RUN_MULTI);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_RUN_FROM_FILE);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_SAVE_QUERY);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_EDIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMITON, menuBG, true);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMITOFF, menuBG, false);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_ROLLBACK);

            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_VIEW);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, MENU_CLEAR_QUERY);

            setAutoCommitFromDB();
        } else if (RESULTS_TAB.equals(currentTabName)) {
            boolean rowSel = resultTable.getRowSelectionAllowed();
            boolean colSel = resultTable.getColumnSelectionAllowed();

            ButtonGroup menuBG = new ButtonGroup();
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, (isQueryResultsSplit ? MENU_COMBINE_PANE : MENU_SEPARATE_PANE));
            SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_SAVE_RESULTS);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_ROW_SELECTION, menuBG, rowSel);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_COL_SELECTION, menuBG, colSel);
            SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_CELL_SELECTION, menuBG, rowSel && colSel);

            menuBG = new ButtonGroup();
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, (isQueryResultsSplit ? MENU_COMBINE_PANE : MENU_SEPARATE_PANE));
            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_EDIT);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_SAVE_RESULTS);
            SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_VIEW);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, MENU_ROW_SELECTION, menuBG, rowSel);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, MENU_COL_SELECTION, menuBG, colSel);
            SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, MENU_CELL_SELECTION, menuBG, rowSel && colSel);
        }

        SqlToolkit.menuManager.setCurrentOwner(this, true);
    }


    /**
     * Check to see if the "copy" event should be enabled, and set the
     * menu events accordingly
     */
    public void checkCopyStatus(CaretEvent ce) {
        boolean okEnable = (ce.getDot() - ce.getMark()) != 0;
        SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_EDIT_COPY, okEnable);
        SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_EDIT_COPY, okEnable);
    }


    /**
     * Set the initial "auto-commit" values based on what the current
     * database setting is.
     */
    protected void setAutoCommitFromDB() {
        dbDef = (DbDefinition) serverListCB.getSelectedItem();
        if (dbDef != null) {
            Connection conn = ConnectionManager.GetConnection(dbDef, true);
            if (conn != null) {
                boolean autoOn = true;
                try {
                    autoOn = conn.getAutoCommit();
                } catch (Exception sqlex) {
                }
                setAutoCommitParams(autoOn, false, true, true);
            }
        }
    }


    /**
     * If auto-commit is off, manually commit the transaction
     */
    protected void commitTransaction() {
        dbDef = (DbDefinition) serverListCB.getSelectedItem();
        if (dbDef != null) {
            Connection conn = ConnectionManager.GetConnection(dbDef, true);
            if (conn != null) {
                try {
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Commit Successful", "DB transaction", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception sqlex) {
                    JOptionPane.showMessageDialog(this, "Commit Failed", "DB transaction", JOptionPane.ERROR_MESSAGE);
                    messageArea.setText("Unable to commit: " + sqlex.getMessage());
                    tabbedPane.setSelectedComponent(messagePane);
                }
            }
        }
    }


    /**
     * If auto-commit is off, manually roll-back the transaction
     */
    protected void rollbackTransaction() {
        dbDef = (DbDefinition) serverListCB.getSelectedItem();
        if (dbDef != null) {
            Connection conn = ConnectionManager.GetConnection(dbDef, true);
            if (conn != null) {
                try {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Rollback Successful", "DB transaction", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception sqlex) {
                    JOptionPane.showMessageDialog(this, "Rollback Failed", "DB transaction", JOptionPane.ERROR_MESSAGE);
                    messageArea.setText("Unable to rollback: " + sqlex.getMessage());
                    tabbedPane.setSelectedComponent(messagePane);
                }
            }
        }
    }

    /**
     * Change the auto-commit parameters and associated menu items, etc.
     * @param autoCommit TRUE or FALSE, the new "auto-commit" value
     * @param updDB flag indicating if the database setting should be changed
     * @param updCB flag indicating if the check-box setting should be changed
     * @param updRB flag indicating if the radio-button menu should be changed
     */
    private void setAutoCommitParams(boolean autoCommit, boolean updDB, boolean updCB, boolean updRB) {
        SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_CONN_COMMIT, !autoCommit);
        SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_CONN_ROLLBACK, !autoCommit);
        SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMIT, !autoCommit);
        SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_ROLLBACK, !autoCommit);

        // Update the database, if necessary
        if (updDB) {
            dbDef = (DbDefinition) serverListCB.getSelectedItem();
            if (dbDef != null) {
                Connection conn = ConnectionManager.GetConnection(dbDef, true);
                if (conn != null) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (Exception sqlex) { }

                    if (autoCommit != autoCommitCB.isSelected()) {
                        autoCommitCB.setSelected(autoCommit);
                    }
                }
            }
        }

        // Update the checkbox (if necessary)
        if (updCB  &&  (autoCommit ^ autoCommitCB.isSelected())) {
            autoCommitCB.setSelected(autoCommit);
        }

        // Update the radio button (if necessary); we have to process the
        // pop-up menu items and main-menu items separately
        if (updRB) {
            if (autoCommit ^ SqlToolkit.menuManager.isSelected(this, MenuManager.POPUP_MENU, MENU_CONN_COMMITON)) {
                SqlToolkit.menuManager.setSelected(this, MenuManager.POPUP_MENU, MENU_CONN_COMMITON, autoCommit);
                SqlToolkit.menuManager.setSelected(this, MenuManager.POPUP_MENU, MENU_CONN_COMMITOFF, !autoCommit);
            }
            if (autoCommit ^ SqlToolkit.menuManager.isSelected(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMITON)) {
                SqlToolkit.menuManager.setSelected(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMITON, autoCommit);
                SqlToolkit.menuManager.setSelected(this, MenuManager.MAIN_MENU_EDIT, MENU_CONN_COMMITOFF, !autoCommit);
            }
        }
    }


    /**
     * Handle all menu events.  This will be called whether the event comes
     * from the main menu, or a pop-up menu
     */
    public void handleMenuEvent(ActionEvent ae) {
        super.handleMenuEvent(ae);
        if (ae.getSource() instanceof JMenuItem) {
            JMenuItem mi = (JMenuItem)ae.getSource();
            if (mi.getText().equalsIgnoreCase(MENU_EDIT_COPY)) {
                editCopy();
            } else if (mi.getText().equalsIgnoreCase(MENU_EDIT_PASTE)) {
                editPaste();
            } else if (mi.getText().equalsIgnoreCase(MENU_EDIT_SELECT)) {
                editSelectAll();
            } else if (mi.getText().equalsIgnoreCase(MENU_EDIT_FIND)) {
                editFind();
            } if (mi.getText().equalsIgnoreCase(MENU_CLEAR_QUERY)) {
                clearQueryPane();
            } else if (mi.getText().equalsIgnoreCase(MENU_RUN_QUERY)) {
                greenBtnPush(false);
            } else if (mi.getText().equalsIgnoreCase(MENU_RUN_MULTI)) {
                greenBtnPush(true);
            } else if (mi.getText().equalsIgnoreCase(MENU_RUN_FROM_FILE)) {
                runFromFile();
            } else if (mi.getText().equalsIgnoreCase(MENU_SAVE_QUERY)) {
                saveRequested();
            } else if (mi.getText().equalsIgnoreCase(MENU_SAVE_RESULTS)) {
                saveRequested();
            } else if (mi.getText().equalsIgnoreCase(MENU_SEPARATE_PANE)) {
                makeSeparateQueryResultTab();
            } else if (mi.getText().equalsIgnoreCase(MENU_COMBINE_PANE)) {
                makeSingleQueryResultTab();
            } else if (mi.getText().equalsIgnoreCase(MENU_ROW_SELECTION)) {
                resultTable.setRowSelectionAllowed(true);
                resultTable.setColumnSelectionAllowed(false);
            } else if (mi.getText().equalsIgnoreCase(MENU_COL_SELECTION)) {
                resultTable.setRowSelectionAllowed(false);
                resultTable.setColumnSelectionAllowed(true);
            } else if (mi.getText().equalsIgnoreCase(MENU_CELL_SELECTION)) {
                resultTable.setRowSelectionAllowed(true);
                resultTable.setColumnSelectionAllowed(true);
            } else if (mi.getText().equalsIgnoreCase(MENU_CONN_COMMITON)) {
                setAutoCommitParams(true, true, true, true);
            } else if (mi.getText().equalsIgnoreCase(MENU_CONN_COMMITOFF)) {
                setAutoCommitParams(false, true, true, true);
            } else if (mi.getText().equalsIgnoreCase(MENU_CONN_COMMIT)) {
                commitTransaction();
            } else if (mi.getText().equalsIgnoreCase(MENU_CONN_ROLLBACK)) {
                rollbackTransaction();
            } 
        }
    }

    /**
     * This panel has gained focus.  Set this as the target of the "Find"
     * dialog, and set auto-commit parameters
     */
    public void becomeActive() {
        super.becomeActive();
        FindDialog findDialog = SqlToolkit.getFindDialog(this);
        findDialog.setTarget(queryEditor);
        setAutoCommitFromDB();
    }


    /**
     * Save either the query or results pane
     */
    public void saveRequested() {
        if (tabbedPane.getSelectedComponent().equals(queryPane)) {
            saveQuery();
        } else if (tabbedPane.getSelectedComponent().equals(resultPane)) {
            saveResults();
        }
    }

    /**
     * Do a "copy" operation
     */
    private void editCopy() {
        queryEditor.copy();
    }

    /**
     * Do a "paste" operation
     */
    private void editPaste() {
        queryEditor.paste();
    }

    /**
     * Do a "select-all" operation
     */
    private void editSelectAll() {
        queryEditor.selectAll();
    }

    /**
     * Do an "edit-find" operation, i.e., find/replace
     */
    private void editFind() {
        FindDialog findDialog = SqlToolkit.getFindDialog(this);
        findDialog.setTarget(queryEditor);
        findDialog.setVisible(true);
    }

    /**
     * Save the current query in the "Favorites" section.  This is not
     * supported ...
     */
    private void saveQuery() {
        System.out.println("SAVE QUERY ...");
    }

    /**
     * Clear the query pane
     */
    private void clearQueryPane() {
        queryEditor.setText("");
    }

    /**
     * Save the selected results to a ".csv" file.
     */
    private void saveResults() {
        // If there are no rows, don't save nothing ...
        if (resultTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No results to save", "Save warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Determine the field delimiter and field optional quote
        String delim = SqlToolkit.userConfig.getFieldDelim();
        String quote = SqlToolkit.userConfig.getFieldQuote();

        // Bring up a file dialog to get the file path
        String path = getFilePathSave("Save Results To ...");
        if (path != null) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new File(path), "UTF-8");

                // Determine which rows are selected; if none are, print all rows
                int[] rowrow = new int[0];
                if (resultTable.getRowSelectionAllowed()) {
                    rowrow = resultTable.getSelectedRows();
                }
                if (rowrow.length == 0) {
                    rowrow = new int[resultTableModel.getRowCount()];
                    for (int i=0;  i<rowrow.length;  i++) {
                        rowrow[i] = i;
                    }
                }
                int rowCnt = rowrow.length;

                // Determine which columns are selected; if none are, print all columns
                int[] colcol = new int[0];
                if (resultTable.getColumnSelectionAllowed()) {
                    colcol = resultTable.getSelectedColumns();	
                }
                if (colcol.length == 0) {
                    colcol = new int[resultTableModel.getColumnCount()];
                    for (int i=0;  i<colcol.length;  i++) {
                        colcol[i] = i;
                    }
                }
                int colCnt = colcol.length;

                // Dump out the column names as the first record
                for (int i=0;  i<colCnt;  i++) {
                    if (i > 0) pw.print(delim);
                    pw.print((quote == null) ? "" : quote);
                    pw.print(formatForCSV(resultTableModel.getColumnName(colcol[i])));
                    pw.print((quote == null) ? "" : quote);
                }
                pw.println();


                // Make sure that the character values are surrounded by quotes if the
                // user so specified
                for (int i=0;  i<rowCnt;  i++) {
                    int row = rowrow[i];
                    for (int j=0;  j<colCnt;  j++) {
                        int col = colcol[j];
                        if (j > 0) pw.print(delim);
                        Class<?> cls = resultTableModel.getColumnClass(j);
                        if (cls == String.class) pw.print((quote == null) ? "" : quote);
                        pw.print(formatForCSV(resultTableModel.getValueAt(row, col)));
                        if (cls == String.class) pw.print((quote == null) ? "" : quote);
                    }
                    pw.println();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "File Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (pw != null) pw.close(); } catch (Exception ex2) {}
            }
        }
    }

    /**
     * Split the query and results into two separate panes [tabs]
     */
    private void makeSeparateQueryResultTab() {
        isQueryResultsSplit = true;
        tabbedPane.removeAll();
        tabbedPane.add(queryPane, QUERY_TAB);
        queryPane.getViewport().add(queryEditor, null);
        tabbedPane.add(resultPane, RESULTS_TAB);
        resultPane.getViewport().add(resultTable, null);
        tabbedPane.add(messagePane, MESSAGE_TAB);
        messagePane.getViewport().add(messageArea, null);
        buildOptionPopupMenu();
        paneLocationSave = querySplitPane.getDividerLocation();
    }

    /**
     * Combine the query and results into one pane [tab]
     */
    private void makeSingleQueryResultTab() {
        isQueryResultsSplit = false;
        tabbedPane.removeAll();
        tabbedPane.add(querySplitPane, MERGE_TAB);
        querySplitPane.setLeftComponent(queryPane);
        querySplitPane.setRightComponent(resultPane);
        tabbedPane.add(messagePane, MESSAGE_TAB);
        messagePane.getViewport().add(messageArea, null);
        querySplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        querySplitPane.setDividerLocation((paneLocationSave == 0) ? 320 : paneLocationSave);
        buildOptionPopupMenu();
    }

    /**
     * Display the pop-up menu in the correct location.  Different OS-es have
     * pop-up triggers with either mouse-pressed or mouse-released, so we
     * must check both events
     */
    private void checkOptionMenu(MouseEvent e, String tabName) {
        buildOptionPopupMenu(tabName);
        SqlToolkit.menuManager.setCurrentOwner(this);
        JPopupMenu optionMenu = SqlToolkit.menuManager.getPopup();
        if (e.isPopupTrigger()  &&  optionMenu != null) {
            optionMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * A new tab was selected ... modify the menus accordingly
     * @param e event
     */
    private void tabbedPane_stateChanged(ChangeEvent e) {
        buildOptionPopupMenu();
    }

    /**
     * A key-release event occurred: check for three actions:
     *    -- F7: execute current query
     *    -- F9: execute multiple queries
     *    -- CTRL-F: bring up the "Find" dialog
     * @param e event
     */
    private void queryEditor_keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F7) {
            greenBtnPush(false);
        } else if (e.getKeyCode() == KeyEvent.VK_F9) {
            greenBtnPush(true);
        } else if (e.getKeyCode() == KeyEvent.VK_F  &&  e.getModifiers() == KeyEvent.CTRL_MASK) {
            editFind();
        }
    }

    /**
     * User selected the "Go" [green] button
     * @param e event
     */
    private void greenBtn_actionPerformed(ActionEvent e) {
        greenBtnPush(false);
    }

    /**
     * User selected the "Pause" [yellow] button
     * @param e event
     */
    private void yellowBtn_actionPerformed(ActionEvent e) {
        yellowBtnPush();
    }

    /**
     * User selected the "Stop" [red] button
     * @param e event
     */
    private void redBtn_actionPerformed(ActionEvent e) {
        redBtnPush(true);
    }

    /**
     * Execute the selected query or queries.  If nothing is selected, and
     * "doMulti" is false, calculate the current query based on the cursor
     *  and execute that.
     *  @param doMulti flag indicating if we are to execute multiple queries
     *         or not.
     */
    private void doQuery(boolean doMulti) {
        dbDef = (DbDefinition) serverListCB.getSelectedItem();
        if (dbDef == null) {
            JOptionPane.showMessageDialog(this, "No database server defined", "",
                    JOptionPane.WARNING_MESSAGE);
        }
        else {
            messageArea.setText("");
            String sqlDelim = SqlToolkit.userConfig.getSqlDelim();
            //			String bodyDelim = SqlToolkit.userConfig.getBodyDelim();
            String query = queryEditor.getSelectedText();

            // If there is selected text, run only that portion of the query; if we aren't
            // doing multiple queries, ignore the delimiter ...
            if (query == null || query.length() < 4) {
                query = sqlDoc.getQueryAtIndex(queryEditor.getCaretPosition(), sqlDelim);
            } else if (! doMulti) {
                sqlDelim = null;
            }

            if (query == null  ||  query.trim().length() < 4) {
                messageArea.setText("Please select query to execute, or put cursor in query ...");
                tabbedPane.setSelectedComponent(messagePane);
                redBtnPush(false);
                return;
            }

            if (!queryHistory.contains(query)) {
                queryHistory.add(query);
                currentQuery = queryHistory.size() - 1;
                enableHistoryButtons();
            }

            // Print out an appropriate message if the connection is bad
            Connection conn = ConnectionManager.GetConnection(dbDef, true);
            if (conn == null) {
                messageArea.setText(ConnectionManager.GetConnectionError(dbDef));
                tabbedPane.setSelectedComponent(messagePane);
                redBtnPush(false);
                return;
            }

            // Have the "factory" kick off the retrieval process
            final long nnow = System.currentTimeMillis();
            resultTableModel = sqlModelFactory.createModelData(conn, query, sqlDelim, doMulti);
            resultTable.setModel(resultTableModel);
            if (isQueryResultsSplit) {
                tabbedPane.setSelectedComponent(resultPane);
            } else {
                tabbedPane.setSelectedComponent(querySplitPane);
            }

            // Set up a small thread-ed process to monitor the query process
            Thread monitorProcess = new Thread(new Runnable() {
                public void run() {
                    while (sqlModelFactory.isActive()) {
                        try { Thread.sleep(250); } catch (Exception ex) {}
                    }
                    redBtnPush(false);

                    // Do a little fun re-sizing of the table columns
                    TableColumnModel tcm = resultTable.getColumnModel();
                    int colCnt = tcm.getColumnCount();
                    for (int i=0;  i<colCnt;  i++) {
                        TableColumn tc = tcm.getColumn(i);
                        int colWidth = resultTableModel.getCharacterWidth(i);
                        tc.setPreferredWidth(Math.min( (colWidth + 2) * 5, 400));
                    }

                    // Check to see if there was an error returned
                    int    rowCnt  = resultTableModel.getRowCount();   
                    String message = "Rows returned: " + rowCnt;
                    message += "\n\nTime: " + (System.currentTimeMillis() - nnow) + " milliseconds";
                    message += sqlModelFactory.getMessage();
                    messageArea.setText(message);

                    String errorMessage = sqlModelFactory.getErrorMessage();
                    if (errorMessage != null  &&  errorMessage.trim().length() > 0) {
                        if (errorMessage.length() > 1024) {
                            errorMessage = errorMessage.substring(0,1024) + " ...";
                        }
                        JOptionPane.showMessageDialog(null, errorMessage, "Errors", JOptionPane.ERROR_MESSAGE);
                    }

                    if (rowCnt > 0) {
                        if (isQueryResultsSplit) {
                            tabbedPane.setSelectedComponent(resultPane);
                        } else {
                            tabbedPane.setSelectedComponent(querySplitPane);
                        }
                    } else {
                        tabbedPane.setSelectedComponent(messagePane);
                    }
                }
            });
            monitorProcess.start();
        }
    }

    /**
     * Start a query, or re-start a paused query
     * @param doMulti flag indicating if we are to execute multiple queries
     *        or not.
     */
    private void greenBtnPush(boolean doMulti) {
        if (greenBtn.isEnabled()) {
            greenBtn.setEnabled(false);
            yellowBtn.setEnabled(true);
            redBtn.setEnabled(true);
            if (sqlModelFactory.isActive()) {
                sqlModelFactory.restart();
            } else {
                doQuery(doMulti);
            }
        }
    }

    /**
     * 
     */
    private void runFromFile() {
        if (sqlModelFactory.isActive()) {
            JOptionPane.showMessageDialog(this, "Please wait until the current query finishes ...");
            return;
        }

        FileDialog fd = new FileDialog(SqlToolkit.getParentFrame(this), "Find Class File ...", FileDialog.LOAD);
        if (prevDir != null) {
            fd.setDirectory(prevDir);
        }
        fd.setVisible(true);

        String path = fd.getDirectory();
        String file = fd.getFile();
        if (file != null) {
            prevDir = path;
        }
    }

    /**
     * Pause a query
     */
    private void yellowBtnPush() {
        sqlModelFactory.halt();
        greenBtn.setEnabled(true);
        yellowBtn.setEnabled(false);
        redBtn.setEnabled(true);
    }


    /**
     * Stop a query.  It can be called when the user pushes the "Stop" button
     * or when the thread monitoring the query detects that we are done.
     * @prompt source of the stop: TRUE indicates user initiated, FALSE
     *         indicates system initiated
     */
    private void redBtnPush(boolean prompt) {
        // Temporarily halt the process (in case we are still running ...)
        sqlModelFactory.halt();

        // If we need to prompt the user, determine if they really, REALLY want
        // to halt the process
        int answer = JOptionPane.YES_OPTION;
        if (prompt) {
            answer = JOptionPane.showConfirmDialog(
                    this,
                    resultTableModel.getRowCount() + " records returned:\nReally, really halt?",
                    "Cease and Desist", JOptionPane.YES_NO_OPTION);
        }

        // If the process isn't to be halted, re-set the current state in terms
        // of what buttons should be active
        if (answer == JOptionPane.NO_OPTION) {
            if (yellowBtn.isEnabled()) {
                sqlModelFactory.restart();
            }
        }
        // If the process is to be halted, get out for good and re-set the current
        // state back to the starting position
        else {
            sqlModelFactory.getOut();
            greenBtn.setEnabled(true);
            yellowBtn.setEnabled(false);
            redBtn.setEnabled(false);
        }
    }

    /**
     * A new database has been selected.  Make sure we can connect to the database, and
     * cache some information about the connection.
     * @param ae event
     */
    private void checkDbSelect(ActionEvent ae) {
        dbDef = (DbDefinition)serverListCB.getSelectedItem();
        if (dbDef == null) {
            JOptionPane.showMessageDialog(this, "No database server defined", "",
                    JOptionPane.WARNING_MESSAGE);
        }

        // Make a connection to the database and get the basic metadata
        Cursor saveCursor = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Connection conn = ConnectionManager.GetConnection(dbDef, true);
        DbInfoModel dbModel = DbInfoCache.GetInfoModel(conn);
        setCursor(saveCursor);

        // If we have no connection display an error message; otherwise get
        // a list of all functions and keywords for help in making our SQL
        // query text pretty
        if (conn == null) {
            messageArea.setText(ConnectionManager.GetConnectionError(dbDef));
            tabbedPane.setSelectedComponent(messagePane);
        } else if (dbModel != null) {
            sqlDoc.setDefaultKeywords();
            sqlDoc.addKeywords(dbModel.getKeywordList());
            sqlDoc.addFunctions(dbModel.getDateTimeFuncList());
            sqlDoc.addFunctions(dbModel.getNumericFuncList());
            sqlDoc.addFunctions(dbModel.getStringFuncList());
            sqlDoc.addFunctions(dbModel.getSystemFuncList());
            setAutoCommitFromDB();
        }
    }

    /**
     * This method is call "auto-magically" when something we are observing
     * has changed.  This can come when the list of servers changes, or when
     * the user configuration changes
     * @param source what initiated the change
     * @param arg what changed
     */
    public void update(Observable source, Object arg) {
        if (source == SqlToolkit.dbDefManager) {
            updateServerList();
        } else {
            checkConfig(arg==null ? "" : arg.toString());
        }
    }

    /**
     * User configuration changed.  We care about a couple of fonts and
     * the tab spacing
     * @param alias key indicating what changed
     */
    private void checkConfig(String alias) {
        if (UserConfig.FONT_QUERY_EDITOR.equals(alias)) {
            Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_EDITOR);
            if (newFont != null) {
                queryEditor.setFont(newFont);
                messageArea.setFont(newFont);
            }
        } else if (UserConfig.FONT_QUERY_RESULT.equals(alias)) {
            Font newFont = SqlToolkit.userConfig.getFont(UserConfig.FONT_QUERY_RESULT);
            if (newFont != null) {
                resultTable.setFont(newFont);
            }
        } else if (UserConfig.PARAM_TAB_SPACING.equals(alias)) {
            messageArea.setTabSize(SqlToolkit.userConfig.getTabSpacing());
            FontMetrics fm = queryEditor.getFontMetrics(queryEditor.getFont());
            int tabSize = SqlToolkit.userConfig.getTabSpacing() *
            fm.stringWidth("X");

            TabStop[] tsList = new TabStop[10];
            for (int i=0;  i<tsList.length;  i++) {
                tsList[i] = new TabStop((i+1) * tabSize);
            }
            TabSet tabSet = new TabSet(tsList);

            Style curStyle = queryEditor.getLogicalStyle();
            StyleConstants.setTabSet(curStyle, tabSet);
            queryEditor.setLogicalStyle(curStyle);
        }
    }

    //	=============================================================================
    //	These methods are used to handle the FIRST/PREV/NEXT/LAST buttons for
    //	browsing through the history of queries
    //	=============================================================================

    /**
     * "First" button pushed
     * @param e event
     */
    private void histFirstBtn_actionPerformed(ActionEvent e) {
        currentQuery = 0;
        setQueryFromHistory();
    }

    /**
     * "Previous" button pushed
     * @param e event
     */
    private void histPrevBtn_actionPerformed(ActionEvent e) {
        currentQuery--;
        setQueryFromHistory();
    }

    /**
     * "Next" button pushed
     * @param e event
     */
    private void histNextBtn_actionPerformed(ActionEvent e) {
        currentQuery++;
        setQueryFromHistory();
    }

    /**
     * "Last" button pushed
     * @param e event
     */
    private void histLastBtn_actionPerformed(ActionEvent e) {
        currentQuery = queryHistory.size() - 1;
        setQueryFromHistory();
    }

    /**
     * Pull the query from the history list
     */
    private void setQueryFromHistory() {
        if (currentQuery >= 0  &&   currentQuery < queryHistory.size()) {
            queryEditor.setText(queryHistory.get(currentQuery));
            queryEditor.setCaretPosition(0);
            tabbedPane.setSelectedComponent(queryPane);
        }
        enableHistoryButtons();
    }

    /**
     * Enable the history buttons only if we have at least one query that
     * has run.
     */
    private void enableHistoryButtons() {
        histFirstBtn.setEnabled(currentQuery > 0);
        histPrevBtn.setEnabled(currentQuery > 0);
        histNextBtn.setEnabled(currentQuery < queryHistory.size()-1);
        histLastBtn.setEnabled(currentQuery < queryHistory.size()-1);
    }
}
