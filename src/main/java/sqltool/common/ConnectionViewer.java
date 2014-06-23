package sqltool.common;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

import sqltool.server.DbDefinition;


/**
 * View all connections, with the option of shutting down [closing] any one or
 * all of them.
 * 
 * @author wjohnson000
 *
 */
public class ConnectionViewer extends JDialog {
	
	private static final long serialVersionUID = 4413802270219843635L;

	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel7 = new JLabel();
	JComboBox serverListCB = new JComboBox();
	JTextField driverTF = new JTextField(24);
	JTextField urlTF = new JTextField(32);
	JTextField userTF = new JTextField();
	JPasswordField passwordTF = new JPasswordField();
	JTextField statusTF = new JTextField();
	JButton disconnectBtn = new JButton();
	JButton disconnectAllBtn = new JButton();
	JButton closeBtn = new JButton();

	
	/**
	 * Main constructor, taking the standard parameters for a JDialog
	 * instance.  After building the UI, pre-populate the list of
	 * open database connections ...
	 *
	 * @param frame Parent frame of the dialog, or null of none
	 * @param title Frame title
	 * @param modal Set whether the dialog is modal or not
	 */
	public ConnectionViewer(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			setDbList();
			this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			pack();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * DefaultScriptGenerator constructor ...
	 */
	public ConnectionViewer() {
		this(null, "Database Connection Editor", false);
	}
	
	
	/**
	 * Build the UI ... basically a list of connections currently
	 * being managed by the "ConnectionManager" class
	 * @throws Exception
	 */
	void jbInit() throws Exception {
		setSize(600,175);
		setResizable(false);
		
		jPanel1.setLayout(gridBagLayout1);
		jLabel1.setText(" ");
		jLabel2.setText("Open Connections:");
		jLabel3.setText("JDBC driver:");
		jLabel4.setText("Connection string:");
		jLabel5.setText("User:");
		jLabel6.setText("Password:");
		jLabel7.setText("Status:");
		
		driverTF.setEnabled(false);
		urlTF.setEnabled(false);
		userTF.setEnabled(false);
		passwordTF.setEnabled(false);
		statusTF.setEnabled(false);
		driverTF.setDisabledTextColor(Color.DARK_GRAY);
		urlTF.setDisabledTextColor(Color.DARK_GRAY);
		userTF.setDisabledTextColor(Color.DARK_GRAY);
		passwordTF.setDisabledTextColor(Color.DARK_GRAY);
		statusTF.setDisabledTextColor(Color.DARK_GRAY);
		
		disconnectBtn.setMargin(new Insets(0, 4, 0, 4));
		disconnectBtn.setText("disconnect");
		disconnectBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnectBtn_actionPerformed(e);
			}
		});
		
		disconnectAllBtn.setMargin(new Insets(0, 4, 0, 4));
		disconnectAllBtn.setText("disconnect all");
		disconnectAllBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnectAllBtn_actionPerformed(e);
			}
		});
		
		closeBtn.setMargin(new Insets(0, 4, 0, 4));
		closeBtn.setText("close");
		closeBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeBtn_actionPerformed(e);
			}
		});
		
		serverListCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkDbSelect(ae);
			}
		});
		
		getContentPane().add(jPanel1);
		jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 300, 0));
		
		jPanel1.add(jLabel2,   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 12, 0), 0, 0));
		jPanel1.add(serverListCB,   new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 12, 6), 0, 0));
		
		jPanel1.add(jLabel3,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		jPanel1.add(driverTF,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		
		jPanel1.add(jLabel4,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		jPanel1.add(urlTF,   new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		
		jPanel1.add(jLabel5,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		jPanel1.add(userTF,   new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		
		jPanel1.add(jLabel6,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		jPanel1.add(passwordTF,   new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		
		jPanel1.add(jLabel7,  new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		jPanel1.add(statusTF,   new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		
		jPanel1.add(jPanel2,  new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		jPanel2.add(disconnectBtn, null);
		jPanel2.add(disconnectAllBtn, null);
		jPanel2.add(closeBtn, null);
	}

	/**
	 * A database-definition has been selected.  Show its details.
	 * @param ae event
	 */
	void checkDbSelect(ActionEvent ae) {
		populateDbFields();
	}

	/**
	 * The "Disconnect" button has been pushed: close this connection and
	 * re-populate the list of active connections
	 * @param ae event
	 */
	void disconnectBtn_actionPerformed(ActionEvent ae) {
		DbDefinition dbDef = (DbDefinition)serverListCB.getSelectedItem();
		if (dbDef != null) {
			ConnectionManager.ShutDown(dbDef);
			setDbList();
		}
	}

	/**
	 * The "Disconnect All" button has been pushed: close all connections
	 * @param ae event
	 */
	void disconnectAllBtn_actionPerformed(ActionEvent ae) {
		ConnectionManager.ShutDown();
		setDbList();
	}

	/**
	 * Close this dialog
	 * @param ae event
	 */
	void closeBtn_actionPerformed(ActionEvent ae) {
		this.dispose();
	}

	/**
	 * Set the list of active connections
	 */
	private void setDbList() {
		List<DbDefinition> connList = new ArrayList<DbDefinition>(10);
		Iterator<DbDefinition> iter = ConnectionManager.GetDbDefList();
		while (iter.hasNext()) {
			connList.add(iter.next());
		}
		serverListCB.setModel(new DefaultComboBoxModel(connList.toArray()));
		populateDbFields();
	}

	/**
	 * Show the details for a selected database connection
	 */
	private void populateDbFields() {
		DbDefinition dbDef = (DbDefinition)serverListCB.getSelectedItem();
		if (dbDef == null) {
			driverTF.setText("");
			urlTF.setText("");
			userTF.setText("");
			passwordTF.setText("");
			statusTF.setText("");
		} else {
			driverTF.setText(dbDef.getDriver());
			urlTF.setText(dbDef.getURL());
			userTF.setText(dbDef.getUser());
			passwordTF.setText(dbDef.getPassword());
			try {
				Connection conn = ConnectionManager.GetConnection(dbDef, true);
				Statement stmt = conn.createStatement();
				statusTF.setText("Valid connection");
				stmt.close();
			} catch (SQLException sqlex) {
				statusTF.setText("Bad connection");
			}
		}
	}
}
