package dragdrop.jtree;

import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;


/**
 * Default implementation of the "FolderItem" interface, which simply
 * defines an object that can be stored in a folder hierarchy.
 * 
 * @author wjohnson000
 * @version 1.0
 */
public class DefaultFolderItem implements FolderItem {

	private static final long serialVersionUID = -8260894569141374210L;

	private static final String MENU_NAME = "item";

	/**
	 * Default constructor
	 */
	public DefaultFolderItem() { }

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getMenuName()
	 */
	@Override
	public String getMenuName() {
		return MENU_NAME;
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getEditor()
	 */
	@Override
	public FolderItemEditor getEditor() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#canDragString()
	 */
	@Override
	public boolean canDragString() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getDragString()
	 */
	@Override
	public String getDragString() {
		return "";
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getValues()
	 */
	@Override
	public Map<String,String> getValues() {
		return new HashMap<String,String>();
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#setValues(java.util.Map)
	 */
	@Override
	public void setValues(Map<String,String> values) {	
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getLeafIcon()
	 */
	@Override
	public ImageIcon getLeafIcon() {
		return null;
	}
}