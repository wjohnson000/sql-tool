package sqltool.common;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import sqltool.common.db.DbDefinitionManager;
import sqltool.config.UserConfig;


/**
 * Singleton that is our source for the menu manager, the database manager,
 * the user configuration, the logger, and the "Find" dialog.  Everything is
 * static.
 * 
 * @author wjohnson000
 *
 */
public class SqlToolkit {
	public static final MenuManager menuManager          = new MenuManager();
	public static final DbDefinitionManager dbDefManager = new DbDefinitionManager();
	public static final UserConfig userConfig            = new UserConfig();
	public static final AppLogger appLogger              = new AppLogger();

	private static FindDialog findDialog                 = null;

	/**
	 * Make the constructor private so it can't be instantiated.
	 */
	private SqlToolkit() { }

	/**
	 * Method to return the "FindDialog" ... only one for the entire application
	 * 
	 * @return "FindDialog" instance
	 */
	public static FindDialog getFindDialog(Component requester) {
		if (findDialog == null) {
			findDialog = new FindDialog(getParentFrame(requester), "Find/Replace");
		}
		return findDialog;
	}

	/**
	 * Convenience method to get the parent "Frame" for any given component
	 * @param component
	 * @return parent "Frame"
	 */
	public static Frame getParentFrame(Component comp) {
		Component parent = comp.getParent();
		while (! (parent == null || parent instanceof Frame)) {
			parent = parent.getParent();
		}
		return (parent == null) ? null : (Frame)parent;
	}

	/**
	 *  Get a file name (full path); by default we don't allow multiple
	 *  file name selections, and we don't want to show those nasty old
	 *  hidden file names ...
	 */
	public static String getFilePath(Component comp, String title, String prevDir, int mode, String buttonText) {
		// Find our parent frame and create the "choose" dialog
		Frame parent = SqlToolkit.getParentFrame(comp);
		JFileChooser jfcDialog = new JFileChooser();
		jfcDialog.setDialogTitle(title);

		// Set the default directory to the most recent directory used, and
		// disable hidden files and multiple selection
		if (prevDir != null) {
			jfcDialog.setCurrentDirectory(new java.io.File(prevDir));
		}
		jfcDialog.setFileHidingEnabled(true);
		jfcDialog.setMultiSelectionEnabled(false);

		// Show the dialog and wait for it to complete
		int retVal = jfcDialog.showDialog(parent, buttonText);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = jfcDialog.getSelectedFile();
			prevDir = file.getParent();
			return file.getAbsolutePath();
		}
		return null;
	}

}
