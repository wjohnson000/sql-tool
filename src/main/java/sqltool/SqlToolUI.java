package sqltool;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;

import dragdrop.jtree.*;
import sqltool.common.ConnectionManager;
import sqltool.common.ConnectionViewer;
import sqltool.common.MenuHandler;
import sqltool.common.MenuManager;
import sqltool.common.SqlToolkit;
import sqltool.config.UserConfigEditor;
import sqltool.query.QueryPanel;
import sqltool.schema.*;
import sqltool.server.DbDefinition;
import sqltool.swing.extra.CloseTabbedPane;


public class SqlToolUI extends JFrame implements MenuHandler {

	private static final long serialVersionUID = -4097656477553354051L;
	
	//	============================================================================
	//	Set up static menu options ...
	//	============================================================================
	static final String MENU_FILE_DBCONN   = "db conn ...";
	static final String MENU_FILE_CONFIG   = "config ...";
	static final String MENU_FILE_IMPORT   = "import ...";
	static final String MENU_FILE_EXPORT   = "export ...";
	static final String MENU_FILE_LOOKFEEL = "look and feel";
	static final String MENU_FILE_EXIT     = "exit";
	static final String MENU_HELP_ABOUT    = "about";
	
	int panelSeq = 1;
	String prevDir = null;
	
	FolderListPanel serverListPanel = new FolderListPanel();
	FolderListPanel favoriteListPanel = new FolderListPanel();
	
	JPanel contentPane;
	JMenuBar mainMenuBar = new JMenuBar();
	
	JToolBar jToolBar = new JToolBar();
	JButton openSqlBBtn = new JButton();
	JButton openSchemaBBtn = new JButton();
	JButton helpBBtn = new JButton();
	ImageIcon image1;
	ImageIcon image2;
	ImageIcon image3;
	ImageIcon image4;
	JLabel statusBar = new JLabel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JSplitPane jSplitPane1 = new JSplitPane();
	JTabbedPane controlPane = new JTabbedPane();
	JTabbedPane actionPane = new CloseTabbedPane();
	
	/**Construct the frame*/
	public SqlToolUI() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.enableInputMethods(false);
		try {
			jbInit();
			buildMenus();
			SqlToolkit.menuManager.addObserver(new Observer() {
				public void update(Observable o, Object arg) {
					updateMenus();
				}
			});
			SqlToolkit.menuManager.setCurrentOwner(this);
		
			serverListPanel.setItemClassName("sqltool.server.DbDefinition");
			serverListPanel.setRootFolderName("Server List");
			favoriteListPanel.setItemClassName("sqltool.favorites.FavoriteSql");
			favoriteListPanel.setRootFolderName("Favorite SQL");
			loadDefaults();
			addPanelTreeListeners();
			openSqlBBtn_actionPerformed(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Component initialization*/
	private void jbInit() throws Exception  {
		image1 = new ImageIcon(sqltool.SqlToolUI.class.getResource("openSQL.gif"));
		image2 = new ImageIcon(sqltool.SqlToolUI.class.getResource("openSchema.gif"));
		image3 = new ImageIcon(sqltool.SqlToolUI.class.getResource("closeFile.gif"));
		image4 = new ImageIcon(sqltool.SqlToolUI.class.getResource("help.gif"));
		
		//setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Your Icon]")));
		setIconImage(Toolkit.getDefaultToolkit().createImage(sqltool.SqlToolUI.class.getResource("sqlLite.gif")));
		
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(gridBagLayout1);
		this.setSize(new Dimension(1120, 720));
		this.setTitle("SQL Tool");
		statusBar.setText(" ");
		
		openSqlBBtn.setIcon(image1);
		openSqlBBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSqlBBtn_actionPerformed(e);
			}
		});
		openSqlBBtn.setToolTipText("Open SQL query tab");
		openSchemaBBtn.setIcon(image2);
		openSchemaBBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSchemaBBtn_actionPerformed(e);
			}
		});
		openSchemaBBtn.setToolTipText("Open SCHEMA browser tab");
		helpBBtn.setIcon(image4);
		helpBBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpBBtn_actionPerformed(e);
			}
		});
		helpBBtn.setToolTipText("Help");
		controlPane.setTabPlacement(JTabbedPane.BOTTOM);
		jToolBar.add(openSqlBBtn);
		jToolBar.add(openSchemaBBtn);
		jToolBar.add(helpBBtn);
		actionPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				actionPane_stateChanged(e);
			}
		});
		controlPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				controlPane_stateChanged(e);
			}
		});
		
		this.setJMenuBar(mainMenuBar);
		contentPane.add(jToolBar, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		contentPane.add(statusBar, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		contentPane.add(jSplitPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jSplitPane1.add(controlPane, JSplitPane.TOP);
		
		controlPane.add(serverListPanel, "Server");
		controlPane.add(favoriteListPanel, "Favorites");
		
		jSplitPane1.add(actionPane, JSplitPane.BOTTOM);
		jSplitPane1.setDividerLocation(200);
	}
	
	void buildMenus() {
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_DBCONN);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_CONFIG);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_IMPORT);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_EXPORT);
		SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_FILE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_LOOKFEEL);
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_LOOKFEEL, false);
		ButtonGroup menuBG = new ButtonGroup();
		UIManager.LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
		for (int i=0;  lafi!=null && i<lafi.length;  i++) {
			String laf = UIManager.getLookAndFeel().getName();
			SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, lafi[i].getName(), menuBG, lafi[i].getName().equalsIgnoreCase(laf));
		}

		SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_FILE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_FILE, MENU_FILE_EXIT);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_HELP, MENU_HELP_ABOUT);
	}
	
	public void handleMenuEvent(ActionEvent ae) {
		if (ae.getSource() instanceof JMenuItem) {
			JMenuItem mi = (JMenuItem)ae.getSource();
			if (mi.getText().equalsIgnoreCase(MENU_FILE_DBCONN)) {
				manageConnections();
			} else if (mi.getText().equalsIgnoreCase(MENU_FILE_CONFIG)) {
				manageUserConfig();
			} else if (mi.getText().equalsIgnoreCase(MENU_FILE_IMPORT)) {
				manageImport();
			} else if (mi.getText().equalsIgnoreCase(MENU_FILE_EXPORT)) {
				manageExport();
			} else if (mi.getText().equalsIgnoreCase(MENU_FILE_EXIT)) {
				exitApp();
			} else if (mi.getText().equalsIgnoreCase(MENU_HELP_ABOUT)) {
				showAboutDialog();
			}
		} else if (ae.getSource() instanceof JRadioButtonMenuItem) {
			JRadioButtonMenuItem mi = (JRadioButtonMenuItem)ae.getSource();
			String lafName = getLookAndFeelClass(mi.getText());
			try {
				if (lafName != null) {
					UIManager.setLookAndFeel(lafName);
					SwingUtilities.updateComponentTreeUI(this);
					pack();
				}
			} catch (Exception ex) {
			}
		}
	}

	// What to do when this becomes active
	public void becomeActive() {
		
	}
	
	public void manageConnections() {
		ConnectionViewer cv = new ConnectionViewer(this, "Manage Open Connections", true);
		Dimension dlgSize = cv.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		cv.setLocation((frmSize.width-dlgSize.width)/2+loc.x, (frmSize.height-dlgSize.height)/2+loc.y);
		cv.setVisible(true);
	}
	
	public void manageUserConfig() {
		UserConfigEditor.OpenUserConfig(this);
	}
	
	public void manageImport() {
		ImportExportDialog expDlg = new ImportExportDialog(this);
		Dimension dlgSize = expDlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		expDlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		expDlg.setVisible(true);
		
		String userHome = System.getProperty("user.home", ".");
		if (expDlg.isServerListOn()) {
			String filename = userHome + "/.sqltool-serverlist.xml";
			Object treeModel = TreeModelFactory.readFromXML(filename, expDlg.getPasscode());
			serverListPanel.setTreeModel(treeModel);
		}
		
		if (expDlg.isSqlListOn()) {
			String filename = userHome + "/.sqltool-sqllist.xml";
			Object treeModel = TreeModelFactory.readFromXML(filename, expDlg.getPasscode());
			favoriteListPanel.setTreeModel(treeModel);
		}
	}
	
	public void manageExport() {
		ImportExportDialog expDlg = new ImportExportDialog(this);
		Dimension dlgSize = expDlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		expDlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		expDlg.setVisible(true);
		
		String userHome = System.getProperty("user.home", ".");
		if (expDlg.isServerListOn()) {
			String filename = userHome + "/.sqltool-serverlist.xml";
			TreeModelFactory.saveAsXML(filename, serverListPanel.getTreeModel(), expDlg.getPasscode());
		}
		
		if (expDlg.isSqlListOn()) {
			String filename = userHome + "/.sqltool-sqllist.xml";
			TreeModelFactory.saveAsXML(filename, favoriteListPanel.getTreeModel(), null);
		}
	}
	
	/**File | Exit action performed*/
	public void exitApp() {
		ConnectionManager.ShutDown();
		saveDefaults();
		SqlToolkit.userConfig.saveConfig();
		SqlToolkit.menuManager.unregisterOwner(serverListPanel);
		SqlToolkit.menuManager.unregisterOwner(favoriteListPanel);
		SqlToolkit.menuManager.unregisterOwner(this);
		System.exit(0);
	}
	
	/**Help | About action performed*/
	public void showAboutDialog() {
		AboutDialog dlg = new AboutDialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setVisible(true);
	}
	
	/**Overridden so we can exit when window is closed*/
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			exitApp();
		}
	}
	
	/**
	 * Open up an SQL query pane with a default tab name
	 * 
	 * @param e triggering event, ignored
	 */
	void openSqlBBtn_actionPerformed(ActionEvent e) {
		QueryPanel qryPanel = new QueryPanel();
		actionPane.addTab("SQL" + (panelSeq++), image1, qryPanel);
		actionPane.setSelectedComponent(qryPanel);
	}
	
	/**
	 * Open up a schema view pane with a default tab name
	 * 
	 * @param e triggering event, ignored
	 */
	void openSchemaBBtn_actionPerformed(ActionEvent e) {
		SchemaPanel schemaPanel = new SchemaPanel();
		actionPane.addTab("Schema" + (panelSeq++), image2, schemaPanel);
		actionPane.setSelectedComponent(schemaPanel);
	}
	
	/**
	 * Open up the help menu dialog, which is all but non-existent
	 * 
	 * @param e triggering event, ignored
	 */
	void helpBBtn_actionPerformed(ActionEvent e) {
		HelpDialog dlg = new HelpDialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setVisible(true);
	}
	
	/**
	 * Perform a "SAVE" operation against the query panel
	 * 
	 * @param e triggering event, ignored
	 */
	void saveBBtn_actionPerformed(ActionEvent e) {
		Component current = actionPane.getSelectedComponent();
		if (current instanceof QueryPanel) {
			QueryPanel qryPanel = (QueryPanel)current;
			qryPanel.saveRequested();
		}
	}
	
	/**
	 * User has selected a different tab in the QUERY/SCHEMA pane; notify the
	 * appropriate panel that it is active and has the focus
	 * 
	 * @param ce triggering event, ignored
	 */
	void actionPane_stateChanged(ChangeEvent ce) {
		Component panel = actionPane.getSelectedComponent();
		if (panel != null  &&  panel instanceof MenuHandler) {
			((MenuHandler)panel).becomeActive();
		}
	}
	
	/**
	 * User has selected a different tab in the SERVER/FAVORITES control pane; notify
	 * the appropriate panel that it is active and has the focus
	 * 
	 * @param ce triggering event, ignored
	 */
	void controlPane_stateChanged(ChangeEvent ce) {
		Component panel = controlPane.getSelectedComponent();
		if (panel != null  &&  panel instanceof MenuHandler) {
			((MenuHandler)panel).becomeActive();
		}
	}
	
	/**
	 * Called on application start-up, this method load the list of registered
	 * servers and favorite SQL by reading them from the serialized stream
	 */
	void loadDefaults() {
		String userHome = System.getProperty("user.home", ".");
		String filename = userHome + "/.sql-tool-defaults";
		ObjectInputStream ois = null;
		
		try {
			FileInputStream fis = new FileInputStream(filename);
			GZIPInputStream zis = new GZIPInputStream(fis);
			ois = new ObjectInputStream(zis);
			Object serverList = ois.readObject();
			serverListPanel.setTreeModel(serverList);
			Object favoriteList = ois.readObject();
			favoriteListPanel.setTreeModel(favoriteList);
			updateServerList();
		} catch (Exception ex) {
			System.out.println("EX: " + ex);
		} finally {
			try { ois.close(); } catch (Exception ex2) { }
		}
	}
	
	/**
	 * Called on application shut-down, this method writes the list of 
	 * registered servers and favorite SQL onto a serialized stream
	 *
	 */
	void saveDefaults() {
		String userHome = System.getProperty("user.home", ".");
		String filename = userHome + "/.sql-tool-defaults";
		ObjectOutputStream oos = null;
		
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			GZIPOutputStream zos = new GZIPOutputStream(fos);
			oos = new ObjectOutputStream(zos);
			oos.writeObject(serverListPanel.getTreeModel());
			oos.writeObject(favoriteListPanel.getTreeModel());
			oos.flush();
		} catch (Exception ex) {
			System.out.println("EX: " + ex);
		} finally {
			try { oos.close(); } catch (Exception ex2) { }
		}
	}
	
	
	// Set up listeners for the server-list panel (so we can notify the open
	// SQL panes of the new/updated server list
	private void addPanelTreeListeners() {
		serverListPanel.setTreeListener(new TreeModelListener() {
			public void treeNodesInserted(javax.swing.event.TreeModelEvent tme) {
				updateServerList();
			}
			public void treeNodesRemoved(javax.swing.event.TreeModelEvent tme) {
				updateServerList();
			}
			public void treeNodesChanged(javax.swing.event.TreeModelEvent tme) { }
			public void treeStructureChanged(javax.swing.event.TreeModelEvent tme) { }
		});
	}
	
	/**
	 * The list of servers has changed: send the new list to the tool-kit so it
	 * can manage the new list, and notify all listeners that there is a new
	 * list available.
	 */
	private void updateServerList() {
		java.util.List<DbDefinition> dbDefList = new ArrayList<DbDefinition>();
		for (Object what : serverListPanel.getNodeList()) {
			dbDefList.add((DbDefinition)what);
		}
		SqlToolkit.dbDefManager.setDbDefList(dbDefList);
	}
	
	
	/**
	 *  Get a file name (full path) for reading or writing data.  THIS METHOD IS
	 *  CURRENTLY NOT USED, SO IT COMMENTED OUT FOR NOW.
	 */
//	private String getFilePath(String title, int mode) {
//		FileDialog fd = new FileDialog(this, title, mode);
//		if (prevDir != null) {
//			fd.setDirectory(prevDir);
//		}
//		fd.show();
//		String name = fd.getFile();
//		if (name != null) {
//			prevDir = fd.getDirectory();
//			return prevDir + name;
//		}
//		return null;
//	}
	
	
	/**
	 * Set the menus that are to be shown, in the order specified by the manager.
	 * If some menu is "null", the default to the menu defined by this main
	 * application.  If it's still false, create a new [disabled] menu.
	 */
	private void updateMenus() {
		mainMenuBar.setVisible(false);
		mainMenuBar.removeAll();
		
		for (int i=0;  i<MenuManager.MENU_ORDER.length;  i++) {
			JMenu aMenu = SqlToolkit.menuManager.getMenu(MenuManager.MENU_ORDER[i]);
			if (aMenu == null) {
				aMenu = SqlToolkit.menuManager.getMenu(this, MenuManager.MENU_ORDER[i]);
				if (aMenu == null) {
					aMenu = new JMenu(MenuManager.MENU_ORDER[i]);
				}
			}
			if (aMenu.getItemCount() == 0) {
				aMenu.setEnabled(false);
			}
			mainMenuBar.add(aMenu);
		}
		mainMenuBar.setVisible(true);
	}


	/**
	 * Retrieve the class name that implements a given look-and-feel
	 */
	private String getLookAndFeelClass(String name) {
		UIManager.LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
		for (int i=0;  lafi!=null && i<lafi.length;  i++) {
			if (lafi[i].getName().equalsIgnoreCase(name)) {
				return lafi[i].getClassName();
			}
		}
		return null;
	}
}