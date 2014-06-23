package dragdrop.jtree;

import java.util.Map;
import javax.swing.ImageIcon;


/**
 * Interface to define a folder item API.  
 * 
 * @author wjohnson000
 * @version 1.0
 */
public interface FolderItem extends java.io.Serializable {

	/**
	 * Simple name that can be plopped into a menu to describe what type
	 * of item this is
	 * @return menu-appropriate name
	 */
	public String getMenuName();

	/**
	 * An instance of a "FolderItemEditor" that knows how to edit this type
	 * of item.
	 * @return folder-item editor
	 */
	public FolderItemEditor getEditor();

	/**
	 * Flag to indicate whether this type of item can be dragged into a
	 * text field
	 * @return TRUE if this has a nice String-based representation;  FALSE otherwise
	 */
	public boolean canDragString();

	/**
	 * Return the String that should be used when doing a drag/drop into a
	 * text field
	 * @return instances String contents
	 */
	public String  getDragString();

	/**
	 * Return application data stored on this item
	 * @return key-value pairs
	 */
	public Map<String,String> getValues();

	/**
	 * Set application data from a map of key-value pairs
	 * @param values key-value pairs
	 */
	public void setValues(Map<String,String> values);

	/**
	 * Return the icon that should be used when displaying this item in the UI.  If
	 * null, the use the system default icon.
	 * @return instance icon
	 */
	public ImageIcon getLeafIcon();
}