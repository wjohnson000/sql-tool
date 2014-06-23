package sqltool.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 * In some panes a "line number" will be displayed.  This is the default border
 * for the panel
 * 
 * @author wjohnson000
 *
 */
public class LineNumberBorder extends AbstractBorder {

	static final long serialVersionUID = -3492425821348182223L;

	/** Use a light-gray background */
	private static Color LINE_COLOR = new Color(164, 164, 164);

	/**
	 * Define a default constructor
	 */
	public LineNumberBorder() { }

	/* (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
	 */
	@Override
	public Insets getBorderInsets(Component comp) {
		return new Insets(0, 0, 0, 0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
	 */
	@Override
	public Insets getBorderInsets(Component comp, Insets insets) {
		insets.left   = 0;
		insets.top    = 0;
		insets.bottom = 0;
		insets.right  = 0;
		return insets;
	}

	/* (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
	 */
	@Override
	public void paintBorder(Component comp, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		g.setColor(LINE_COLOR);
		g.drawLine(x+width-1, y, x+width-1, y+height);
		g.setColor(oldColor);
	}

	/**
	 * The line color, light gray
	 * @return line color, always light gray
	 */
	public Color getLineColor() {
		return LINE_COLOR;
	}

	/**
	 * No thick border line
	 * @return border thickness, always zero (0)
	 */
	public int getThickness() {
		return 0;
	}

	/**
	 * No rounded corners
	 * @return rounded corners, always FALSE
	 */
	public boolean getRoundedCorners() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#isBorderOpaque()
	 */
	@Override
	public boolean isBorderOpaque() {
		return false;
	}
}
