package sqltool.common.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import sqltool.server.DbDefinition;


/**
 * Manage a list of database definitions; these can be made available to
 * the user so they can select an active database connect
 * 
 * @author wjohnson000
 */
public class DbDefinitionManager extends Observable {
//	============================================================================
//	tag that is sent out to observers ...
//	============================================================================
	public static final String OBSERVABLE_TAG = "observable.db-definition-list";
	
	
//	============================================================================
//	Instance variables
//	============================================================================
	List<DbDefinition> dbDefList = new ArrayList<DbDefinition>();
	
	
	/**
	 * Set the list of database definitions
	 * 
	 * @param dbDefList List of "DbDefinition" instances
	 */
	public void setDbDefList(List<DbDefinition> dbDefList) {
		this.dbDefList = dbDefList;
		broadcastChange();
	}
	
	
	/**
	 * Return a list of all DbDefinitions ... all ACTIVE definitions
	 * 
	 * @return List of "DbDefinition" instances
	 */
	public List<DbDefinition> getDbDefList() {
	    List<DbDefinition> activeDbList = new ArrayList<DbDefinition>();
	    for (DbDefinition dbDef : dbDefList) {
	        if (dbDef.isActive()) {
	            activeDbList.add(dbDef);
	        }
	    }
		return activeDbList;
	}
	
	
	/**
	 * Return a list of all JDBC driver names currently being referenced by any
	 * of the definitions.
	 * 
	 * @return list of JDBC driver class names
	 */
	public List<String> getDriverNames() {
		List<String> res = new ArrayList<String>(3);
		for (int i=0;  dbDefList!=null & i<dbDefList.size();  i++) {
			DbDefinition dbDef = dbDefList.get(i);
			if (! res.contains(dbDef.getDriver())) {
				res.add(dbDef.getDriver());
			}
		}
		Collections.sort(res);
		return res;
	}
	
	
	/**
	 * Return a list of all JDBC driver names currently being referenced by any
	 * of the definitions.
	 * 
	 * @return list of JDBC driver class names
	 */
	public List<String> getURLs(String driverName) {
		List<String> res = new ArrayList<String>(3);
		for (int i=0;  dbDefList!=null & i<dbDefList.size();  i++) {
			DbDefinition dbDef = dbDefList.get(i);
			if (dbDef.getDriver().equals(driverName)  &&  ! res.contains(dbDef.getURL())) {
				res.add(dbDef.getURL());
			}
		}
		Collections.sort(res);
		return res;
	}
	
	
	/**
	 * This method notifies all interested observers that the list of database
	 * definitions has changed
	 * 
	 * @param 
	 */
	private void broadcastChange() {
		setChanged();
		notifyObservers(OBSERVABLE_TAG);
	}
}
