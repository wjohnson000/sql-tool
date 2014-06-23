package dragdrop.jtree;

import java.awt.Container;


/**
 * Interface for all folder-item editors
 * @author wjohnson000
 *
 */
public interface FolderItemEditor {

	/**
	 * Edit a folder-item
	 * @param item item to edit
	 * @return TRUE if the item changed; FALSE otherwise
	 */
	public boolean edit(FolderItem item);

	/**
	 * Edit a folder-item, and tie the panel to a parent container
	 * @param parent parent UI object
	 * @param item item to edit
	 * @return TRUE if the item changed; FALSE otherwise
	 */
	public boolean edit(Container parent, FolderItem item);
}