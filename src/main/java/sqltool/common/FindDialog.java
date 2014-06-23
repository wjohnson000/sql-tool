package sqltool.common;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import sqltool.query.SqlDocument;


/**
 * Standard Find/Replace dialog for text fields.  Allow for separate find
 * and replace; a replace and find next; and replace all.  Allow for defining
 * the scope [all lines or selected lines].  Allow for forwards or backwards
 * operations.
 * 
 * @author wjohnson000
 *
 */
public class FindDialog extends JDialog {

	private static final long serialVersionUID = 1874939474938899231L;

	int findPosition = -1;
	boolean canReplace = false;

	JTextPane target;
	JLabel    findLabel = new JLabel();
	JLabel    replaceLabel = new JLabel();
	JLabel    statusLabel = new JLabel();

	JTextField findTF = new JTextField(20);
	JTextField replaceTF = new JTextField(20);

	JRadioButton fwdRB = new JRadioButton();
	JRadioButton bckRB = new JRadioButton();
	JRadioButton allRB = new JRadioButton();
	JRadioButton sltRB = new JRadioButton();
	ButtonGroup dirBG = new ButtonGroup();
	ButtonGroup scopeBG = new ButtonGroup();

	JCheckBox caseCB = new JCheckBox();

	JButton findBtn = new JButton();
	JButton replaceBtn = new JButton();
	JButton replaceFindBtn = new JButton();
	JButton replaceAllBtn = new JButton();
	JButton closeBtn = new JButton();

	JPanel topPanel = new JPanel();
	Box dirPanel = new Box(BoxLayout.Y_AXIS);
	Box scopePanel = new Box(BoxLayout.Y_AXIS);
	Box optionPanel = new Box(BoxLayout.Y_AXIS);


	/**
	 * Constructor takes owner frame and title, and creates a non-modal
	 * dialog so that the user can operate in both the main panel and this
	 * dialog simultaneously
	 * 
	 * @param owner Frame which "owns" this dialog
	 * @param title Dialog title
	 * @throws HeadlessException
	 */
	public FindDialog(Frame owner, String title) throws HeadlessException {
		super(owner, title, false);
		try {
			buildUI();
			enableButtons();
			pack();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * Set the target of this dialog, i.e., which editor we are supposed to do
	 * the search/replace in
	 * 
	 * @param editor JTextPane where there search and/or replace operations are
	 *               to be done
	 */
	public void setTarget(JTextPane editor) {
		target = editor;
		statusLabel.setText("");
		canReplace = target.isEditable();
	}

	/**
	 * Create and place all of the UI elements in the search dialog, but don't
	 * show it: the application that creates the dialog must be in charge of
	 * managing the visibility ...
	 */
	private void buildUI() {
		Color bkColor01 = new Color(240, 240, 240);
		Color bkColor02 = new Color(224, 224, 224);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().setBackground(bkColor01);
		setResizable(false);
		statusLabel.setForeground(Color.red);
		statusLabel.setFont(new java.awt.Font(statusLabel.getFont().getFamily(), 0, 11));

		// Set all of the labels
		findLabel.setText("Find:");
		replaceLabel.setText("Replace:");
		statusLabel.setText("");
		fwdRB.setText("Forward");
		bckRB.setText("Backward");
		allRB.setText("All");
		sltRB.setText("Selected Text");
		caseCB.setText("Case Sensitive");

		findBtn.setText("Find");
		replaceBtn.setText("Replace");
		replaceFindBtn.setText("Replace/Find");
		replaceAllBtn.setText("Replace All");
		closeBtn.setText("Close");

		// Add the radio buttons to their button groups
		dirBG.add(fwdRB);
		dirBG.add(bckRB);
		fwdRB.setSelected(true);

		scopeBG.add(allRB);
		scopeBG.add(sltRB);
		allRB.setSelected(true);
		sltRB.setEnabled(false);

		// Set up the button actions
		findTF.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent ke) {
				enableButtons();
			}
		});

		findBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doFind(true);
			}
		});
		replaceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkBulkStart();
				doReplace();
				checkBulkFinish();
			}
		});
		replaceFindBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkBulkStart();
				doReplaceFind();
				checkBulkFinish();
			}
		});
		replaceAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				checkBulkStart();
				doReplaceAll();
				checkBulkFinish();
			}
		});
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		});
		
		// Set up the internal panels
		topPanel.setLayout(new GridBagLayout());
		dirPanel.setBorder(BorderFactory.createTitledBorder("Direction"));
		scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));
		optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		topPanel.setBackground(bkColor01);
		fwdRB.setBackground(bkColor01);
		bckRB.setBackground(bkColor01);
		allRB.setBackground(bkColor01);
		sltRB.setBackground(bkColor01);
		findBtn.setBackground(bkColor02);
		replaceBtn.setBackground(bkColor02);
		replaceFindBtn.setBackground(bkColor02);
		replaceAllBtn.setBackground(bkColor02);

		// Create the TOP panel
		topPanel.add(findLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 0, 2), 0, 0));
		topPanel.add(findTF, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 6, 0, 2), 0, 0));
		topPanel.add(replaceLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 0, 2), 0, 0));
		topPanel.add(replaceTF, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 6, 0, 2), 0, 0));

		// Create the DIRECTION panel (box)
		dirPanel.add(fwdRB);
		dirPanel.add(bckRB);

		// Create the SCOPE panel (box)
		scopePanel.add(allRB);
		scopePanel.add(sltRB);

		// Create the OPTIONS panel (box)
		optionPanel.add(caseCB);

		// Add the elements to this frame
		getContentPane().add(topPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(8, 6, 0, 2), 0, 0));

		getContentPane().add(dirPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(16, 6, 0, 2), 0, 0));
		getContentPane().add(scopePanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(16, 6, 0, 2), 0, 0));

		getContentPane().add(optionPanel, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(16, 6, 0, 2), 0, 0));

		getContentPane().add(findBtn, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(16, 6, 0, 2), 0, 0));
		getContentPane().add(replaceFindBtn, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(16, 6, 0, 2), 0, 0));
		getContentPane().add(replaceBtn, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(4, 6, 0, 2), 0, 0));
		getContentPane().add(replaceAllBtn, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(4, 6, 0, 2), 0, 0));

		getContentPane().add(statusLabel, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new Insets(12, 6, 6, 6), 0, 0));
		getContentPane().add(closeBtn, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0,
				GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(12, 6, 6, 6), 0, 0));
	}

	/**
	 * Control which buttons are active depending on the current state of affairs.
	 * The 'FIND' and 'REPLACE ALL' buttons are active as long as there is
	 * something in the "findTF" field.  The 'REPLACE FIND' and 'REPLACE ALL'
	 * need a second condition, that being that we have actually found something,
	 * based on the "findPosition" instance variable
	 */
	private void enableButtons() {
		boolean okFind = findTF.getText().length() > 0;
		findBtn.setEnabled(okFind);
		replaceBtn.setEnabled(canReplace  &&  okFind  &&  findPosition >= 0);
		replaceFindBtn.setEnabled(canReplace  &&  okFind  &&  findPosition >= 0);
		replaceAllBtn.setEnabled(canReplace  &&  okFind);
	}

	/**
	 * Perform the FIND operation, looking for the next match of the string.  We
	 * first look starting from the current (caret) position.  If no match is
	 * found we then do another search starting at the beginning of the text.
	 * If we wrap around we issue a BEEP, informing the user that we either
	 * didn't find the text, or the text was found but we had to wrap.  Then
	 * see if we need to enable the buttons.
	 * 
	 * If the "caseCB" is selected, we need to do a case-sensitive find.  If
	 * it's not selected, do case-insensitive find.
	 * @return TRUE if we had to do a wrap, FALSE otherwise
	 */
	private boolean doFind(boolean doBeep) {
		boolean doWrap = false;
		// Get the text from the document, which will handle the CR/LF
		// better.  If that fails, get the text directly.
		String docText = "";
		try {
			docText = target.getDocument().getText(0, target.getDocument().getLength());
		} catch (BadLocationException e) {
			docText = target.getText();
		}

		String fndText = findTF.getText();

		// Convert the target text and the find text to lower-case if we
		// are doing a case-insensitive operation
		if (! caseCB.isSelected()) {
			docText = docText.toLowerCase();
			fndText = fndText.toLowerCase();
		}

		// Search forward or backwards, depending on the user's selection
		int newPos = 0;
		if (fwdRB.isSelected()) {
			newPos = docText.indexOf(fndText, target.getCaretPosition());
		} else {
			newPos = docText.lastIndexOf(fndText, target.getSelectionStart()-1);
		}

		// If we found the target string, select the matching text; if no
		// match was found, then wrap around to either the top or bottom,
		// depending on whether we are searching FWD or BACK.  If we find
		// the target string, select the matching text.
		//
		// If we have to wrap the search, output an audible "beep" whether
		// the text is found or not.
		if (newPos >= 0) {
			target.getCaret().setSelectionVisible(true);
			target.setCaretPosition(newPos);
			target.moveCaretPosition(newPos + fndText.length());
			statusLabel.setText("Text found ...");
		} else {
			doWrap = true;
			if (fwdRB.isSelected()) {
				newPos = docText.indexOf(fndText);
			} else {
				newPos = docText.lastIndexOf(fndText);			
			}
			if (newPos >= 0) {
				target.getCaret().setSelectionVisible(true);
				target.setCaretPosition(newPos);
				target.moveCaretPosition(newPos + fndText.length());
				statusLabel.setText("Search wrapped ...");
			} else {
				statusLabel.setText("Text not found ...");
			}
			if (doBeep) {
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
		}
		findPosition = newPos;
		enableButtons();
		return doWrap;
	}

	/**
	 * Perform a "REPLACE" operation; the current selected text must match what's in
	 * the 'find' field in order to proceed.  The operation is done in two steps:
	 *  -- delete the matching text
	 *  -- insert the new (replacement) text
	 * Then we select the new text, cause we're nice
	 */
	private void doReplace() {
		try {
			boolean match = false;
			int curPos = target.getSelectionStart();

			// Make sure we're handling the case-sensitive or case-insensitive option
			if (caseCB.isSelected()) {
				match = target.getSelectedText().equals(findTF.getText());
			} else {
				match = target.getSelectedText().equalsIgnoreCase(findTF.getText());
			}

			// If we have a match, remove the old string, insert the new string, and
			// adjust the text selection
			if (match) {
				target.getDocument().remove(curPos, findTF.getText().length());
				target.getDocument().insertString(curPos, replaceTF.getText(), null);
				target.getCaret().setSelectionVisible(true);
				target.setCaretPosition(curPos);
				target.moveCaretPosition(curPos + replaceTF.getText().length());
			}
		} catch (Exception ex) {
			System.out.println("EX: " + ex);
		}
	}

	/**
	 * Perform a "REPLACE" operation followed by another "FIND" operation; this
	 * allows the user to easily step through the matches and choose which ones
	 * to replace and which to skip over 
	 */
	private void doReplaceFind() {
		doReplace();
		doFind(true);
	}

	/**
	 * Perform a "REPLACE" operation on every match, w/out any chance to review
	 * the matches.
	 */
	private void doReplaceAll() {
		target.setCaretPosition(0);
		int count=0;
		boolean doWrap = doFind(false);
		while (! doWrap) {
			count++;
			doReplace();
			doWrap = doFind(false);
		}
		statusLabel.setText("Occurrences updated: " + count);
	}

	/**
	 * Method to enhance the performance of a REPLACE operation: if we are
	 * working with an underlying 'SqlDocument' instance, then flag this as
	 * a BULK-LOAD so we don't try and update the document styles until the end
	 */
	private void checkBulkStart() {
		if (target.getDocument() instanceof SqlDocument) {
			((SqlDocument)target.getDocument()).startBulkLoad();
		}
	}

	/**
	 * Method to enhance the performance of a REPLACE operation: if we are
	 * working with an underlying 'SqlDocument' instance, then flag this as
	 * the end of a BULK-LOAD so we can add the styles
	 */
	private void checkBulkFinish() {
		if (target.getDocument() instanceof SqlDocument) {
			((SqlDocument)target.getDocument()).finishBulkLoad();
		}
	}
}
