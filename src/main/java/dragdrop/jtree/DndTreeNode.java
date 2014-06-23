package dragdrop.jtree;

import java.io.IOException;
import java.awt.datatransfer.*;
import javax.swing.tree.*;


/**
 * The object maanged by this "DndTreeNode" can either be another "DndTreeNode"
 * or a "FolderItem".  If the former, then is is a parent folder or sub-folder.
 */
public class DndTreeNode extends DefaultMutableTreeNode
		implements Transferable {
	
	static final long serialVersionUID = -5171173505172041684L;
	
	// For DnD, we can allow other folders or specific items to be dropped here
	public final static DataFlavor FOLDER_FLAVOR =
		new DataFlavor(DndTreeNode.class, "Folder");
	public final static DataFlavor STRING_FLAVOR = DataFlavor.stringFlavor;
	private static final DataFlavor[] Flavors = { FOLDER_FLAVOR, STRING_FLAVOR };
	
	String label = null;

	/**
	 * Default constructor
	 * @param label display label for this node
	 * @param userObject object being "managed" by this node
	 */
	public DndTreeNode(String label, Object userObject) {
		super(userObject);
		setLabel(label);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		// This is a LEAF node if we are managing a "FolderItem"; otherwise it
		// is not a leaf and we can allow children
		return getUserObject() instanceof FolderItem;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultMutableTreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren() {
		return ! isLeaf();
	}
	
	
//	=============================================================================
//	Methods necessary to be a "Transferable" thing
//	=============================================================================

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor df) {
		if (df.equals(FOLDER_FLAVOR)) {
			return true;
		} else if (df.equals(STRING_FLAVOR)) {
			return isLeaf()  &&  ((FolderItem)getUserObject()).canDragString();
		} else {
			return false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return Flavors;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public Object getTransferData(DataFlavor df)
	throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(df)) {
			if (df.equals(FOLDER_FLAVOR)) {
				return this;
			} else {
				return ((FolderItem)getUserObject()).getDragString();
			}
		} else {
			throw new UnsupportedFlavorException(df);
		}
	}
}
