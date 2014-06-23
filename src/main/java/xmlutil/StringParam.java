package xmlutil;

/**
 * A String parameter that can be part of a configuration file
 * @author wjohnson000
 *
 */
public class StringParam extends ConfigParam{
	private String value;

	/**
	 * Retrieve the string value
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the string value
	 * @param value new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see xmlutil.ConfigParam#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " -- value: " + value;
	}
}
