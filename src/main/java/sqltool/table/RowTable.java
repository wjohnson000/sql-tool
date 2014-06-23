package sqltool.table;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;

import sqltool.swing.extra.renderer.JJTableHeaderRenderer;


/**
 * Custom "JTable" that includes a separate viewport listing the row
 * number.
 * 
 * @author wjohnson000
 *
 */
public class RowTable extends JTable {

	private static final long serialVersionUID = 2751844893352877620L;

	private JTable rowNumberTable = null;
	private int    rowNumberCount = 0;
	
//	=============================================================================
//	A couple of the fun constructors inherited from JTable
//	=============================================================================
	public RowTable() {
		super();
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(true);
		setTableHeader(new RowTableHeader(this.getTableHeader().getColumnModel()));
	}
	
	public RowTable(TableModel tm) {
		super(tm);
		setupRowTable();
	}
	
	/**
	 * When a new model is set, make sure we also link it to our ROW table
	 * @param model new table model
	 */
	public void setModel(TableModel model) {
		super.setModel(model);
		if (rowNumberTable != null) {
			rowNumberCount = model.getRowCount();
		} else {
			setupRowTable();
		}
	}
	
	@Override
	public String getToolTipText(MouseEvent me) {
		StringBuilder sbuf = new StringBuilder();
		int iRow = rowAtPoint(me.getPoint());
		int iCol = columnAtPoint(me.getPoint());
		int jCol = convertColumnIndexToModel(iCol);
		Object what = getValueAt(iRow, jCol);
		if (what != null  &&  what instanceof Clob) {
			String line = "";
			try {
				Reader reader = ((Clob)what).getCharacterStream();
				BufferedReader br = new BufferedReader(reader);
				while ((line=br.readLine()) != null  &&  sbuf.length() < 255) {
					sbuf.append(line);
				}
				br.close();
			} catch(Exception ex) {
				sbuf.append(" ... " + ex.getMessage());
			}
		}
		return sbuf.toString();
	}

	/**
	 * Lock the first column so it doesn't scroll horizontally
	 */
	public void setupRowTable() {
		// Find the "JScrollPane" for this table
		Container parent = getParent();
		while (! (parent == null  ||  parent instanceof JScrollPane)) {
			parent = parent.getParent();
		}
		
		if (parent != null) {
			final JScrollPane pane = (JScrollPane)parent;
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			getTableHeader().setUpdateTableInRealTime(false);
			
			// All we care about is a simple model that will give us a row count
			rowNumberTable = new JTable(new AbstractTableModel() {
				private static final long serialVersionUID = -4718682979643977364L;
				public int getRowCount() { return rowNumberCount; }
				public int getColumnCount() { return 1; }
				public String getColumnName(int col) { return " "; }
				public Class<?> getColumnClass(int col) { return Integer.class; }
				public Object getValueAt(int rowN, int colN) { return new Integer(rowN + 1); }
			});
			
			rowNumberTable.setRowSelectionAllowed(this.getRowSelectionAllowed());
			rowNumberTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			rowNumberTable.setAutoCreateColumnsFromModel(false);
			rowNumberTable.getTableHeader().setUpdateTableInRealTime(false);
			rowNumberTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
				private static final long serialVersionUID = -47060840682619295L;

				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {
					super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					setHorizontalAlignment(RIGHT);
					setBackground(pane.getBackground());
					setForeground(Color.black);
					return this;
				}

			});
			
			TableColumnModel tcm = rowNumberTable.getColumnModel();
			TableColumn tc = tcm.getColumn(0);
			tc.setPreferredWidth(60);
			tc.setMinWidth(60);
			tc.setMaxWidth(60);
			rowNumberTable.setColumnModel(tcm);
			
			rowNumberTable.setPreferredScrollableViewportSize(rowNumberTable.getPreferredSize());
			pane.setRowHeaderView(rowNumberTable);
			pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowNumberTable.getTableHeader());
		}
	}

	/**
	 * If the table has changed, notify the extra "ROW" table
	 */
	public void tableChanged(TableModelEvent e) {
		// Let the parent class get a crack at this, but ignore any error because
		// there might be timing issues.
		try {
			super.tableChanged(e);
		} catch (Exception ex) { }

		rowNumberCount = getModel().getRowCount();
		if (e.getType() == TableModelEvent.INSERT  ||  e.getType() == TableModelEvent.DELETE) {
			((AbstractTableModel)rowNumberTable.getModel()).fireTableDataChanged();
		}
	}

	/**
	 * If we are rendering a secondary header (column headers for additional queries),
	 * then use a table header renderer, explicitly setting the background color to
	 * light gray.  Otherwise use the renderer appropriate for the current data type
	 */
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (getModel() != null  &&  ((RowTableModel)getModel()).isSecondaryHeader(row)  &&
				getModel().getValueAt(row, col) != null) {
			return new JJTableHeaderRenderer(java.awt.Color.lightGray);
		} else {
			return super.getCellRenderer(row, col);
		}
	}
}
