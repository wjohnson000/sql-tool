package xmlutil;

/**
 * An integer parameter that can be part of a configuration file
 * @author wjohnson000
 *
 */
public class IntParam extends ConfigParam {
	private int value;

	/**
	 * Retrieve the integer value
	 * @return value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Set the integer value
	 * @param value new value
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Set the integer value from a String
	 * @param value new value
	 */
	public void setValue(String value) {
		try {
			this.value = Integer.parseInt(value);
		} catch (Exception ex) {
			this.value = 0;
		}
	}

	/* (non-Javadoc)
	 * @see xmlutil.ConfigParam#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " -- value: " + value;
	}
	
}
