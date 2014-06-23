package sqltool;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Observer;
import javax.swing.*;
import sqltool.common.MenuHandler;
import sqltool.common.MenuManager;
import sqltool.common.SqlToolkit;
import sqltool.server.*;


/**
 * Parent class for all multi-tabbed panes.  It can handle menu events, and
 * will be notified when important things happen
 * 
 * @author wjohnson000
 *
 */
public abstract class TabParentPanel extends JPanel
		implements MenuHandler, Observer {

	private static final long serialVersionUID = 7342694939262503064L;

//	============================================================================
//	Don't put **ugly** scientific notation values in output file ...
//	============================================================================
	private static DecimalFormat dfWhole = new DecimalFormat("0");
	private static DecimalFormat dfFraction = new DecimalFormat("0.00");

//	============================================================================
//	Set up static menu options ...
//	============================================================================
	static final String MENU_CLOSE_TAB  = "close";
	static final String MENU_RENAME_TAB = "rename tab ...";
	
//	============================================================================
//	Instance variables
//	============================================================================
	protected DbDefinition dbDef = null;  // Current database connection
	protected String prevDir = null;      // Save the directory last used on a file search
	
	
	/**
	 * Constructor sets the layout and builds the menus
	 */
	public TabParentPanel() {
		this.setLayout(new BorderLayout());
		buildOptionPopupMenu();
		SqlToolkit.dbDefManager.addObserver(this);
	}

	
	/**
	 *  Each subclass must decide what to do when the panel goes away ...
	 */
	public abstract void pleaseCleanUp();
	
	/**
	 *  Build the base pop-up menu for all user options
	 *    -- rename this tab ...
	 *    -- close this tab ...
	 */
	protected void buildOptionPopupMenu() {
		SqlToolkit.menuManager.unregisterOwner(this);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU,     MENU_RENAME_TAB);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU,     MENU_CLOSE_TAB);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_RENAME_TAB);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_VIEW, MENU_CLOSE_TAB);
	}
	
	
	/**
	 * If a "Close" or "Rename" menu event comes along, handle it here
	 * @param ae event
	 */
	public void handleMenuEvent(ActionEvent ae) {
		if (ae.getSource() instanceof JMenuItem) {
			JMenuItem mi = (JMenuItem)ae.getSource();
			if (mi.getText().equalsIgnoreCase(MENU_CLOSE_TAB)) {
				closeTab();
			} else if (mi.getText().equalsIgnoreCase(MENU_RENAME_TAB)) {
				renameTab();
			}
		}
	}

	/**
	 * If this pane becomes active, let the menu-manager know that it is
	 * to handle all menu events
	 */
	public void becomeActive() {
		SqlToolkit.menuManager.setCurrentOwner(this);
	}
	
	/**
	 * Allow the user to re-name the tab
	 */
	private void renameTab() {
		String name = JOptionPane.showInputDialog(this, "Name: ", "Rename tab",
				JOptionPane.QUESTION_MESSAGE);
		if (name != null && name.length() > 0) {
			JTabbedPane parent = (JTabbedPane)this.getParent();
			parent.setTitleAt(parent.getSelectedIndex(), name);
		}
	}
	
	
	/**
	 * Allow the user to close this tab
	 */
	private void closeTab() {
		JTabbedPane parent = (JTabbedPane)this.getParent();
		parent.remove(this);
		pleaseCleanUp();
	}

	/**
	 * Show the "File-Chooser" dialog in file "OPEN" mode, i.e, we are looking
	 * for an existing file only.
	 * @param title dialog title
	 * @return full path of the file
	 */
	protected String getFilePathOpen(String title) {
		return SqlToolkit.getFilePath(this, title, prevDir, JFileChooser.OPEN_DIALOG, "Open");
	}

	/**
	 * Show the "File-Chooser" dialog in file "SAVE" mode, i.e, we are looking
	 * for an existing file or new file
	 * @param title dialog title
	 * @return full path of the file
	 */
	protected String getFilePathSave(String title) {
		return SqlToolkit.getFilePath(this, title, prevDir, JFileChooser.SAVE_DIALOG, "Save");
	}

	
	/**
	 *  Massage a value before printing
	 */
	protected String formatForCSV(Object obj) {
		String res = "";
		if (obj != null) {
			String sVal = obj.toString();
			if (obj instanceof Double  ||  obj instanceof Float) {
				sVal = dfFraction.format(obj);
	    		if (sVal.endsWith(".00")) {
	    			sVal = dfWhole.format(obj);
	    		}				
			}
			res = sVal.replace('\n','-').replace('\r','-').replace('"','\'');
		}
		return res;
	}
}
