package xmlutil;


/**
 * Simple tag-value pair, sort of like a key-value pair.
 * @author wjohnson000
 *
 */
class ParamOption {
	protected String tag;
	protected String value;

	protected ParamOption(String tag, String value) {
		this.tag   = tag;
		this.value = value;
	}
}


/**
 * Manage a list of options that could be part of a configuration file.
 * 
 * @author wjohnson000
 *
 */
public class ConfigParam {
	String        name;
	String        description = "Unknown";
	ParamOption[] optionList;
	
	/**
	 * Returns the name of this parameter
	 * @return parameter name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the description of this parameter
	 * @return parameter description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the number of [optional] options that are defined
	 * for this parameter
	 * @return count of options
	 */
	public int optionCount() {
		return (optionList == null) ? 0 : optionList.length;
	}
	
	/**
	 * Returns the option "tag" [display name] of the option at
	 * a given index
	 * @param ndx index of the option
	 * @return option tag, or "null" if invalid index specified
	 */
	public String getOptionTag(int ndx) {
		if (ndx >= 0  &&  ndx < optionCount()) {
			return optionList[ndx].tag;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the "value" of the option at a given index
	 * @param ndx index of the option
	 * @return option value, or "null" if invalid index specified
	 */
	public String getOptionValue(int ndx) {
		if (ndx >= 0  &&  ndx < optionCount()) {
			return optionList[ndx].value;
		} else {
			return null;
		}
	}
	
	/**
	 * Set the option name
	 * @param name new name of this parameter
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Set the option description
	 * @param description new description of this parameter
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Add a new option to this parameter
	 * @param tag the tag [display] name
	 * @param value the value for this parameter
	 */
	public void addOption(String tag, String value) {
		ParamOption[] newOptionList = new ParamOption[optionCount() + 1];
		for (int i=0;  i<newOptionList.length-1;  i++) {
			newOptionList[i] = optionList[i];
		}
		newOptionList[newOptionList.length-1] = new ParamOption(tag, value);
		optionList = newOptionList;
	}
	
	public String toString() {
		return name + " [" + getClass().getName() + "]";
	}
}
