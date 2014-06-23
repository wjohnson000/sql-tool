package sqltool.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;


/**
 * The manager for all menu events in the system.  Different UI components can
 * create and/register main-menu and pop-up menu items.  The menus can then
 * dynamically change depending on what UI component is currently "active".
 * 
 * @author wjohnson000
 */
public class MenuManager extends Observable {
//	============================================================================
//	tag that is sent out to observers ...
//	============================================================================
	public static final String OBSERVABLE_TAG = "observable.menu";
	
	
//	============================================================================
//	Main menu types
//	============================================================================
	public static final String MAIN_MENU_FILE = "File";
	public static final String MAIN_MENU_EDIT = "Edit";
	public static final String MAIN_MENU_VIEW = "View";
	public static final String MAIN_MENU_HELP = "Help";
	
	public static final String POPUP_MENU     = "popup";
	
	public static final String[] MENU_ORDER = {
		MenuManager.MAIN_MENU_FILE,
		MenuManager.MAIN_MENU_EDIT,
		MenuManager.MAIN_MENU_VIEW,
		MenuManager.MAIN_MENU_HELP
	};
	

	/** Active menu handler */
	MenuHandler menuHandler = null;

	/** Cache of menu-sets for all menu-handlers in the system*/
	Map<MenuHandler,MenuSet> allMenuSet  = new HashMap<MenuHandler,MenuSet>();
	
	
	/**
	 * Return the menu of a certain "type" for the current owner
	 * @param type type of menu being requested [menu bar, popup, etc]
	 */
	public JMenu getMenu(String type) {
		MenuSet menuSet = getCurrentMenuSet();
		return menuSet.getMenu(type);
	}
	
	/**
	 * Return the menu of a certain "type" for the specified owner
	 * @param type 
	 */
	public JMenu getMenu(MenuHandler mHandler, String type) {
		MenuSet menuSet = getMenuSet(mHandler);
		return menuSet.getMenu(type);
	}
	
	/**
	 * Return the popup menu for the current owner
	 */
	public JPopupMenu getPopup() {
		MenuSet menuSet = getCurrentMenuSet();
		return menuSet.getPopup();
	}
	
	/**
	 * Set the active owner (basically the component that has focus) so all
	 * subsequent operations work on their menus.  If this menu-handler is
	 * already active, do nothing
	 * @param menuHandler new menu handler
	 */
	public void setCurrentOwner(MenuHandler menuHandler) {
		setCurrentOwner(menuHandler, false);
	}
	
	/**
	 * Set the active owner (basically the component that has focus) so all
	 * subsequent operations work on their menus.  Notify all observers of
	 * the change, even if this menu-handler is already the active one if
	 * the "force" flag is true
	 * @param menuHandler new menu handler
	 * @param force if TRUE, notify all observers of the change even if
	 *        there is no change to the menu handler
	 */
	public void setCurrentOwner(MenuHandler menuHandler, boolean force) {
		if (this.menuHandler != menuHandler  ||  force) {
			this.menuHandler = menuHandler;
			broadcastChange();
		}
	}
	
	
	/**
	 * Unregister a "MenuSet" because a handler is going away ...
	 * @param mHandler the menu-handler to un-register
	 */
	public void unregisterOwner(MenuHandler mHandler) {
		allMenuSet.remove(mHandler);
		if (this.menuHandler == mHandler) {
			setCurrentOwner(null, true);
		}
	}
	
	
	/**
	 * Find the "MenuSet" for the current owner
	 */
	private MenuSet getCurrentMenuSet() {
		return getMenuSet(menuHandler);
	}
	
	
	/**
	 * Find the "MenuSet" for the given owner
	 * @param owner object that will handle menu actions
	 */
	private MenuSet getMenuSet(MenuHandler mHandler) {
		MenuSet menuSet;
		if (mHandler == null) {
			menuSet = new MenuSet();
		} else {
			menuSet = (MenuSet)allMenuSet.get(mHandler);
			if (menuSet == null) {
				menuSet = new MenuSet();
				allMenuSet.put(mHandler, menuSet);
			}
		}
		return menuSet;
	}
	
	
	/**
	 * Create a new menu item and add it to the appropriate menu
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 */
	public void addMenu(final MenuHandler mHandler, String type, String label) {
		MenuSet menuSet = getMenuSet(mHandler);
		menuSet.addMenu(mHandler, type, label);
	}
	
	
	/**
	 * Create a radio button menu item and add it to the appropriate menu
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 * @param menuBG button group to which this button will be added
	 * @param isSelected boolean to indicate if this is to be set or not
	 */
	public void addMenu (final MenuHandler mHandler, String type, String label,
			ButtonGroup menuBG, boolean isSelected) {
		MenuSet menuSet = getMenuSet(mHandler);
		menuSet.addMenu(mHandler, type, label, menuBG, isSelected);
	}
	
	
	/**
	 * Create a separator and add it to the appropriate menu
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 */
	public void addSeparator(final MenuHandler mHandler, String type) {
		MenuSet menuSet = getMenuSet(mHandler);
		menuSet.addSeparator(type);
	}


	/**
	 * Determine if a menu option is enabled
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 */
	public boolean isEnabled(final MenuHandler mHandler, String type, String label) {
		MenuSet menuSet = getMenuSet(mHandler);
		return menuSet.isEnabled(type, label);
	}
	
	
	/**
	 * Enable or disable a current menu item
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 */
	public void setEnabled(final MenuHandler mHandler, String type, String label,
			boolean isEnabled) {
		MenuSet menuSet = getMenuSet(mHandler);
		menuSet.setEnabled(type, label, isEnabled);
	}


	/**
	 * Determine if a menu option is enabled
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 */
	public boolean isSelected(final MenuHandler mHandler, String type, String label) {
		MenuSet menuSet = getMenuSet(mHandler);
		return menuSet.isSelected(type, label);
	}
	
	
	/**
	 * Enable or disable a current menu item
	 * 
	 * @param mHandler object that will handle menu actions
	 * @param type which menu is to be updated
	 * @param label new menu label
	 */
	public void setSelected(final MenuHandler mHandler, String type, String label,
			boolean isEnabled) {
		MenuSet menuSet = getMenuSet(mHandler);
		menuSet.setSelected(type, label, isEnabled);
	}
	

	/*
	 * This method notifies all interested observers that one or more
	 * of the menus have changed ...
	 */
	private void broadcastChange() {
		setChanged();
		notifyObservers(OBSERVABLE_TAG);
	}
}
