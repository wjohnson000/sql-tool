package sqltool.config;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.*;

import sqltool.common.SqlToolkit;


/**
 * Editor for the "UserConfig", allowing the user to update the current
 * configuration details.
 * 
 * @author wjohnson000
 *
 */
public class UserConfigEditor extends JDialog {

    private static final long serialVersionUID = -4659119677519336714L;

    /**
     * Convenience method to open the configuration tool
     */
    private static UserConfigEditor UceDlg = null;
    public static void OpenUserConfig(Frame parent) {
        if (UceDlg == null) {
            UceDlg = new UserConfigEditor(parent, "User Configuration ...");
            Dimension dlgSize = UceDlg.getPreferredSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();
            UceDlg.setLocation((frmSize.width-dlgSize.width)/2+loc.x, (frmSize.height-dlgSize.height)/2+loc.y);
            UceDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }
        UceDlg.setVisible(true);
    }

    // UI stuff
    private int dialogRow;
    private boolean hasChanged = false;
    private Frame owner = null;
    private JTextField tabSpaceTF = null;
    private JTextField clickCountTF = null;
    private JTextField fieldDelimTF = null;
    private JTextField sqlDelimTF = null;
    private JTextField bodyDelimTF = null;
    private JTextField logFileTF = null;
    private JRadioButton singleQuoteRB = null;
    private JRadioButton doubleQuoteRB = null;
    private JRadioButton noQuoteRB = null;
    private JRadioButton noneLogRB = null;
    private JRadioButton infoLogRB = null;
    private JRadioButton debugLogRB = null;
    private ButtonGroup  quoteGroup = null;
    private ButtonGroup  logGroup = null;
    private Font plainFont = new java.awt.Font("Monospaced", Font.PLAIN, 11);
    private Font boldFont  = new java.awt.Font("Monospaced", Font.BOLD, 11);

    // Font stuff
    private Map<String,Font>   fontMap  = new HashMap<String,Font>(10);
    private Map<String,JLabel> descrMap = new HashMap<String,JLabel>(10);


    public UserConfigEditor(Frame owner, String title) {
        super(owner, title, true);
        this.owner = owner;
        buildUI();
    }

    /**
     * Construct the UI
     */
    private void buildUI() {
        getContentPane().setLayout(new GridBagLayout());

        // ====================================================================
        // Set the primary label
        // ====================================================================
        JLabel jLabel01 = new JLabel("FONT DEFINITIONS:");
        jLabel01.setFont(boldFont);

        getContentPane().add(
                jLabel01, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(12, 6, 0, 2), 160, 0));

        // ====================================================================
        // Add some spacing to make the panel prettier, and add the fonts
        // ====================================================================
        getContentPane().add(
                new JLabel(""), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(12, 6, 0, 2), 100, 0));

        dialogRow = 1;
        addFontLine("Query Editor:", UserConfig.FONT_QUERY_EDITOR);
        addFontLine("Query Result:", UserConfig.FONT_QUERY_RESULT);
        addFontLine("Schema Editor:", UserConfig.FONT_SCHEMA_EDITOR);
        addFontLine("Schema Result:", UserConfig.FONT_SCHEMA_RESULT);

        // ====================================================================
        // Set the secondary label
        // ====================================================================
        JLabel jLabel02 = new JLabel("OTHER PARAMETERS:");
        jLabel02.setFont(boldFont);

        getContentPane().add(
                jLabel02, new GridBagConstraints(0, dialogRow, 2, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(12, 6, 0, 2), 160, 0));

        // ====================================================================
        // Add a line for the command-line delimiter
        // ====================================================================
        dialogRow++;
        JLabel jLabel06 = new JLabel("SQL command delimiter:");
        jLabel06.setFont(plainFont);
        getContentPane().add(
                jLabel06, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        sqlDelimTF = new JTextField(5);
        sqlDelimTF.setText(SqlToolkit.userConfig.getSqlDelim());
        sqlDelimTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });
        getContentPane().add(
                sqlDelimTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the command-body delimiter
        // ====================================================================
        dialogRow++;
        JLabel jLabel07 = new JLabel("SQL body delimiter:");
        jLabel07.setFont(plainFont);
        getContentPane().add(
                jLabel07, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        bodyDelimTF = new JTextField(5);
        bodyDelimTF.setText(SqlToolkit.userConfig.getBodyDelim());
        bodyDelimTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });

        getContentPane().add(
                bodyDelimTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the tab spacing
        // ====================================================================
        dialogRow++;
        JLabel jLabel03 = new JLabel("Tab spacing:");
        jLabel03.setFont(plainFont);
        getContentPane().add(
                jLabel03, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        tabSpaceTF = new JTextField(5);
        tabSpaceTF.setText(""+SqlToolkit.userConfig.getTabSpacing());
        tabSpaceTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });
        getContentPane().add(
                tabSpaceTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the click count
        // ====================================================================
        dialogRow++;
        JLabel jLabel03a = new JLabel("Click count:");
        jLabel03a.setFont(plainFont);
        getContentPane().add(
                jLabel03a, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        clickCountTF = new JTextField(5);
        clickCountTF.setText(""+SqlToolkit.userConfig.getClickCount());
        clickCountTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });
        getContentPane().add(
                clickCountTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the field delimiter
        // ====================================================================
        dialogRow++;
        JLabel jLabel04 = new JLabel("Field delimiter:");
        jLabel04.setFont(plainFont);
        getContentPane().add(
                jLabel04, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        fieldDelimTF = new JTextField(5);
        fieldDelimTF.setText(SqlToolkit.userConfig.getFieldDelim());
        fieldDelimTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });
        getContentPane().add(
                fieldDelimTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the optional quota on character fields
        // ====================================================================
        dialogRow++;
        JLabel jLabel05 = new JLabel("Optional Quote:");
        jLabel05.setFont(plainFont);
        getContentPane().add(
                jLabel05, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        singleQuoteRB = new JRadioButton("Single");
        singleQuoteRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        doubleQuoteRB = new JRadioButton("Double");
        doubleQuoteRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        noQuoteRB = new JRadioButton("None");
        noQuoteRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        String quote = SqlToolkit.userConfig.getFieldQuote();
        if ("'".equals(quote)) {
            singleQuoteRB.setSelected(true);
        } else if ("\"".equals(quote)) {
            doubleQuoteRB.setSelected(true);
        } else {
            noQuoteRB.setSelected(true);
        }
        quoteGroup = new ButtonGroup();
        quoteGroup.add(singleQuoteRB);
        quoteGroup.add(doubleQuoteRB);
        quoteGroup.add(noQuoteRB);
        JPanel quotePanel = new JPanel();
        quotePanel.add(singleQuoteRB);
        quotePanel.add(doubleQuoteRB);
        quotePanel.add(noQuoteRB);
        getContentPane().add(
                quotePanel, new GridBagConstraints(1, dialogRow, 2, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the log file name
        // ====================================================================
        dialogRow++;
        JLabel jLabelXX = new JLabel("Log file (full path):");
        jLabelXX.setFont(plainFont);
        getContentPane().add(
                jLabelXX, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        logFileTF = new JTextField(32);
        logFileTF.setText(SqlToolkit.userConfig.getLogFile());
        logFileTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                hasChanged = true;
            }
        });
        getContentPane().add(
                logFileTF, new GridBagConstraints(1, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add a line for the logging level (NONE, INFO, DEBUG)
        // ====================================================================
        dialogRow++;
        JLabel jLabel05a = new JLabel("Debug Level:");
        jLabel05a.setFont(plainFont);
        getContentPane().add(
                jLabel05a, new GridBagConstraints(0, dialogRow, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        noneLogRB = new JRadioButton("None");
        noneLogRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        infoLogRB = new JRadioButton("Info");
        infoLogRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        debugLogRB = new JRadioButton("Debug");
        debugLogRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hasChanged = true;
            }
        });
        String logLevel = SqlToolkit.userConfig.getLogLevel();
        if ("INFO".equals(logLevel)) {
            infoLogRB.setSelected(true);
        } else if ("DEBUG".equals(logLevel)) {
            debugLogRB.setSelected(true);
        } else {
            noneLogRB.setSelected(true);
        }
        logGroup = new ButtonGroup();
        logGroup.add(noneLogRB);
        logGroup.add(infoLogRB);
        logGroup.add(debugLogRB);
        JPanel logPanel = new JPanel();
        logPanel.add(noneLogRB);
        logPanel.add(infoLogRB);
        logPanel.add(debugLogRB);
        getContentPane().add(
                logPanel, new GridBagConstraints(1, dialogRow, 2, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        // ====================================================================
        // Add the button line
        // ====================================================================
        dialogRow++;
        JButton acceptBtn = new JButton("Accept");
        acceptBtn.setFont(plainFont);
        acceptBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyChanges();
                dispose();
            }
        });

        JButton applyBtn  = new JButton("Apply");
        applyBtn.setFont(plainFont);
        applyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyChanges();
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(plainFont);
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(acceptBtn);
        btnPanel.add(applyBtn);
        btnPanel.add(cancelBtn);
        getContentPane().add(
                btnPanel, new GridBagConstraints(0, dialogRow, 3, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 6, 0, 2), 0, 0));

        pack();
    }

    /**
     * Add a UI entry showing the font, with a button to modify it
     * @param label label
     * @param key some key string to identify what font is being shown 
     */
    private void addFontLine(String label, final String key) {
        final int row = dialogRow++;
        Font aFont = SqlToolkit.userConfig.getFont(key);

        JLabel jLabel01 = new JLabel(label);
        jLabel01.setFont(plainFont);
        jLabel01.setForeground(Color.BLUE);
        getContentPane().add(
                jLabel01, new GridBagConstraints(0, row, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 12, 0, 2), 0, 0));

        JLabel fontDescr = new JLabel(getDescription(aFont));
        fontDescr.setFont(plainFont);
        descrMap.put(key, fontDescr);
        getContentPane().add(
                fontDescr, new GridBagConstraints(1, row, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 0, 2), 0, 0));

        JButton jButton01 = new JButton("Choose ...");
        jButton01.setFont(boldFont);
        getContentPane().add(
                jButton01, new GridBagConstraints(2, row, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 4, 0, 2), 0, 0));

        jButton01.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                chooseFont(key);
            }
        });

    }

    /**
     * Get the description of a font, which will consist of the font name
     * and size.
     * @param aFont font
     * @return description of that font
     */
    private String getDescription(Font aFont) {
        if (aFont == null) {
            return "<Undefined>";
        } else {
            return aFont.getFontName() + ", " + aFont.getSize();
        }
    }

    /**
     * Allow the user to choose a new font.  If a new font is chosen for a
     * particular entry, update the label and save the new font.
     * @param key identifier for what font is being changed
     */
    private void chooseFont(String key) {
        Font someFont = FontChooser.GetFont(owner);
        if (someFont != null) {
            hasChanged = true;
            JLabel fLabel = descrMap.get(key);
            fLabel.setText(getDescription(someFont));
            fontMap.put(key, someFont);
        }
    }

    /**
     * When the configuration is done, notify all interested parties of any
     * applicable changes.
     */
    private void applyChanges() {
        if (hasChanged) {
            // Set any new fonts
            for (Iterator<String> iter = fontMap.keySet().iterator();  iter.hasNext(); ) {
                String key  = iter.next();
                Font   font = fontMap.get(key);
                SqlToolkit.userConfig.setFont(key, font);
            }

            // Set the new tab spacing, making sure the user entered an integer value
            int tabsp = SqlToolkit.userConfig.getTabSpacing();
            try {
                tabsp = Integer.parseInt(tabSpaceTF.getText());
            } catch (Exception ex) {
                tabsp = SqlToolkit.userConfig.getTabSpacing();
            }
            tabSpaceTF.setText(""+tabsp);
            SqlToolkit.userConfig.setTabSpacing(tabsp);

            // Set the click count (for sorting table columns)
            int clkcnt = SqlToolkit.userConfig.getClickCount();
            try {
                clkcnt = Integer.parseInt(clickCountTF.getText());
            } catch (Exception ex) {
                clkcnt = SqlToolkit.userConfig.getClickCount();
            }
            SqlToolkit.userConfig.setClickCount(clkcnt);
            clickCountTF.setText("" + SqlToolkit.userConfig.getClickCount());

            // Set the field delimeter
            SqlToolkit.userConfig.setFieldDelim(fieldDelimTF.getText());

            // Set the optional quote character
            String quote = "";
            if (singleQuoteRB.isSelected()) {
                quote = "'";
            } else if (doubleQuoteRB.isSelected()) {
                quote = "\"";
            }
            SqlToolkit.userConfig.setFieldQuote(quote);

            // Set the field delimeter
            SqlToolkit.userConfig.setSqlDelim(sqlDelimTF.getText());

            SqlToolkit.userConfig.setBodyDelim(bodyDelimTF.getText());

            // Set the log file name
            SqlToolkit.userConfig.setLogFile(logFileTF.getText());

            // Set the log level
            String logLevel = "NONE";
            if (infoLogRB.isSelected()) {
                logLevel = "INFO";
            } else if (debugLogRB.isSelected()) {
                logLevel = "DEBUG";
            }
            SqlToolkit.userConfig.setLogLevel(logLevel);

            hasChanged = false;
            fontMap = new HashMap<String,Font>(10);
        }
    }
}
