package sqltool.swing.extra.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;

import sqltool.common.SqlToolkit;
import sqltool.table.RowTableModel;

/**
 * New "Renderer" for a table header, which will allow for indicating which
 * columns have no values, the current sort column, and "up" and "down"
 * arrows for indicating sort direction.
 * 
 * @author wjohnson000
 *
 */
public class JJTableHeaderRenderer extends DefaultTableCellRenderer {

	static final long serialVersionUID = 7891542925941836278L;
	private ImageIcon sortAsc  = null;
	private ImageIcon sortDesc = null;
	
	private Border regBorder   = null;  // Border for unsorted (regular) columns
	private Border sortBorder  = null;  // Border for the sorted column
	private Font   boldFont    = null;  // Bold font for sorted column
	private Font   emptyFont   = null;  // Font for empty columns
	private Color  bkgColor    = null;  // Optional background color
	

	/**
	 * Set up two properties at creation time:
	 *   -- Horizontal alignment, which is "CENTER" on title
	 *   -- Opacity, which is "false", so the background color is inherited
	 * Create the borders, and load the two "sorted-direction" icons
	 */

	public JJTableHeaderRenderer() {
		super();
		SqlToolkit.appLogger.logDebug("JJTableHaderRenderer.construct ... " + this);

		setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		setOpaque(false);
		
		regBorder   = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		sortBorder  = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

		sortAsc  = new ImageIcon(JJTableHeaderRenderer.class.getResource("sortAsc.gif"));
		sortDesc = new ImageIcon(JJTableHeaderRenderer.class.getResource("sortDesc.gif"));
		setIconTextGap(8);
	}

	/**
	 * Create a header with a custom background color
	 */
	public JJTableHeaderRenderer(Color bkgColor) {
		this();
		setOpaque(true);
		this.bkgColor = bkgColor;
	}
	
	/**
	 * Return the component used to render this header:
	 *   -- Raised "bevel" border for all columns except for the sorted one
	 *   -- Lowered "bevel" border for the sorted column
	 *   -- "bold" font for the sorted column
	 *   -- "italic" font for columns w/out any data
	 */
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		int realColumn = table.getColumnModel().getColumn(column).getModelIndex();
		setIcon(null);
		setBorder(regBorder);
		if (table.getModel() instanceof RowTableModel) {
			if (realColumn == ((RowTableModel)table.getModel()).getSortColumn()) {
				setBorder(sortBorder);
				if (boldFont == null) {
					boldFont = getFont().deriveFont(Font.BOLD);
				}
				setFont(boldFont);
				if (((RowTableModel)table.getModel()).isSortAscending()) {
					setIcon(sortAsc);
				} else {
					setIcon(sortDesc);
				}
			} else if (((RowTableModel)table.getModel()).isColumnEmpty(realColumn)) {
				setText((value == null || value.toString().length() == 0) ? "  " : "[" + value.toString() + "]");
				if (emptyFont == null) {
					emptyFont = getFont().deriveFont(Font.ITALIC);
				}
				setFont(emptyFont);
			}
		}
		
		if (bkgColor != null) {
			this.setBackground(bkgColor);
		}
		
		return this;
	}
}
