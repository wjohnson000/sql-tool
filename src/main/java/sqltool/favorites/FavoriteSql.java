package sqltool.favorites;

import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;

import dragdrop.jtree.*;


/**
 * Folder item that will store an SQL string and an alias by which the SQL
 * is known.
 * @author wjohnson000
 *
 */
public class FavoriteSql implements FolderItem {

	static final long serialVersionUID = 1695451375371448406L;
	
	// Manage an SQL string referenced by an alias
	String alias;
	String sql;
	
	/**
	 * Default constructor
	 */
	public FavoriteSql() {
		this ("", "");
	}

	/**
	 * Construct a folder item given the alias and sql string
	 * @param alias item alias
	 * @param sql sql string
	 */
	public FavoriteSql(String alias, String sql) {
		this.alias = alias;
		this.sql = sql;
	}
	
	public String getAlias() { return alias; }
	public String getSql()   { return sql; }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return alias;
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getMenuName()
	 */
	@Override
	public String getMenuName() {
		return "sql";
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getEditor()
	 */
	@Override
	public FolderItemEditor getEditor() {
		return new SqlEditor();
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#canDragString()
	 */
	@Override
	public boolean canDragString() {
		return true;
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getDragString()
	 */
	@Override
	public String getDragString() {
		return getSql();
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getValues()
	 */
	@Override
	public Map<String,String> getValues() {
		Map<String,String> myValues = new TreeMap<String,String>();
		myValues.put("alias", alias);
		myValues.put("sql", sql);
		return myValues;
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#setValues(java.util.Map)
	 */
	@Override
	public void setValues(Map<String,String> values) {
		alias = values.get("alias");
		sql   = values.get("sql");
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getLeafIcon()
	 */
	@Override
	public ImageIcon getLeafIcon() {
		return null;
	}
}