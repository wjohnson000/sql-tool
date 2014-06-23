package sqltool.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;


/**
 * Set of menus -- menu-bar menus, pop-up menu -- for a given UI object.
 * @author wjohnson000
 *
 */
public class MenuSet {
	
//	============================================================================
//	Instance variables
//	-- myMenus:  a "map" of all menus (JMenu instances)
//	-- myPopup:  the "popup" menu associated with this object
//	============================================================================
	Map<String,JMenu> myMenus = new HashMap<String,JMenu>();
	JPopupMenu myPopup = null;


	/**
	 * Return the menu for a certain main menu-bar type
	 * @param type main menu-bar type
	 * @return menu for that type
	 */
	public JMenu getMenu(String type) {
		return myMenus.get(type);
	}

	/**
	 * Return the pop-up menu
	 * @return pop-up menu
	 */
	public JPopupMenu getPopup() {
		return myPopup;
	}

	/**
	 * Create a new menu item and add it to the menu
	 * @param receiver menu-handler for this menu
	 * @param type menu-bar type
	 * @param label menu label
	 */
	public void addMenu(final MenuHandler receiver, String type, String label) {
		JMenuItem menuItem = new JMenuItem(label);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				receiver.handleMenuEvent(ae);
			}
		});
		Container menu = getMenuContainer(type);
		menu.add(menuItem);
	}

	/**
	 * Create a new radio-button menu item and add it to the menu
	 * @param receiver menu-handler for this menu
	 * @param type menu-bar type
	 * @param label menu label
	 */
	public void addMenu (final MenuHandler receiver, String type, String label,
			ButtonGroup menuBG, boolean isSelected) {
		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(label);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				receiver.handleMenuEvent(ae);
			}
		});
		menuBG.add(menuItem);
		menuItem.setSelected(isSelected);
		Container menu = getMenuContainer(type);
		menu.add(menuItem);
	}

	/**
	 * Add a separator to a menu
	 * @param type menu-bar type
	 */
	public void addSeparator(String type) {
		Container menu = getMenuContainer(type);
		if (menu instanceof JMenu) {
			((JMenu)menu).addSeparator();
		} else if (menu instanceof JPopupMenu) {
			((JPopupMenu)menu).addSeparator();
		}
	}

	/**
	 * Determine if a menu is currently enabled
	 * @param type menu-bar type
	 * @param label menu label
	 * @return TRUE if it's enabled; FALSE otherwise
	 */
	public boolean isEnabled(String type, String label) {
		JMenuItem menuItem = null;
		if (MenuManager.POPUP_MENU.equalsIgnoreCase(type)) {
			menuItem = getOptionMenuItem(label);
		} else {
			menuItem = getMenuItem(type, label);
		}
		return (menuItem == null) ? false : menuItem.isEnabled();
	}

	/**
	 * Enable or disable a menu option, either in a pop-up or a regular
	 * menu
	 * @param type menu-bar type
	 * @param label menu label
	 * @param isEnabled TRUE to enable a menu, FALSE to disable a menu
	 */
	public void setEnabled(String type, String label, boolean isEnabled) {
		JMenuItem menuItem = null;
		if (MenuManager.POPUP_MENU.equalsIgnoreCase(type)) {
			menuItem = getOptionMenuItem(label);
		} else {
			menuItem = getMenuItem(type, label);
		}
		if (menuItem != null) {
			menuItem.setEnabled(isEnabled);
		}
	}

	/**
	 * Check to see if a menu option is selected; this is mainly for a radio
	 * button menu
	 * @param type menu-bar type
	 * @param label menu label
	 * @return TRUE if the menu is selected; FALSE otherwise
	 */
	public boolean isSelected(String type, String label) {
		JMenuItem menuItem = null;
		if (MenuManager.POPUP_MENU.equalsIgnoreCase(type)) {
			menuItem = getOptionMenuItem(label);
		} else {
			menuItem = getMenuItem(type, label);
		}
		return (menuItem == null) ? false : menuItem.isSelected();
	}

	/**
	 * Flag a menu option as selected; this is mainly for a radio button menu
	 * @param type menu-bar type
	 * @param label menu label
	 * @param isSelected TRUE to select it; FALSE to deselect id
	 */
	public void setSelected(String type, String label, boolean isSelected) {
		JMenuItem menuItem = null;
		if (MenuManager.POPUP_MENU.equalsIgnoreCase(type)) {
			menuItem = getOptionMenuItem(label);
		} else {
			menuItem = getMenuItem(type, label);
		}
		if (menuItem != null) {
			menuItem.setSelected(isSelected);
		}
	}

	/**
	 * Return the menu container, either the menu (JMenu) or popup (JPopupMenu) and
	 * return as a "Container"
	 * @param type menu-bar type
	 * @return menu container
	 */
	private Container getMenuContainer(String type) {
		Container res = null;
		if (MenuManager.POPUP_MENU.equals(type)) {
			if (myPopup == null) {
				myPopup = new JPopupMenu();
			}
			res = myPopup;
		} else {
			JMenu aMenu = getMenu(type);
			if (aMenu == null) {
				aMenu = new JMenu(type);
				myMenus.put(type, aMenu);
			}
			res = aMenu;
		}
		return res;
	}


	/**
	 * Find the menu item given the type and menu label.  We need to
	 * protected against separators, which will be returned as "null"
	 * @param type menu-bar type
	 * @param label menu label
	 * @return actual menu instance
	 */
	private JMenuItem getMenuItem(String type, String label) {
		JMenuItem xxxMenu;
		JMenu aMenu = getMenu(type);
		for (int i=0;  aMenu!=null && i<aMenu.getItemCount();  i++) {
			xxxMenu = aMenu.getItem(i);
			if (xxxMenu != null  &&  xxxMenu.getText().equalsIgnoreCase(label)) {
				return xxxMenu;
			}
		}
		return null;
	}


	/**
	 * Find the pop-up menu item given the type and menu label.  We need to
	 * protected against separators, which will be returned as "null"
	 * @param label menu label
	 * @return actual menu instance
	 */
	private JMenuItem getOptionMenuItem(String label) {
		Component xxxMenu;
		for (int i=0;  myPopup!=null && i<myPopup.getComponentCount();  i++) {
			xxxMenu = myPopup.getComponent(i);
			if (xxxMenu instanceof JMenuItem) {
				if (((JMenuItem)xxxMenu).getText().equalsIgnoreCase(label)) {
					return (JMenuItem)xxxMenu;
				}
			}
		}
		return null;
	}
}
