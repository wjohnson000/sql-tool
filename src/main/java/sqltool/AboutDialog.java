package sqltool;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import sqltool.common.SqlToolkit;
import sqltool.table.RowTableHeader;


//Class to manage the property name/value pairs for displaying in the table
class PropertyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 9109319128528247022L;

	String[][] propData = null;
	
	public PropertyTableModel() {
		ArrayList<String> propName = new ArrayList<String>(25);
		Enumeration<Object> keys = System.getProperties().keys();
		while (keys.hasMoreElements()) {
			propName.add(keys.nextElement().toString());
		}
		Collections.sort(propName);
		propData = new String[propName.size()][2];
		
		for (int i=0;  i<propName.size();  i++) {
			propData[i][0] = propName.get(i).toString();
			propData[i][1] = System.getProperty(propData[i][0], "?");
		}
	}
	
	// Return the number of columns in this model (exactly 2)
	public int getColumnCount() {
		return 2;
	}
	
	// Return the name of a specific column
	public String getColumnName(int col) {
		return (col == 0) ? "Property Name" : "Property Value";
	}
	
	// Return the Java 'Class' of a specific column
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
	
	// Return the number of rows in this model
	public int getRowCount() {
		return (propData == null) ? 0 : propData.length;
	}
	
	// Return the value at row "rowN", column "colN"
	public Object getValueAt(int rowN, int colN) {
		if (rowN < getRowCount())
			return propData[rowN][colN];
		else
			return "";
	}
}


public class AboutDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -4416850061564658910L;

	JPanel panel1 = new JPanel();
	JButton button1 = new JButton();
	String product = "Simple JDBC-compliant query tool";
	String version = "Version: 1.0, first 'official' release";
	String author = "Written by: Wayne Johnson";
	String comments = "Lite SQL query tool and database examiner ...";
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel imageLabel = new JLabel();
	JScrollPane jScrollPane1 = new JScrollPane();
	JTable propTable = new JTable();
	JPanel textPanel = new JPanel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel7 = new JLabel();
	JLabel jLabel8 = new JLabel();
	JLabel jLabel1 = new JLabel();
	
	public AboutDialog(Frame parent) {
		super(parent);
		SqlToolkit.appLogger.logDebug("Creating an 'AboutDialog' ...");
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
			propTable.setTableHeader(new RowTableHeader(propTable.getTableHeader().getColumnModel()));
			propTable.setModel(new PropertyTableModel());
			
			// Set the column widths and "nice" font renderers
			TableColumnModel tcm = propTable.getColumnModel();
			tcm.getColumn(0).setPreferredWidth(170);
			tcm.getColumn(1).setPreferredWidth(520);
		} catch(Exception e) {
			e.printStackTrace();
		}
		pack();
	}
	
	/**Component initialization*/
	private void jbInit() throws Exception  {
		this.setTitle("About");
		setResizable(false);
		panel1.setLayout(gridBagLayout1);
		button1.setMargin(new Insets(0, 6, 0, 6));
		button1.setText("OK");
		button1.addActionListener(this);
		imageLabel.setIcon(new ImageIcon(AboutDialog.class.getResource("sqlLite.gif")));
		textPanel.setLayout(gridBagLayout2);
		jLabel5.setText(product);
		jLabel6.setText(author);
		jLabel7.setText(version);
		jLabel8.setText(comments);
		panel1.setMinimumSize(new Dimension(460, 400));
		panel1.setPreferredSize(new Dimension(460, 400));
		propTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jLabel1.setText("Properties:");
		this.getContentPane().add(panel1, null);
		panel1.add(button1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 8, 0), 0, 0));
		panel1.add(imageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(12, 12, 0, 0), 0, 0));
		panel1.add(jScrollPane1, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8, 8, 2, 8), 0, 0));
		panel1.add(textPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 8, 0, 0), 0, 0));
		textPanel.add(jLabel5,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
		textPanel.add(jLabel6,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 3, 0, 0), 0, 0));
		textPanel.add(jLabel7,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 3, 0, 0), 0, 0));
		textPanel.add(jLabel8,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 3, 0, 0), 0, 0));
		panel1.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 0), 0, 0));
		jScrollPane1.getViewport().add(propTable, null);
	}
	
	/**Overridden so we can exit when window is closed*/
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}
	
	/**Close the dialog*/
	void cancel() {
		dispose();
	}
	
	/**Close the dialog on a button event*/
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button1) {
			cancel();
		}
	}
}