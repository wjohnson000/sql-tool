package sqltool.config;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.*;


/**
 * Font chooser dialog.  It will allow the user to select pretty much any
 * system font and any reasonable size.
 * @author wjohnon000
 * 
 */
public class FontChooser extends JDialog {

	private static final long serialVersionUID = -2336241430491754262L;

//	============================================================================
//	Constants (min and max font size)
//	============================================================================
	private static int MIN_FONT_SIZE = 4;
	private static int MAX_FONT_SIZE = 32;
	
	
	/**
	 * Convenience method to return a font ...
	 */
	private static FontChooser FcDlg = null;
	public static Font GetFont(Frame parent) {
		if (FcDlg == null) {
			FcDlg = new FontChooser(parent, "", true);
			Dimension dlgSize = FcDlg.getPreferredSize();
			Dimension frmSize = parent.getSize();
			Point loc = parent.getLocation();
			FcDlg.setLocation((frmSize.width-dlgSize.width)/2+loc.x, (frmSize.height-dlgSize.height)/2+loc.y);
			FcDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			FcDlg.setModal(true);
		}
		
		// The "show()" method will block until the user does an ACCEPT or CANCEL,
		// at which point we'll either return the current font or null
		FcDlg.userAccepted = false;
		FcDlg.setVisible(true);
		return (FcDlg.userAccepted) ? FcDlg.currentFont: null;
	}
	
	
//	=============================================================================
//	INSTANCE variables
//	=============================================================================
	private static final String SAMPLE_TEXT =
		"abcdefghijklmnopqrstuvwxyz\n" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
		"1234567890!@#$%^&*()-_=+[]{};':,./<>?";
	private Font currentFont = null;
	private boolean userAccepted = false;
	
	
//	=============================================================================
//	UI elements, created by JBuilder
//	=============================================================================
	JPanel panel1 = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel fontSizeLabel = new JLabel();
	JButton smallButton = new JButton();
	JButton largeButton = new JButton();
	JPanel jPanel1 = new JPanel();
	JLabel fontNameLabel = new JLabel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JLabel sampleTextLabel = new JLabel();
	JScrollPane jScrollPane1 = new JScrollPane();
	JPanel jPanel2 = new JPanel();
	JButton cancelButton = new JButton();
	JButton acceptBtn = new JButton();
	JList fontList = new JList();
	JTextField sizeTF = new JTextField();
	JLabel filler = new JLabel();
	JTextArea sampleText = new JTextArea();
	

	/**
	 * Default constructor for users who don't care about anything.  It will
	 * create a "modal" dialog.
	 */
	public FontChooser() {
		this(null, "", false);
	}

	/**
	 * Create a font-choose with a title and the ability to control the
	 * dialog's modality.
	 * 
	 * @param frame parent frame
	 * @param title title
	 * @param modal TRUE for a modal dialog; FALSE otherwise
	 */
	public FontChooser(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			String[] javaFonts = {
			    Font.DIALOG,  Font.MONOSPACED, Font.SANS_SERIF,  Font.SERIF
			};
			ArrayList<String> concatList = new ArrayList<String>(Arrays.asList(javaFonts));
			concatList.addAll(Arrays.asList(getFontNames()));
			String[] concatArray = concatList.toArray(new String[concatList.size()]);
			fontList.setListData(concatArray);
			fontList.setSelectedIndex(0);
			pack();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create all of the dialog's UI components
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		panel1.setLayout(gridBagLayout1);
		this.setTitle("-- -- S E L E C T   A   F O N T -- --");
		fontSizeLabel.setFont(new java.awt.Font("Dialog", 1, 12));
		fontSizeLabel.setText("Font size:");
		smallButton.setFont(new java.awt.Font("Monospaced", 0, 12));
		smallButton.setMargin(new Insets(0, 2, 2, 2));
		smallButton.setText("<<");
		smallButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smallButton_actionPerformed(e);
			}
		});
		largeButton.setFont(new java.awt.Font("Monospaced", 0, 12));
		largeButton.setMargin(new Insets(0, 2, 2, 2));
		largeButton.setText(">>");
		largeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				largeButton_actionPerformed(e);
			}
		});
		fontNameLabel.setFont(new java.awt.Font("Dialog", 1, 12));
		fontNameLabel.setText("Font name");
		jPanel1.setLayout(gridBagLayout2);
		sampleTextLabel.setFont(new java.awt.Font("Dialog", 1, 12));
		sampleTextLabel.setToolTipText("");
		sampleTextLabel.setText("Sample text");
		cancelButton.setMargin(new Insets(2, 4, 2, 4));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		acceptBtn.setMargin(new Insets(2, 4, 2, 4));
		acceptBtn.setText("Accept");
		acceptBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptBtn_actionPerformed(e);
			}
		});
		filler.setText(" ");
		sizeTF.setFont(new java.awt.Font("Monospaced", 0, 12));
		sizeTF.setText("12");
		sizeTF.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizeTF_actionPerformed(e);
			}
		});
		fontList.setFont(new java.awt.Font("Dialog", 0, 11));
		fontList.setVisibleRowCount(12);
		fontList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				fontList_valueChanged(e);
			}
		});
		sampleText.setText(SAMPLE_TEXT);
		sampleText.setEditable(false);
		getContentPane().add(panel1);
		panel1.add(fontSizeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 8, 0, 0), 0, 0));
		panel1.add(smallButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 6, 2, 2), 0, 0));
		panel1.add(largeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 2, 0), 0, 0));
		panel1.add(jPanel1, new GridBagConstraints(0, 1, 5, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(fontNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 8, 2, 2), 90, 0));
		jPanel1.add(sampleTextLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 2, 2, 2), 240, 0));
		jPanel1.add(jScrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 8, 2, 2), 0, 0));
		jPanel1.add(sampleText, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		jScrollPane1.getViewport().add(fontList, null);
		panel1.add(jPanel2, new GridBagConstraints(0, 2, 5, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(acceptBtn, null);
		jPanel2.add(cancelButton, null);
		panel1.add(sizeTF, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 8, 2, 0), 36, 0));
		panel1.add(filler, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/**
	 * ACCEPT -- set 'userAccepted' to TRUE and close the dialog
	 */
	private void acceptBtn_actionPerformed(ActionEvent e) {
		userAccepted = true;
		dispose();
	}
	
	/**
	 * CANCEL -- set 'userAccepted' to FALSE and close the dialog
	 */
	private void cancelButton_actionPerformed(ActionEvent e) {
		userAccepted = false;
		dispose();
	}
	
	/**
	 * Call out to the system to get a list of font names
	 */
	private String[] getFontNames() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	
	
	/**
	 * User has selected a new font (family) name
	 */
	private void fontList_valueChanged(ListSelectionEvent e) {
		if (! e.getValueIsAdjusting()) {
			displayFont();
		}
	}
	
	
	/**
	 * Decrease the font size by 2 points
	 */
	private void smallButton_actionPerformed(ActionEvent e) {
		int fontSize = getFontSize();
		if (fontSize > MIN_FONT_SIZE) {
			sizeTF.setText("" + (fontSize-2));
			displayFont();
		}
	}
	
	
	/**
	 * Increase the font size by 2 points
	 */
	private void largeButton_actionPerformed(ActionEvent e) {
		int fontSize = getFontSize();
		if (fontSize < MAX_FONT_SIZE) {
			sizeTF.setText("" + (fontSize+2));
			displayFont();
		}
	}
	
	
	/**
	 * The user has key-ed in a new font value.  We don't let the user go less than
	 * 4 points or greater than 32 points
	 */
	private void sizeTF_actionPerformed(ActionEvent e) {
		int fontSize = getFontSize();
		if (fontSize < MIN_FONT_SIZE  ||  fontSize > MAX_FONT_SIZE) {
			sizeTF.setText("12");
		}
		displayFont();
	}
	
	
	/**
	 * Return a font size based on user input; if we garbage data in the field,
	 * default to 12 point
	 */
	private int getFontSize() {
		String fontSize = sizeTF.getText();
		int iSize = 0;
		try {
			iSize = Integer.parseInt(fontSize);
		} catch (Exception ex) {
			iSize = 12;
			sizeTF.setText("12");
		}
		return iSize;
	}
	
	
	/**
	 * Create a new plain font based on the FAMILY name and SIZE, and use it to
	 * display the sample text
	 */
	private void displayFont() {
		if (fontList.getSelectedIndex() > -1) {
			int   fontSize = getFontSize();
			String fontName = (String)fontList.getSelectedValue();
			currentFont = new Font(fontName, Font.PLAIN, fontSize);
			sampleText.setFont(currentFont);
			sampleTextLabel.setText("Sample text -- " + fontName);
		}
	}
}