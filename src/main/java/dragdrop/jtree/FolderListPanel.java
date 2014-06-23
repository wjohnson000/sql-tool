package dragdrop.jtree;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import sqltool.common.MenuHandler;
import sqltool.common.MenuManager;
import sqltool.common.SqlToolkit;


/**
 * Panel that contains a folder-list, which is a tree managing any type
 * of objects.
 * @author wjohnson000
 *
 */
public class FolderListPanel extends JPanel implements MenuHandler {
	
	private static final long serialVersionUID = -3429099731815592651L;

//	============================================================================
//	Set up static menu options ...
//	============================================================================
	static final String MENU_FOLDER_CREATE = "create folder";
	static final String MENU_FOLDER_RENAME = "rename folder";
	static final String MENU_FOLDER_DELETE = "delete folder";
	static final String MENU_FOLDER_SORT   = "sort contents";
	static final String MENU_ITEM_CREATE   = "create ";
	static final String MENU_ITEM_EDIT     = "edit ";
	static final String MENU_ITEM_DELETE   = "delete ";
	
	/** Create a root folder node, which doesn't manage any object */
	private DndTreeNode rootFolder = new DndTreeNode("Folder List", null);

	/** Create a tree-model containing the new folder */
	private DefaultTreeModel folderModel = new DefaultTreeModel(rootFolder, true);

	/** The class for all folder items, dynamically created on request */
	private String folderItemClass = "dragdrop.jtree.DefaultFolderItem";

	/** An editor for all the items */
	private FolderItemEditor itemEditor = null;

	/** Default name for the items in the managed tree*/
	private String itemName = "item";

	/** Swing UI elements: a grid-bad layout, scroll pane, and drag-and-drop tree */
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JScrollPane jScrollPane1 = new JScrollPane();
	DndJTree folderTree = new DndJTree();

	/**
	 * Default constructor, which creates the panel and the pop-up menu
	 */
	public FolderListPanel() {
		try {
			jbInit();
			try {
				FolderItem item = (FolderItem) Class.forName(folderItemClass).newInstance();
				itemName = item.getMenuName();
			} catch (Exception ex) {
			}
			buildOptionPopupMenu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct all the UI objects
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		final MenuHandler mHandler = this;
		this.setLayout(gridBagLayout1);
		folderTree.setModel(folderModel);

		// Listen for keyboard, selection and mouse events on the tree
		folderTree.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				folderTree_keyReleased(e);
			}
		});
		folderTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				folderTree_valueChanged(e);
			}
		});
		folderTree.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				folderTree_mouseReleased(e);
			}
			public void mousePressed(MouseEvent e) {
				folderTree_mousePressed(e);
			}
		});

		// Add the scroll pane to the panel, and the tree to the scroll pane
		this.add(jScrollPane1, new GridBagConstraints(0, 0,
				GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		jScrollPane1.getViewport().add(folderTree, null);

		// Listen for focus events so we can make sure the correct menu is set
		folderTree.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fe) {
				SqlToolkit.menuManager.setCurrentOwner(mHandler);
			}
		});
	}
	
	/**
	 * Register a listener for tree changes
	 */
	public void setTreeListener(TreeModelListener tml) {
		folderModel.addTreeModelListener(tml);
	}
	
	/**
	 * Define the class name (full class path) of the object item managed by the
	 * tree. When the "create" menu is selected, we create one of these things.
	 */
	public void setItemClassName(String className) {
		folderItemClass = className;
		buildOptionPopupMenu();

		FolderItem item = null;
		try {
			item = (FolderItem) Class.forName(folderItemClass).newInstance();
			ImageIcon icon = item.getLeafIcon();
			if (icon != null) {
				folderTree.setLeafIcon(icon);
			}
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Name (label) that will be displayed with the root folder object
	 */
	public void setRootFolderName(String name) {
		if (name != null) {
			rootFolder.setLabel(name);
		}
	}
	
	/**
	 * Mandated method ... return the tree model managed by this panel
	 */
	public DefaultTreeModel getTreeModel() {
		return folderModel;
	}
	
	/**
	 * Set the tree model for this panel. We make sure that we get the correct
	 * object (type) before using it.  The object is actually managed by the
	 * TREE.
	 */
	public void setTreeModel(Object folderModel) {
		if (folderModel != null  &&  folderModel instanceof DefaultTreeModel) {
			this.folderModel = (DefaultTreeModel) folderModel;
			this.rootFolder = (DndTreeNode) this.folderModel.getRoot();
			folderTree.setModel(this.folderModel);
			folderTree.setSelectionRow(0);
		}
	}
	
	/**
	 * Return a list of all leaf nodes managed by this tree.  Since we don't know
	 * what type of object is being managed, we must return a list of "Object"
	 */
	public ArrayList<Object> getNodeList() {
		ArrayList<Object> res = new ArrayList<Object>(12);
		if (folderModel != null) {
			DndTreeNode root = (DndTreeNode) folderModel.getRoot();
			addNodesToList(root, res);
		}
		return res;
	}

	/**
	 * Retrieve child nodes recursively, given a node in the tree
	 * @param node point in tree from where objects are retrieved
	 * @param res list into which results are to be placed
	 */
	private void addNodesToList(DndTreeNode node, ArrayList<Object> res) {
		for (int i=0;  i<folderModel.getChildCount(node);  i++) {
			DndTreeNode child = (DndTreeNode) folderModel.getChild(node, i);
			if (child.getUserObject() == null  ||
					child.getUserObject() instanceof DndTreeNode) {
				addNodesToList(child, res);
			} else {
				res.add(child.getUserObject());
			}
		}
	}
	
	/**
	 * Build the pop-up menu for all user options
	 *    -- create folder ...
	 *    -- rename folder ...
	 *    -- delete folder ...
	 *    -- sort folder contents ...
	 *    -- create item ...
	 *    -- edit item ...
	 *    -- delete item ...
	 *
	 * Register "this" as the target of all menu selections
	 */
	private void buildOptionPopupMenu() {
		SqlToolkit.menuManager.unregisterOwner(this);
		
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_FOLDER_CREATE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_FOLDER_RENAME);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_FOLDER_DELETE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_FOLDER_SORT);
		SqlToolkit.menuManager.addSeparator(this, MenuManager.POPUP_MENU);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_ITEM_CREATE + itemName);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_ITEM_EDIT + itemName);
		SqlToolkit.menuManager.addMenu(this, MenuManager.POPUP_MENU, MENU_ITEM_DELETE + itemName);
		
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_CREATE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_RENAME);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_DELETE);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_SORT);
		SqlToolkit.menuManager.addSeparator(this, MenuManager.MAIN_MENU_EDIT);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_ITEM_CREATE + itemName);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_ITEM_EDIT + itemName);
		SqlToolkit.menuManager.addMenu(this, MenuManager.MAIN_MENU_EDIT, MENU_ITEM_DELETE + itemName);
		
		SqlToolkit.menuManager.setCurrentOwner(this);
	}

	/* (non-Javadoc)
	 * @see sqltool.common.MenuHandler#becomeActive()
	 */
	@Override
	public void becomeActive() {
		// Register this panel as the owner of the EDIT, VIEW and POPUP menus
		SqlToolkit.menuManager.setCurrentOwner(this);
	}

	/* (non-Javadoc)
	 * @see sqltool.common.MenuHandler#handleMenuEvent(java.awt.event.ActionEvent)
	 */
	@Override
	public void handleMenuEvent(ActionEvent ae) {
		// Dispatch the menu action
		if (ae.getSource() instanceof JMenuItem) {
			JMenuItem mi = (JMenuItem)ae.getSource();
			if (mi.getText().equalsIgnoreCase(MENU_FOLDER_CREATE)) {
				createFolder();
			} else if (mi.getText().equalsIgnoreCase(MENU_FOLDER_RENAME)) {
				renameFolder();
			} else if (mi.getText().equalsIgnoreCase(MENU_FOLDER_DELETE)) {
				deleteFolder();
			} else if (mi.getText().equalsIgnoreCase(MENU_FOLDER_SORT)) {
				sortFolder();
			} else if (mi.getText().equalsIgnoreCase(MENU_ITEM_CREATE + itemName)) {
				createItem();
			} else if (mi.getText().equalsIgnoreCase(MENU_ITEM_EDIT + itemName)) {
				editItem();
			} else if (mi.getText().equalsIgnoreCase(MENU_ITEM_DELETE + itemName)) {
				deleteItem();
			}
		}
	}
	
	/**
	 * Create a new folder, and insert it alphabetically in the correct order.
	 * The context comes from the selected folder
	 */
	private void createFolder() {
		// Prompt the user for a folder name
		String name = JOptionPane.showInputDialog(this, "Folder Name:",
				"New Folder", JOptionPane.QUESTION_MESSAGE);

		// Create a new folder and insert it into the proper place
		if (name != null) {
			DndTreeNode folder = new DndTreeNode(name, null);
			DndTreeNode parent = getSelectedFolder();
			int offset = 0;
			for (int i=0;  i<parent.getChildCount();  i++) {
				DndTreeNode aNode = (DndTreeNode)parent.getChildAt(i);
				if (aNode.getAllowsChildren()) {
					if (aNode.getLabel().compareToIgnoreCase(name) < 0) {
						offset = i+1;
					}
				}
			}
			folderModel.insertNodeInto(folder, parent, offset);
		}
	}
	
	/**
	 * Rename an existing folder, but do NOT sort the folders alphabetically.
	 * NOTE: the root folder can't be renamed
	 */
	private void renameFolder() {
		DndTreeNode parent = getSelectedFolder();
		if (parent == rootFolder) {
			JOptionPane.showMessageDialog(this,
					"Re-naming of the 'root'\nfolder is not allowed.",
					"Rename Folder", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String name = JOptionPane.showInputDialog(this, "New Name:",
					"Rename Folder", JOptionPane.QUESTION_MESSAGE);
			if (name != null) {
				parent.setLabel(name);
				folderTree.scrollPathToVisible(new TreePath(parent.getPath()));
			}
		}
	}
	
	/**
	 * Delete an existing folder.  Since this will affect the folder and all
	 * of it's contents, we'll confirm this action before doing anything.
	 */
	private void deleteFolder() {
		DndTreeNode folder = getSelectedFolder();
		if (folder != rootFolder) {
			
			String message = "Delete folder '" + folder.getLabel() + "'";
			if (folder.getChildCount() > 0) {
				message += " and contents";
			}
			message += "?";
			if (JOptionPane.showConfirmDialog(this, message, "Confirm Delete",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				
				// Save current path so we can default the selection to next node
				TreePath selectPath = folderTree.getSelectionPath().getParentPath();
				TreeNode parent     = folder.getParent();
				int ndx = folder.getParent().getIndex(folder);
				
				// Remove the folder
				folderModel.removeNodeFromParent(folder);
				
				// Make sure we don't try and select past the final folder, and if
				// delete the only node in a parent, set the parent as "active"
				ndx = Math.min(ndx, parent.getChildCount()-1);
				if (ndx >= 0) {
					TreeNode prev = parent.getChildAt(ndx);
					selectPath = selectPath.pathByAddingChild(prev);
				}
				folderTree.setSelectionPath(selectPath);
			}
		}
	}
	
	/**
	 * Sort the nodes of a folder, which could be sub-folders or items.
	 */
	private void sortFolder() {
		DndTreeNode parent = getSelectedFolder();
		if (JOptionPane.showConfirmDialog(this, "Sort folder '"
				+ parent.getLabel() + "' contents?", "Confirm Sort",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			
			// Remove all of the folder items (sub-folders and/or leaf nodes)
			List<DndTreeNode> tempList = new ArrayList<DndTreeNode>(folderModel.getChildCount(parent));
			while (folderModel.getChildCount(parent) > 0) {
				DndTreeNode child = (DndTreeNode) folderModel.getChild(parent, 0);
				folderModel.removeNodeFromParent(child);
				tempList.add(child);
			}
			
			// Sort the folder contents: the sub-folders should appear first, and
			// then the managed items
			Collections.sort(tempList, new Comparator<DndTreeNode>() {
				public int compare(DndTreeNode node1, DndTreeNode node2) {
					if (node1.getAllowsChildren() ^ node2.getAllowsChildren()) {
						return node1.getAllowsChildren() ? -1 : 1;
					} else {
						return node1.getLabel().compareToIgnoreCase(node2.getLabel());
					}
				}
			});
			
			// Return the items to the folder, sorted by their label
			for (int i = 0; i < tempList.size(); i++) {
				DndTreeNode child = tempList.get(i);
				folderModel.insertNodeInto(child, parent, parent.getChildCount());
			}
		}
	}
	
	/**
	 * Create a new folder item and insert it alphabetically into the correct
	 * sorted position among the other children
	 */
	protected void createItem() {
		// Create a new item and get an item editor, which is the "wizard"
		// that can create a fully-defined item
		FolderItem item = null;
		try {
			item = (FolderItem) Class.forName(folderItemClass).newInstance();
			itemEditor = item.getEditor();
		} catch (Exception ex) {
		}

		// If either the item or editor doesn't exists, bail on this operation
		if (item == null || itemEditor == null) {
			JOptionPane.showMessageDialog(this, "Unable to create item",
					"Create", JOptionPane.ERROR_MESSAGE);
		} else {
			// Edit the item and make sure the user chose "save"
			if (itemEditor.edit(item)) {
				DndTreeNode folder = new DndTreeNode(item.toString(), item);
				DndTreeNode parent = getSelectedFolder();

				// Skip over all sub-folders, and find the correct position of
				// the new item
				int offset = 0;
				for (int i=0;  i<parent.getChildCount();  i++) {
					DndTreeNode aNode = (DndTreeNode)parent.getChildAt(i);
					if (aNode.getAllowsChildren()) {
						offset = i+1;
					} else if (aNode.getLabel().compareToIgnoreCase(folder.getLabel()) < 0) {
						offset = i+1;
					}
				}

				// Insert the new node and scroll so that it's visible
				folderModel.insertNodeInto(folder, parent, offset);
				folderTree.scrollPathToVisible(new TreePath(folder.getPath()));
			}
		}
	}
	
	/**
	 * Edit an existing item.  This will NOT re-sort the list of items in the
	 * event that the item's name/label is changed.
	 */
	protected void editItem() {
		DndTreeNode node = getSelectedNode();
		if (node != null) {
			FolderItem item = (FolderItem) node.getUserObject();
			itemEditor = item.getEditor();

			// If either the item or editor doesn't exists, bail on this operation
			if (item == null || itemEditor == null) {
				JOptionPane.showMessageDialog(this, "Unable to edit item",
						"Edit", JOptionPane.ERROR_MESSAGE);
			} else {
				// Edit the item and make sure the user chose "save"
				if (itemEditor.edit(item)) {
					node.setUserObject(item);
					node.setLabel(item.toString());
					folderTree.scrollPathToVisible(new TreePath(node.getPath()));
				}
			}
		}
	}
	
	/**
	 * Delete an existing item from the folder.  This operation will be ignored
	 * if attempted on a sub-folder.
	 */
	protected void deleteItem() {
		DndTreeNode node = getSelectedNode();
		if (!node.getAllowsChildren()) {
			if (JOptionPane.showConfirmDialog(this, "Delete item '"
					+ node.getUserObject() + "'?", "Confirm Delete",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				
				// Save current path so we can default the selection to next node
				TreePath selectPath = folderTree.getSelectionPath().getParentPath();
				TreeNode parent     = node.getParent();
				int ndx = node.getParent().getIndex(node);
				
				// Remove the node
				folderModel.removeNodeFromParent(node);
				
				// Make sure we don't try and select past the final node, and if
				// we  delete the only node in a parent, set the parent as "active"
				ndx = Math.min(ndx, parent.getChildCount()-1);
				if (ndx >= 0) {
					TreeNode prev = parent.getChildAt(ndx);
					selectPath = selectPath.pathByAddingChild(prev);
				}
				folderTree.setSelectionPath(selectPath);
			}
		}
	}
	
	/**
	 * Display the pop-up menu for this panel at the location selected
	 * @param e mouse event
	 */
	void folderTree_mouseReleased(MouseEvent e) {
		SqlToolkit.menuManager.setCurrentOwner(this);
		JPopupMenu optionMenu = SqlToolkit.menuManager.getPopup();
		if (e.isPopupTrigger()  &&  optionMenu != null) {
			optionMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	/**
	 * If a mouse is pressed, check for a double-click to edit an item
	 * @param e mouse event
	 */
	void folderTree_mousePressed(MouseEvent e) {
		SqlToolkit.menuManager.setCurrentOwner(this);
		JPopupMenu optionMenu = SqlToolkit.menuManager.getPopup();
		if ( SqlToolkit.menuManager.isEnabled(this, MenuManager.POPUP_MENU, MENU_ITEM_EDIT + itemName)
				&&  e.getClickCount() == 2) {
			editItem();
		} else if (e.isPopupTrigger() && optionMenu != null) {
			optionMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	/**
	 * Add support for the "Delete" key on either a folder or item
	 * @param e key event
	 */
	void folderTree_keyReleased(KeyEvent e) {
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {
			DndTreeNode node = getSelectedNode();
			if (node.getAllowsChildren()) {
				deleteFolder();
			} else {
				deleteItem();
			}
		}
		
	}
	
	/**
	 * If the tree selection changes, enable/disable menu options as appropriate.
	 * NOTE: this affects both the main menu and the pop-up menu
	 */
	void folderTree_valueChanged(TreeSelectionEvent e) {
		setMenuEnableState();
	}
	
	
	// Enable or Disable menu options based on current selection
	protected void setMenuEnableState() {
		DndTreeNode node = getSelectedNode();
		
		SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_FOLDER_RENAME,
				(node != rootFolder  &&  node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_FOLDER_DELETE,
				(node != rootFolder  &&  node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_FOLDER_SORT,
				(node.getAllowsChildren()  &&  node.getChildCount() > 1));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_ITEM_EDIT + itemName,
				(! node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.POPUP_MENU, MENU_ITEM_DELETE + itemName,
				(! node.getAllowsChildren()));
		
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_RENAME,
				(node != rootFolder  &&  node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_DELETE,
				(node != rootFolder  &&  node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_FOLDER_SORT,
				(node.getAllowsChildren()  &&  node.getChildCount() > 1));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_ITEM_EDIT + itemName,
				(! node.getAllowsChildren()));
		SqlToolkit.menuManager.setEnabled(this, MenuManager.MAIN_MENU_EDIT, MENU_ITEM_DELETE + itemName,
				(! node.getAllowsChildren()));
	}
	
	/**
	 * Find the currently selected node, either a sub-folder or item
	 * @return selected node
	 */
	protected DndTreeNode getSelectedNode() {
		TreePath parentPath = folderTree.getSelectionPath();
		if (parentPath == null) {
			return rootFolder;
		} else {
			return (DndTreeNode) parentPath.getLastPathComponent();
		}
	}
	
	/**
	 * Find the currently selected folder.  If an item is selected, default to
	 * the root folder
	 * @return selected folder
	 */
	protected DndTreeNode getSelectedFolder() {
		TreePath parentPath = folderTree.getSelectionPath();
		if (parentPath != null) {
			for (int i=parentPath.getPathCount()-1;  i>0;  i--) {
				if (((DndTreeNode) parentPath.getPathComponent(i)).getAllowsChildren()) {
					return (DndTreeNode)parentPath.getPathComponent(i);
				}
			}
		}
		
		return rootFolder;
	}
}