package sqltool;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import sqltool.common.SqlToolkit;


/**
 * Main class.  This creates the UI and makes it visible.
 * 
 * @author wjohnson000
 * @version 1.0
 */

public class SqlToolMain {
	boolean packFrame = false;
	
	/**Construct the application*/
	public SqlToolMain() {
		SqlToolUI frame = new SqlToolUI();

		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame) {
			frame.pack();
		} else {
			frame.validate();
		}

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		frame.setVisible(true);

		enableShiftBackspace();
	}


	/**
	 * This method enables the "SHIFT-BACKSPACE" and "CTRL-BACKSPACE" keys for
	 * all text fields, text areas and text panes
	 */
	private void enableShiftBackspace() {
		String[] compTypes = {
				"TextArea",  "TextField",  "TextPane",  "EditorPane",
				"FormattedTextField",  "PasswordField"};
		for (String compType : compTypes) {
			InputMap inMap = (InputMap) UIManager.get(compType + ".focusInputMap");
			if (inMap != null) {
				Object backAction = inMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
				inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK), backAction);
				inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK), backAction);
			}
		}
	}


	/**Main method*/
	public static void main(String[] args) {
		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SqlToolMain();
			}
		});

		System.out.println("Locale: " + Locale.getDefault());

		// For purposes beyond the scope of this comment, we are going to log the current THREAD
		// activity every 30 seconds ...
		SqlToolkit.appLogger.logDebug("");
		SqlToolkit.appLogger.logDebug("========================================================================");
		SqlToolkit.appLogger.logDebug("Start the SQLTOOL at: " + new java.util.Date());
		SqlToolkit.appLogger.logDebug("========================================================================");
		JvmLogger.displayStartupInfo();
		while (true) {
			try { Thread.sleep(30000); } catch (Exception ex) { }
			JvmLogger.displayCurrentInfo();
		}
	}
}