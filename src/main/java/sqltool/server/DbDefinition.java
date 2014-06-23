package sqltool.server;

import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;

import dragdrop.jtree.*;


/**
 * Implements {@link FolderItem} for a database definition, from which a
 * connection can be generated.
 * 
 * @author wjohnson000
 *
 */
public class DbDefinition implements FolderItem {

    private static final long serialVersionUID = 5184024567973101156L;
//	static final long serialVersionUID = 2636551356049900124L;
	
	// Manage the definition for a JDBC database connection
	private String  alias;
	private String  driver;
	private String  url;
	private String  user;
	private String  password;
	private String  testQuery;
	private boolean isActive;

	/**
	 * Default constructor creates a blank definition
	 */
	public DbDefinition() {
		this("", "", "", "", "", "");
	}

	/**
	 * Full constructor
	 * @param alias alias for this item
	 * @param driver database driver class
	 * @param url database URL
	 * @param user username
	 * @param password password
	 */
	public DbDefinition(
	        String alias, String driver, String url, String user, String password, String testQuery) {
		this.alias = alias;
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		this.testQuery = testQuery;
		this.isActive = true;
	}
	
	public String getAlias()      { return alias; }
	public String getDriver()     { return driver; }
	public String getURL()        { return url; }
	public String getUser()       { return user; }
	public String getPassword()   { return password; }
	public String getTestQuery()  { return testQuery; }
	public boolean isActive()     { return isActive; }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()      { return alias; }
	
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getMenuName()
	 */
	@Override
	public String getMenuName() {
		return "server";
	}

	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItem#getEditor()
	 */
	@Override
	public FolderItemEditor getEditor() {
		return new DbDefinitionEditor();
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
	

	/**
	 * Method required to pull out all values needed to re-created this object
	 * from an XML file.  We need to save five values.
	 * 
	 * @return Map containing keys and value for the DB server definition
	 */
	@Override
	public Map<String,String> getValues() {
		Map<String,String> myValues = new TreeMap<String,String>();

		myValues.put("alias", alias);
		myValues.put("driver", driver);
		myValues.put("url", url);
		myValues.put("user", user);
		myValues.put("password", password);
		myValues.put("testQuery", testQuery);
		myValues.put("isActive", String.valueOf(isActive));

		return myValues;
	}
	

	/**
	 * Companion method to "getValues()", this method takes a Map of key/value
	 * pairs and re-sets all instance values.
	 * 
	 * @param Map containing keys and value for the DB server definition
	 */
	@Override
	public void setValues(Map<String,String> values) {
		alias     = values.get("alias");
		driver    = values.get("driver");
		url       = values.get("url");
		user      = values.get("user");
		password  = values.get("password");
		testQuery = values.get("testQuery");
		isActive  = "true".equals(values.get("isActive"));
	}

	/**
	 * This method generates the custom image for leaf items in the tree
	 */
	@Override
	public ImageIcon getLeafIcon() {
		return new ImageIcon(DbDefinition.class.getResource("serverdef.gif"));
	}
}