package sqltool;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import sqltool.common.SqlToolkit;


/**
 * When the user does an "import" or "export" operation we need to know if
 * they want to save the list of SQL favorites, or the list of Servers, or
 * both.  And if they save the list of Servers, what passcode [if any]
 * should be used to encode/decode the passwords
 *  
 * @author wjohnson
 *
 */
public class ImportExportDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -6049433004241218527L;

	// Values that will get set in the dialog
	private boolean    doSqlList = false;
	private boolean    doServerList = false;
	private String     passcode = null;
	
	// UI elements
	private JLabel     mainLabel;
	private JLabel     otherLabel;
	private JCheckBox  sqlCB;
	private JCheckBox  serverCB;
	private JTextField passcodeTF;
	private JButton    acceptBtn;
	private JButton    cancelBtn;
	private JPanel     buttonPnl;
	
	
	/**
	 * Constructor builds the UI but doesn't display it.  This dialog is
	 * restricted to the "MODAL" mode only.
	 * 
	 * @param parent Frame of the application that invoked the dialog
	 */
	public ImportExportDialog(Frame parent) {
		super(parent, "Select Export/Import options", true);
		SqlToolkit.appLogger.logDebug("Creating an 'ImportExportDialog' ...");
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			buildUI();
		} catch(Exception e) {
			e.printStackTrace();
		}
		pack();
		passcodeTF.requestFocus();
	}
	
	/**
	 * Value of the "Save SQL List" option
	 * 
	 * @return TRUE if the user selected this option, FALSE otherwise
	 */
	public boolean isSqlListOn() {
		return doSqlList;
	}
	
	/**
	 * Value of the "Save Server List" option
	 * 
	 * @return TRUE if the user selected this option, FALSE otherwise
	 */
	public boolean isServerListOn() {
		return doServerList;
	}
	
	/**
	 * Value of the passcode to be used to encrypt/decrypt any password
	 * 
	 * @return Passcode, or null if not selected
	 */
	public String getPasscode() {
		return passcode;
	}
	
	/**
	 * Create the UI elements and build the form
	 */
	private void buildUI() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		setResizable(false);
		
		mainLabel  = new JLabel("Select the data to be saved/restored:");
		sqlCB      = new JCheckBox("SQL favorites list", true);
		serverCB   = new JCheckBox("Server list", true);
		otherLabel = new JLabel("optional pass code: ");
		passcodeTF = new JTextField(20);
		acceptBtn  = new JButton("Accept");
		cancelBtn  = new JButton("Cancel");
		buttonPnl  = new JPanel();
		
		mainLabel.setForeground(Color.BLUE);
		serverCB.addActionListener(this);
		acceptBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		
		getContentPane().add(
				mainLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(8, 4, 0, 2), 0, 0));
		
		getContentPane().add(
				sqlCB, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 12, 0, 2), 0, 0));
		
		getContentPane().add(
				serverCB, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 12, 0, 2), 0, 0));
		
		getContentPane().add(
				otherLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 32, 0, 2), 0, 0));
		
		getContentPane().add(
				passcodeTF, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 2, 0, 2), 0, 0));
		
		
		buttonPnl.add(acceptBtn);
		buttonPnl.add(cancelBtn);
		getContentPane().add(
				buttonPnl, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 6, 0, 2), 0, 0));
	}
	
	/**
	 * Close the dialog by disposing of the window
	 */
	void cancel() {
		dispose();
	}
	
	/**
	 * Action event handler ... either the ACCEPT or CANCEL button initiates
	 * the action, or the SERVER checkbox controls the editability of the
	 * passcode field.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == acceptBtn) {
			doSqlList = sqlCB.isSelected();
			doServerList = serverCB.isSelected();
			passcode = passcodeTF.getText();
			if (passcode.trim().length() == 0) {
				passcode = null;
			}
			cancel();
		} else if (e.getSource() == cancelBtn) {
			doSqlList = false;
			doServerList = false;
			passcode = null;
			cancel();
		} else if (e.getSource() == serverCB) {
			passcodeTF.setEnabled(serverCB.isSelected());
		}
	}
	
}
