package sqltool.table;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import sqltool.common.SqlToolkit;
import sqltool.swing.extra.renderer.JJTableHeaderRenderer;


/**
 * Customer Header for the RowTable.  Custom tool-tips, sorting and pop-up
 * menus can be supported.
 * 
 * @author wjohnson000
 *
 */
public class RowTableHeader extends JTableHeader {

	private static final long serialVersionUID = 6079176526398287478L;

	/**
	 * Default constructor
	 */
	public RowTableHeader() {
		localSetup();
	}
	
	/**
	 * Constructor that takes pre-existing TableColumnModel
	 * @param tcm table column model
	 */
	public RowTableHeader(TableColumnModel tcm) {
		super(tcm);
		localSetup();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.JTableHeader#getToolTipText(java.awt.event.MouseEvent)
	 */
	@Override
	public String getToolTipText(MouseEvent me) {
		Point p = me.getPoint();
		int column = columnModel.getColumnIndexAtX(p.x);
		if (column >= 0) {
			return columnModel.getColumn(column).getHeaderValue().toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Listen for mouse events to catch requests to sort the column or
	 * display a pop-up menu
	 */
	private void localSetup() {
		setDefaultRenderer(new JJTableHeaderRenderer());
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				mouseClickedForSort(me);
			}
			public void mousePressed(MouseEvent me) {
				checkPopupMenu(me);
			}
			public void mouseReleased(MouseEvent me) {
				checkPopupMenu(me);
			}
		});
	}

	/**
	 * Sort the data after a single- or double-click, as specified in the user
	 * configuration data.
	 * 
	 * @param me mouse event
	 */
	private void mouseClickedForSort(MouseEvent me) {
		Point p = me.getPoint();
		int column = columnModel.getColumnIndexAtX(p.x);
		int realColumn = columnModel.getColumn(column).getModelIndex();
		if (me.getButton() == 1   &&   me.getClickCount() == SqlToolkit.userConfig.getClickCount()) {
			TableModel tm = getTable().getModel();
			if (tm != null  &&  tm instanceof RowTableModel) {
				((RowTableModel)tm).sortData(realColumn);
			}
		}
	}


	/**
	 * Display a pop-up menu.  NOTE: this is not yet fully implemented
	 * @param me mouse event
	 */
	@SuppressWarnings("unused")
	private void checkPopupMenu(MouseEvent me) {
		Point p = me.getPoint();
		int column = columnModel.getColumnIndexAtX(p.x);
		int realColumn = columnModel.getColumn(column).getModelIndex();
		if (me.isPopupTrigger()) {
			// Add code to display a pop-up menu
		}
	}
}
