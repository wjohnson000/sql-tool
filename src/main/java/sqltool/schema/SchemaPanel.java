package sqltool.schema;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.event.*;

import sqltool.TabParentPanel;
import sqltool.common.*;
import sqltool.common.db.DbInfoCache;
import sqltool.common.db.DbInfoModel;
import sqltool.server.DbDefinition;


/**
 * Display basic schema information, including tabs for the tables, views,
 * synonyms and procedures.
 * @author wjohnson000
 *
 */
public class SchemaPanel extends TabParentPanel implements Observer {

	private static final long serialVersionUID = 5853109474530428028L;

	private static final String INFO_TAB      = "DB Info";
	private static final String CUSTOM_TAB    = "Custom";
	
	private JComboBox serverListCB = new JComboBox();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JLabel jLabel1 = new JLabel();
	private JPanel mainPanel = new JPanel();
	private JButton refreshBtn = new JButton();

	private InfoPanel        infoPanel    = new InfoPanel();
	private DbCustomPanel    customPanel  = new DbCustomPanel();
	
	
	/**
	 * Create the main panel:
	 *    -- drop-down list of server
	 *    -- drop-down list of schemas
	 *    -- tabbed pane that will hold child panels
	 */
	public SchemaPanel() {
		final MenuHandler mHandler = this;
		
		jLabel1.setText("Database server:");
		serverListCB.setMaximumRowCount(24);

		serverListCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dbSelectAndRefresh(ae, false);
			}
		});
		
		refreshBtn.setEnabled(true);
		refreshBtn.setText("Refresh");
		refreshBtn.setToolTipText("Refresh schema data");
		refreshBtn.setMargin(new Insets(0, 2, 0, 2));
		refreshBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dbSelectAndRefresh(ae, true);
			}
		});
		refreshBtn.setFocusPainted(false);

		mainPanel.setLayout(new GridBagLayout());
		mainPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				checkOptionMenu(e);
			}
			public void mouseReleased(MouseEvent e) {
				checkOptionMenu(e);
			}
		});
		
		mainPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 6, 0, 2), 0, 0));
		mainPanel.add(serverListCB, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 2, 5, 0), 0, 0));
		mainPanel.add(refreshBtn, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 6, 0, 2), 0, 0));
		
		tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tabbedPane_stateChanged(e);
			}
		});
		tabbedPane.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				checkOptionMenu(e);
			}
			public void mouseReleased(MouseEvent e) {
				checkOptionMenu(e);
			}
		});
		tabbedPane.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fe) {
				SqlToolkit.menuManager.setCurrentOwner(mHandler);
			}
		});
		
		tabbedPane.add(infoPanel, INFO_TAB);
		tabbedPane.add(customPanel, CUSTOM_TAB);
		
		add(mainPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		updateServerList();
		
		SqlToolkit.userConfig.addObserver(this);
	}
	
	/**
	 *  Set the list of servers ...
	 */
	protected void updateServerList() {
		DbDefinition dbTemp = (DbDefinition)serverListCB.getSelectedItem();
		serverListCB.setModel(new DefaultComboBoxModel(SqlToolkit.dbDefManager.getDbDefList().toArray()));
		if (dbTemp != null) {
			serverListCB.setSelectedItem(dbTemp);
		}
		dbSelectAndRefresh(null, false);
	}
	
	/**
	 * Clean-up things when the panel is going away
	 */
	public void pleaseCleanUp() {
		SqlToolkit.userConfig.deleteObserver(this);
		SqlToolkit.menuManager.unregisterOwner(this);

		// Help the GC process a little ...
		removeAll();
		customPanel  = null;
		serverListCB = null;

	}
	
	
	/**
	 * New tab selected
	 */
	private void tabbedPane_stateChanged(ChangeEvent ce) {
		buildOptionPopupMenu();
	}


	/* (non-Javadoc)
	 * @see sqltool.TabParentPanel#buildOptionPopupMenu()
	 */
	@Override
	protected void buildOptionPopupMenu() {
		super.buildOptionPopupMenu();
		SqlToolkit.menuManager.setCurrentOwner(this);
	}
	
	
	/**
	 * Show the pop-up menu in the correct location:  different OS-es have
	 * pop-up triggers associated with either mouse-pressed or mouse-released
	 * events, so we check for both
	 */
	private void checkOptionMenu(MouseEvent e) {
		SqlToolkit.menuManager.setCurrentOwner(this);
		JPopupMenu optionMenu = SqlToolkit.menuManager.getPopup();
		if (e.isPopupTrigger() && optionMenu != null) {
			optionMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	

	/**
	 * Select and optionally refresh the selected database information
	 * 
	 * @param ae action event that triggered this event (or null)
	 * @param refresh boolean flag indicating if the data is to be
	 *        refreshed/reloaded from the Db or not ...
	 * 
	 */
	private void dbSelectAndRefresh(ActionEvent ae, boolean refresh) {
		dbDef = (DbDefinition)serverListCB.getSelectedItem();
		if (dbDef == null) {
			JOptionPane.showMessageDialog(this, "No database server defined", "",
					JOptionPane.WARNING_MESSAGE);
		}
		
		Cursor saveCursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Connection conn = ConnectionManager.GetConnection(dbDef, true);
		DbInfoModel dbModel = DbInfoCache.GetInfoModel(conn);
		if (refresh) {
			dbModel.refreshData(conn);
		}
		setCursor(saveCursor);

		infoPanel.setModel(dbModel);
		customPanel.setModel(dbModel);

		// Disable the "Custom" tab if we have no data for this database
		tabbedPane.setEnabledAt(1, dbModel.isCustomModelDefined());
	}


	/**
	 * We are looking for events from the "DB-Definition Manager", which would indicate we
	 * need to update the list of database connections ...
	 */
	public void update(Observable source, Object arg) {
		if (source == SqlToolkit.dbDefManager) {
			updateServerList();
		}
	}
}
