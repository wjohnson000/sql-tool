package sqltool.server;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;

import sqltool.common.ConnectionManager;
import sqltool.common.SqlToolkit;


/**
 * Dialog (UI) for editing a database definition ({@link DbDefinition})
 * @author wjohnson000
 *
 */
public class DbDefinitionEditorUI extends JDialog {

	private static final long serialVersionUID = -8234847914366660567L;

	protected boolean     isOK = true;
	private DbDefinition model = null;
	
	private JPanel panel1 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
    private JLabel jLabel6 = new JLabel();
	private JLabel jLabel7 = new JLabel();
	private JLabel jLabel8 = new JLabel();
    private JLabel jLabel9 = new JLabel();
	private JTextField userTF = new JTextField();
	private JTextField aliasTF = new JTextField(36);
	private JTextField testQueryTF = new JTextField(36);
	private JPasswordField passwordTF = new JPasswordField();
	private JCheckBox isActiveCB = new JCheckBox();
	private JComboBox driverListCB = new JComboBox();
	private JComboBox urlListCB = new JComboBox();
	private JPanel jPanel1 = new JPanel();
	private JButton cancelBtn = new JButton();
	private JButton acceptBtn = new JButton();
	private JButton testBtn = new JButton();
	private JButton copyPasswordBtn = new JButton();


	/**
	 * Default constructor
	 */
	public DbDefinitionEditorUI() {
		this(null, "Database Connection Editor", false);
	}

	/**
	 * Create the UI, the components, lay everything out
	 * @param frame parent frame
	 * @param title frame title
	 * @param modal flag indicating if this is modal or not
	 */
	public DbDefinitionEditorUI(Frame frame, String title, boolean modal) {
		super(frame, title, modal);

		try {
			jbInit();
			isOK = true;
			this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			driverListCB.setEditable(true);
			driverListCB.setModel(new DefaultComboBoxModel(SqlToolkit.dbDefManager.getDriverNames().toArray()));
			driverListCB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					setUrlList();
				}
			});
			urlListCB.setEditable(true);
			passwordTF.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ke) {
					setPasswordToolTip();
				}
			});
			pack();
			setSize(640,240);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create all of the components
	 * @throws Exception
	 */
	void jbInit() throws Exception {
		setSize(720,175);
		panel1.setLayout(gridBagLayout1);
		jLabel2.setText("JDBC driver:");
		jLabel3.setText("Connection string:");
		jLabel4.setText("User:");
		jLabel5.setText("Password");
		jLabel6.setText("Test query:");
		jLabel7.setText(" ");
        jLabel8.setText("Alias:");
        jLabel9.setText("Is Active:");

        cancelBtn.setMargin(new Insets(0, 4, 0, 4));
		cancelBtn.setText("cancel");
		cancelBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelBtn_actionPerformed(e);
			}
		});

		acceptBtn.setMargin(new Insets(0, 4, 0, 4));
		acceptBtn.setText("accept");
		acceptBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptBtn_actionPerformed(e);
			}
		});

		testBtn.setMargin(new Insets(0, 4, 0, 4));
		testBtn.setText("test connection");
		testBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testBtn_actionPerformed(e);
			}
		});

        copyPasswordBtn.setMargin(new Insets(0, 4, 0, 4));
        copyPasswordBtn.setText("copy password");
        copyPasswordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyPasswordBtn_actionPerformed(e);
            }
        });

		getContentPane().add(panel1);
		panel1.add(jLabel7,   new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 300, 0));
		panel1.add(jLabel8,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(aliasTF,   new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel2,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(driverListCB,   new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel3,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(urlListCB,   new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel4,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(userTF,   new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel5,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(passwordTF,   new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
				,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel9,  new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
		panel1.add(isActiveCB,   new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jLabel6,  new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 2), 0, 0));
        panel1.add(testQueryTF,   new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 6), 0, 0));
		panel1.add(jPanel1,  new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(acceptBtn, null);
		jPanel1.add(cancelBtn, null);
		jPanel1.add(testBtn, null);
		jPanel1.add(copyPasswordBtn, null);
	}

	/**
	 * If a driver is selected, retrieve a list of all URLs associated with
	 * that driver and populate the combo-box 
	 */
	void setUrlList() {
		if (driverListCB.getSelectedIndex() >= 0) {
			String drv = (String)driverListCB.getSelectedItem();
			java.util.List<String> urlList = SqlToolkit.dbDefManager.getURLs(drv);
			String url = (String)urlListCB.getSelectedItem();
			if (! urlList.contains(url)) {
				urlList.add(url);
			}
			urlListCB.setModel(new DefaultComboBoxModel(urlList.toArray()));
			urlListCB.setSelectedItem(url);
		}
	}

	/**
	 * "ACCEPT" button selected ... update the underlying model with any
	 * new values the user may have entered.
	 * 
	 * @param e action
	 */
	void acceptBtn_actionPerformed(ActionEvent e) {
		isOK = true;
		Map<String,String> myValues = new TreeMap<String,String>();

		myValues.put("alias", aliasTF.getText());
		myValues.put("driver", (String)driverListCB.getSelectedItem());
		myValues.put("url", (String)urlListCB.getSelectedItem());
		myValues.put("user", userTF.getText());
		myValues.put("password", new String(passwordTF.getPassword()));
		myValues.put("testQuery", testQueryTF.getText());
		myValues.put("isActive", String.valueOf(isActiveCB.isSelected()));

		model.setValues(myValues);

		this.dispose();
	}

	/**
	 * "CANCEL" button selected ... ignore any changes
	 * @param e event
	 */
	void cancelBtn_actionPerformed(ActionEvent e) {
		isOK = false;
		this.dispose();
	}

	/**
	 * "TEST" button selected ... try to connect to the database if all
	 * the information has been set.
	 * @param e event
	 */
	void testBtn_actionPerformed(ActionEvent e) {
		String als = aliasTF.getText();
		String drv = (String)driverListCB.getSelectedItem();
		String url = (String)urlListCB.getSelectedItem();
		String usr = userTF.getText();
		String pwd = new String(passwordTF.getPassword());
		String tqy = testQueryTF.getText();

		// Save the current cursor and replace it with the WAIT cursor
		Cursor saveCursor = this.getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		if (drv.trim().length() > 0  &&  url.trim().length() > 0  &&
				usr.trim().length() > 0  &&  pwd.trim().length() > 0) {
			if (als.trim().length() == 0)
				als = drv + ":" + url + ":" + usr + ":" + pwd;
			als += ":_zzdummy_";
			DbDefinition dbTemp = new DbDefinition(als, drv, url, usr, pwd, tqy);
			Connection conn = ConnectionManager.GetConnection(dbTemp, false);
			JOptionPane.showMessageDialog(
					this,
					(conn == null ? ConnectionManager.GetConnectionError(dbTemp) : "Connection was successful"),
					"Test connection results",
					JOptionPane.INFORMATION_MESSAGE);
			try { conn.close(); } catch (Exception ex) { }
		}
		setCursor(saveCursor);
	}

	void copyPasswordBtn_actionPerformed(ActionEvent e) {
	    String pwd = new String(passwordTF.getPassword());
	    StringSelection selection = new StringSelection(pwd);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}

	/**
	 * Set the model for the dialog, a {@link DbDefinition}
	 * @param dbDef
	 */
	void setModel(DbDefinition dbDef) {
		model = dbDef;
		aliasTF.setText(model.getAlias());
		driverListCB.setSelectedItem(model.getDriver());
		urlListCB.setSelectedItem(model.getURL());
		userTF.setText(model.getUser());
		passwordTF.setText(model.getPassword());
		testQueryTF.setText(model.getTestQuery());
		isActiveCB.setSelected(model.isActive());
		setPasswordToolTip();
	}

	/**
	 * Set the password tool-tip, which is the plain text of the password.  This
	 * is useful for when the password can't be remembered.
	 */
	void setPasswordToolTip() {
		passwordTF.setToolTipText(new String(passwordTF.getPassword()));
	}
}