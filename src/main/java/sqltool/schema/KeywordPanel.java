/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sqltool.schema;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import sqltool.common.db.DbInfoModel;


/**
 * Simple panel that can show the keywords and built-in function names for a
 * specific database connection
 * @author wjohnson000
 *
 */
public class KeywordPanel extends JPanel {

	private static final long serialVersionUID = -717493574703320675L;

	private static final Rectangle TOP_OF_PANE = new Rectangle(0, 0, 1, 1);
	
	private DbInfoModel myModel    = null;
	
	private JPanel       cbPanel   = new JPanel();
	private JLabel       infoLabel = new JLabel();
	private ButtonGroup  onlyBG    = new ButtonGroup();
	private JRadioButton kwRB      = new JRadioButton();
	private JRadioButton fnRB      = new JRadioButton();
	private JCheckBox    sysFnCB   = new JCheckBox();
	private JCheckBox    strFnCB   = new JCheckBox();
	private JCheckBox    numFnCB   = new JCheckBox();
	private JCheckBox    dttFnCB   = new JCheckBox();
	private JTextArea    textArea  = new JTextArea();
	private JScrollPane  textPane  = new JScrollPane();
	
	/**
	 * Create the panel, the components, and lay everything out
	 */
	public KeywordPanel() {
		
		// Set all of the fixed text display values
		infoLabel.setText("View keywords and functions ...");
		infoLabel.setForeground(Color.blue);
		kwRB.setText("Keyword List");
		fnRB.setText("Function List");
		sysFnCB.setText("System");
		strFnCB.setText("String");
		numFnCB.setText("Numeric");
		dttFnCB.setText("Date/Time");
		
		// Connect the two radio buttons
		onlyBG.add(kwRB);
		onlyBG.add(fnRB);
		kwRB.setSelected(true);
		
		// Initially disable all of the check boxes
		sysFnCB.setEnabled(false);
		sysFnCB.setFocusPainted(false);
		sysFnCB.setSelected(true);
		strFnCB.setEnabled(false);
		strFnCB.setFocusPainted(false);
		numFnCB.setEnabled(false);
		numFnCB.setFocusPainted(false);
		dttFnCB.setEnabled(false);
		dttFnCB.setFocusPainted(false);
		
		// Create an action listener to handle events on ALL radio/check buttons
		ActionListener onlyAL = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDisplay();
			}
		};
		
		kwRB.addActionListener(onlyAL);
		fnRB.addActionListener(onlyAL);
		sysFnCB.addActionListener(onlyAL);
		strFnCB.addActionListener(onlyAL);
		numFnCB.addActionListener(onlyAL);
		dttFnCB.addActionListener(onlyAL);
		
		// Add the check boxes to a panel 
		cbPanel.setBorder(BorderFactory.createEtchedBorder());
		cbPanel.add(sysFnCB, null);
		cbPanel.add(strFnCB, null);
		cbPanel.add(numFnCB, null);
		cbPanel.add(dttFnCB, null);
		
		// Configure the display area
		textArea.setText("<no list>");
		textArea.setDisabledTextColor(Color.darkGray);
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Monospaced", 0, 12));
		textPane.getViewport().add(textArea, null);
		
		// Start adding the components to the main layout panel
		setLayout(new GridBagLayout());
		add(infoLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(8, 6, 0, 2), 0, 0));
		add(kwRB, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 2, 0, 2), 0, 0));
		add(fnRB, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 2, 0, 2), 0, 0));
		add(cbPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 2, 0, 2), 0, 0));
		add(textPane, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(10, 2, 0, 2), 0, 0));
	}
	
	/**
	 * New database model [connection] has been selected: update all of the
	 * information in the panel
	 * @param aModel new model
	 */
	public void setModel(DbInfoModel aModel) {
		myModel = aModel;
		updateDisplay();
	}
	
	/**
	 * Display keywords, system functions, string functions, numeric
	 * functions and/or date-time functions, as appropriate
	 */
	private void updateDisplay() {
		sysFnCB.setEnabled(fnRB.isSelected());
		strFnCB.setEnabled(fnRB.isSelected());
		numFnCB.setEnabled(fnRB.isSelected());
		dttFnCB.setEnabled(fnRB.isSelected());
		
		textArea.setText("");
		if (myModel.isValid()) {
			if (kwRB.isSelected()) {
				displayList("KEYWORDS", myModel.getKeywordList());
			} else {
				if (sysFnCB.isSelected()) {
					displayList("SYSTEM FUNCTIONS", myModel.getSystemFuncList());
				}
				if (strFnCB.isSelected()) {
					displayList("STRING FUNCTIONS", myModel.getStringFuncList());
				}
				if (numFnCB.isSelected()) {
					displayList("NUMERIC FUNCTIONS", myModel.getNumericFuncList());
				}
				if (dttFnCB.isSelected()) {
					displayList("DATE/TIME FUNCTIONS", myModel.getDateTimeFuncList());
				}
			}
		}
		textArea.setCaretPosition(0);
		textArea.scrollRectToVisible(TOP_OF_PANE);
	}

	/**
	 * Display a list of objects
	 * @param title title
	 * @param source list of objects
	 */
	private void displayList(String title, String[] source) {
		textArea.append(title + "\n");
		for (int i=0;  source!=null && i<source.length;  i++) {
			textArea.append("   " + source[i] + "\n");
		}
		textArea.append("\n");
	}
}
