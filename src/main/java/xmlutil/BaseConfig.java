package xmlutil;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;


/**
 * An abstract class that can read and write itself to the user's home
 * directory, and can manage values of STRING, INT, FONT or CONFIG types.
 * The latter defines a list of values.  Other types can be supported as
 * necessary.
 *  
 * @author wjohnson000
 *
 */
public abstract class BaseConfig extends Observable {
	private Map<String,ConfigParam> paramMap;
	
	/**
	 * This method must be defined in each sub-class, and must return
	 * the configuration file name (NAME ONLY; the default path must
	 * be System.property("user.home").
	 *
	 * @return configuration file name
	 */
	public abstract String getConfigFileName();
	
	
	/**
	 * Constructor sets up the containers for all of the configurable data
	 *
	 */
	protected BaseConfig() {
		paramMap = new HashMap<String,ConfigParam>();
		String userHome = System.getProperty("user.home", ".");
		String filename = userHome + "/" + getConfigFileName();
		ConfigParam[] cParam = SystemConfigFactory.readConfigFromXML(filename);
		
		for (int i=0;  i<cParam.length;  i++) {
			String name = cParam[i].getName();
			paramMap.put(name, cParam[i]);
		}
	}
	

	/**
	 * Save the parameter settings to the user's home directory
	 *
	 */
	public void saveConfig() {
		String userHome = System.getProperty("user.home", ".");
		String filename = userHome + "/" + getConfigFileName();
		
		ConfigParam[] cParam = (ConfigParam[])paramMap.values().toArray(new ConfigParam[0]);
		SystemConfigFactory.saveConfigAsXML(filename, cParam);
	}
	
	
	/**
	 * Get the font associated with a given alias
	 * 
	 * @param alias the alias [name] used to identify this font to the application
	 * @return font the associated font
	 */
	public Font getFont(String alias) {
		return getFontValue(alias);
	}

	/**
	 * Register a font, based on a name and font definition
	 * 
	 * @param alias the alias [name] used to identify this font to the application
	 * @param font the associated font
	 */
	public void setFont(String alias, Font font) {
		setFontValue(alias, font);
		notifyAll(alias);
	}

	/**
	 * Notify all "observers" that something has changed
	 */
	protected void notifyAll(String alias) {
		setChanged();
		notifyObservers(alias);
	}

	/**
	 * Return an "int" value given the parameter name (key)
	 * @param key alias
	 * @return int value for that parameter
	 */
	protected int getIntValue(String key) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof IntParam) {
			return ((IntParam)cParam).getValue();
		} else {
			return 0;
		}
	}
	
	/**
	 * Return a "String" value given the parameter name (key)
	 * @param key alias
	 * @return String value for that parameter
	 */
	protected String getStringValue(String key) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof StringParam) {
			return ((StringParam)cParam).getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Return a "Font" value given the parameter name (key)
	 * @param key alias
	 * @return Font value for that parameter
	 */
	protected Font getFontValue(String key) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof FontParam) {
			FontParam fParam = (FontParam)cParam;
			return new Font(fParam.getFamily(), Font.PLAIN, fParam.getSize());
		} else {
			return null;
		}
	}
	
	/**
	 * Set the "int" value for a given the parameter name (key)
	 * @param key alias
	 * @param val int value for that parameter
	 */
	protected void setIntValue(String key, int val) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof IntParam) {
			IntParam iParam = (IntParam)cParam;
			iParam.setValue(val);
		} else {
			IntParam iParam = new IntParam();
			iParam.setName(key);
			iParam.setValue(val);
			paramMap.put(key, iParam);
		}
	}
	
	/**
	 * Set the "String" value for a given the parameter name (key)
	 * @param key alias
	 * @param val String value for that parameter
	 */
	protected void setStringValue(String key, String val) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof StringParam) {
			StringParam sParam = (StringParam)cParam;
			sParam.setValue(val);
		} else {
			StringParam sParam = new StringParam();
			sParam.setName(key);
			sParam.setValue(val);
			paramMap.put(key, sParam);
		}
	}
	
	/**
	 * Set the "Font" value for a given the parameter name (key)
	 * @param key alias
	 * @param val Font value for that parameter
	 */
	protected void setFontValue(String key, Font font) {
		ConfigParam cParam = paramMap.get(key);
		if (cParam != null  &&  cParam instanceof FontParam) {
			FontParam fParam = (FontParam)cParam;
			fParam.setFamily(font.getFamily());
			fParam.setSize(font.getSize());
		} else {
			FontParam fParam = new FontParam();
			fParam.setName(key);
			fParam.setFamily(font.getFamily());
			fParam.setSize(font.getSize());
			paramMap.put(key, fParam);
		}
	}
}
