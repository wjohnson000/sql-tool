package sqltool.query;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.Color;


/**
 * Create a new "LightGray" style, and make sure that all text in this
 * document is that color.
 * @author wjohnson000
 *
 */
public class LineNumberDocument extends DefaultStyledDocument {
	
	static final long serialVersionUID = -1324389143939920278L;

	/**
	 * Constructor sets up the default styles to use in "coloring" the SQL
	 *  -- LIGHT-GRAY for everything
	 */
	public LineNumberDocument() {
		Style onlyStyle = addStyle("LightGray", null);
		StyleConstants.setForeground(onlyStyle, new Color(92, 92, 92));  //Color.lightGray);
	}
	
	/**
	 * Method that handles insertion of new text: call the parent method
	 * to handle the actual insertion, but then make the colors pretty
	 * 
	 * @param offs offset into current text where new text is added
	 * @param str new text to insert
	 * @param as Attributes associated with the new text
	 * 
	 */
	public void insertString(int offs, String str, AttributeSet as)
			throws BadLocationException {
		super.insertString(offs, str, as);
		setPrettyColors();
	}
	
	/**
	 * Method that handles deleting of old text: call the parent method
	 * to handle the actual deletion, but then make the colors pretty
	 * 
	 * @param offs offset into current text where new text is added
	 * @param str new text to insert
	 * 
	 */
	public void remove(int offs, int len)
			throws BadLocationException {
		super.remove(offs, len);
		setPrettyColors();
	}
	

	/**
	 * Add color to the text:
	 *   -- LIGHT-GRAY for everything
	 */
	private void setPrettyColors() {
		this.setCharacterAttributes(0, getLength(), getStyle("LightGray"), true);
	}
}
