package xmlutil;

/**
 * A font parameter that can be part of a configuration file
 * @author wjohnson000
 *
 */
public class FontParam extends ConfigParam {
	private String family;
	private int    size;

	/**
	 * Retrieve the font family
	 * @return font family
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Set the font family
	 * @param family font family
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * Retrieve the font size
	 * @return font size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Set the font size
	 * @param size font size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Set the font size from a String value
	 * @param value font size
	 */
	public void setSize(String value) {
		try {
			this.size = Integer.parseInt(value);
		} catch (Exception ex) {
			this.size = 0;
		}
	}

	/* (non-Javadoc)
	 * @see xmlutil.ConfigParam#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " -- font: " + family + " . " + size;
	}
}
