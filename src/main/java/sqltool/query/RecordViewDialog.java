package sqltool.query;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import sqltool.table.RowTableModel;


/**
 * For a selected record, show all of the field values as key/value
 * pairs in a formatted frame.  Possibly allow the user to update
 * any value.
 * 
 * @author wjohnson000
 *
 */
public class RecordViewDialog  extends JDialog {

	private static final long serialVersionUID = -5874837587420920890L;

	// =============================================================================
	//  I N S T A N C E   V A R I A B L E S
	// =============================================================================
	int                rowNumber = 0;
	RowTableModel      sqlModel  = null;
	HashMap<String,Object> origValue = new HashMap<String,Object>(100);
	HashMap<String,Object> newValue  = new HashMap<String,Object>(100);


	/**
	 * Only method to create the dialog editor
	 */
	public RecordViewDialog(Frame parent, String title, RowTableModel model, int rowNum) {
		super(parent, title, true);

		this.rowNumber = rowNum;
		this.sqlModel  = model;
		buildUI();
		this.pack();
	}


	/**
	 * Build the UI
	 */
	private void buildUI() {
		int dialogRow = 0;
		Font plainFont = new java.awt.Font("Monospaced", Font.PLAIN, 11);
		Font boldFont  = new java.awt.Font("Monospaced", Font.BOLD, 11);

		setSize(720,475);
		JScrollPane mainPane  = new JScrollPane();
		JPanel      dataPanel = new JPanel();

		dataPanel.setLayout(new GridBagLayout());
		getContentPane().add(mainPane);
		mainPane.getViewport().add(dataPanel);
		mainPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Main label
		JLabel jLabel1 = new JLabel();
		jLabel1.setText("Data details for row # " + rowNumber);
		jLabel1.setFont(boldFont);
		dataPanel.add(
				jLabel1, new GridBagConstraints(0, dialogRow, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(12, 6, 10, 2), 160, 0));

		// Display each of the data elements
		for (int col=0;  col<sqlModel.getColumnCount();  col++) {
			dialogRow++;
			String colName = sqlModel.getColumnName(col);
			Object colValu = sqlModel.getValueAt(rowNumber, col);
			final String key = col + "." + colName;
			origValue.put(key, colValu);

			// Add the value
			JLabel jLabel2 = new JLabel(colName);
			jLabel2.setFont(plainFont);
			dataPanel.add(
					jLabel2, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
							GridBagConstraints.EAST, GridBagConstraints.NONE,
							new Insets(2, 32, 0, 2), 0, 0));

			// Add the value
			JTextField jValueTF = new JTextField(80);
			jValueTF.setText(colValu == null ? "<null>" : colValu.toString());
			jValueTF.addKeyListener(new java.awt.event.KeyAdapter() {
				private String kkey = key;
				public void keyTyped(KeyEvent ke) {
					System.out.println("KE . src: " + ke.getSource().getClass().getName() + " --> " + ke.getSource());
					System.out.println(".  . cmp: " + ke.getComponent().getClass().getName() + " --> " + ke.getComponent());
					newValue.put(kkey, ke.getComponent());
				}
			});

			dataPanel.add(
					jValueTF, new GridBagConstraints(1, dialogRow, 1, 1, 1.0, 0.0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
							new Insets(2, 2, 0, 2), 0, 0));

		}
	}

}
