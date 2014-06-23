package sqltool.table;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.table.*;


/**
 * Extend the "AbstractTableModel" to support a model that can be sorted,
 * etc.
 * 
 * @author wjohnson000
 *
 */
public class RowTableModel extends AbstractTableModel {

	static final long serialVersionUID = 5841462711193959570L;

	// =============================================================================
	//  I N S T A N C E    V A R I A B L E S
	//   -- sortColumn: Column by which data is sorted (default = 0 [ROW #])
	//   -- isAscend: TRUE if data is sorted ascending, FALSE otherwise
	//   -- colWidth: Length of the longest entry in that column
	//   -- colName: Array of column names, retrieved from the ResultSetMetaData
	//   -- colEmpty: Indicates if there is data in any row for that column
	//   -- colType: Array of Class types, based on the java.sql.Types of the
	//               column data, retrieved from the ResultSetMetaData
	//   -- rowData: Table data, an ArrayList of Object[] arrays
	// =============================================================================
	protected int     sortColumn = -1;
	protected boolean isAscend = false;
	protected boolean canSort  = false;
	
	protected int[]     colWidth = null;
	protected String[]  colName = null;
	protected boolean[] colEmpty = null;
	protected Class<?>[]   colType = null;
	protected ArrayList<Object> rowData = null;

	/**
	 * Constructor don't do nothing other than set the initial state ...
	 */
	public RowTableModel() {
		this(null, null);
	}

	/**
	 * Create a new model with the specified column names and column types
	 * @param colName
	 * @param colType
	 */
	public RowTableModel(String[] colName, Class<?>[] colType) {
		setColumnData(colName, colType);
		resetTableData();
	}
	
	/**
	 * Set the column names and column object types
	 * @param colName column names
	 * @param colType column object [class] types
	 */
	public void setColumnData(String[] colName, Class<?>[] colType) {
		this.colName = colName;
		this.colType = colType;
		resetTableData();
		fireTableStructureChanged();
	}
	
	/**
	 * Reset the data, keeping the column structure the same, including the
	 * column widths
	 */
	public void resetTableData() {
		sortColumn = -1;
		isAscend = true;
		rowData = new ArrayList<Object>(10);
		colEmpty = null;
		colWidth = null;
		if (colName != null) {
			colWidth = new int[colName.length];
			colEmpty = new boolean[colName.length];
			for (int i = 0; i < colWidth.length; i++) {
				colEmpty[i] = true;
				colWidth[i] = colName[i].length();
			}
		}
	}
	
	/**
	 * Add a row of data.  This consists of:
	 *   -- If this is the first row, set up the model
	 *   -- Add the data
	 *   -- Check to see if any of the previous empty columns now have data
	 *   -- Update the display to show the new row
	 * 
	 * @param data new row passed back from the database query
	 * @param isData TRUE if this a really data, and FALSE if this is a secondary header
	 */
	public void addRow(Object[] data, boolean isData) {
		if (rowData == null) {
			resetTableData();
		}
		
		rowData.add(data);
		int limit = Math.min(data.length, (colWidth == null ? 0 : colWidth.length));
		for (int i=0;  i<limit;  i++) {
			colWidth[i] = Math.max(colWidth[i], (data[i] == null ? 0 : data[i].toString().length()));
			if (colEmpty[i]  &&  isData) {
				colEmpty[i] = (data[i] == null)  ||  (data[i].toString().trim().length() == 0);
			}
		}
		int rows = rowData.size();
		fireTableRowsInserted(rows - 1, rows - 1);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		if (colName == null) {
			return 0;
		} else {
			return colName.length;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int col) {
		if (colName == null  ||  col >= colName.length) {
			return " ";
		} else {
			return colName[col];
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int col) {
		if (colType == null  ||  col >= colType.length) {
			return Integer.class;
		} else {
			return colType[col];
		}
	}
	
	/**
	 * Return the width of a column
	 * @param col column number
	 * @return with of the column
	 */
	public int getCharacterWidth(int col) {
		if (colWidth == null  ||  col < 0  ||  col >= colWidth.length) {
			return 5;
		} else {
			return colWidth[col];
		}
	}
	
	/**
	 * Return flag indicating if the column is empty
	 * @param col column number
	 * @return TRUE if the the column contains no useful data; FALSE otherwise
	 */
	public boolean isColumnEmpty(int col) {
		if (colEmpty == null  ||  col >= colEmpty.length) {
			return true;
		} else {
			return colEmpty[col];
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (rowData == null) {
			return 0;
		} else {
			return rowData.size();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowN, int colN) {
		if (rowData != null  &&  rowN < rowData.size()) {
			if (colName == null) {
				return new Integer(rowN + 1);
			} else {
				Object[] row = (Object[]) rowData.get(rowN);
				return (colN < row.length) ? row[colN] : null;
			}
		}
		return "";
	}
	
	/**
	 * Return a flag indicating if the given row is a secondary header, which will
	 * always be FALSE for this class.
	 * @param row row number
	 * @return FALSE
	 */
	public boolean isSecondaryHeader(int row) {
		return false;
	}
	
	/**
	 * Return the column number which is the currently sorted one
	 * @return column number by which data is sorted
	 */
	public int getSortColumn() {
		return sortColumn;
	}

	/**
	 * Return a flag as to whether the data is sorted ascending or not
	 * @return TRUE if the data is sorted ascending; FALSE otherwise
	 */
	public boolean isSortAscending() {
		return isAscend;
	}

	/**
	 * Sort the data by the given column number.  Note: if the column is the
	 * same as the current sort column, we re-sort it after reversing the 
	 * ascending/descending criteria.
	 * 
	 * @param column column number
	 */
	public void sortData(int column) {
		if (canSort) {
			if (column == sortColumn) {
				isAscend = !isAscend;
			} else {
				isAscend = true;
				sortColumn = column;
			}
			sortData();
			fireTableDataChanged();
		}
	}
	
	// Sort the data for real; we need to handle data of the following type:
	//   integer, long, double, timestamp, big-decimal or string
	private void sortData() {
		Collections.sort(rowData, new Comparator<Object>() {
			public int compare(Object obj1, Object obj2) {
				Object[] row1 = (Object[]) obj1;
				Object[] row2 = (Object[]) obj2;
				
				Class<?> type = colType[sortColumn];
				Object val1 = row1[sortColumn];
				Object val2 = row2[sortColumn];
				
				// Make nulls less than anything
				if (val1 == null  &&  val2 == null) {
					return 0;
				} else if (val1 == null) {
					return (isAscend) ? -1 : 1;
				} else if (val2 == null) {
					return (isAscend) ? 1 : -1;
				}
				
				// Do the appropriate comparison, based on the column object
				// type
				if (type == Integer.class) {
					Integer int1 = (Integer) val1;
					Integer int2 = (Integer) val2;
					return (isAscend) ? int1.compareTo(int2) : int2.compareTo(int1);
				} else if (type == Long.class) {
					Long long1 = (Long) val1;
					Long long2 = (Long) val2;
					return (isAscend) ? long1.compareTo(long2) : long2.compareTo(long1);
				} else if (type == Double.class) {
					Double dbl1 = (Double) val1;
					Double dbl2 = (Double) val2;
					return (isAscend) ? dbl1.compareTo(dbl2) : dbl2.compareTo(dbl1);
				} else if (type == Timestamp.class) {
					Timestamp tmp1 = (Timestamp) val1;
					Timestamp tmp2 = (Timestamp) val2;
					return (isAscend) ? tmp1.compareTo(tmp2) : tmp2.compareTo(tmp1);
				} else if (type == BigDecimal.class) {
					BigDecimal tmp1 = (BigDecimal) val1;
					BigDecimal tmp2 = (BigDecimal) val2;
					return (isAscend) ? tmp1.compareTo(tmp2) : tmp2.compareTo(tmp1);
				} else if (type == Boolean.class) {
					Boolean tmp1 = (Boolean) val1;
					Boolean tmp2 = (Boolean) val2;
					int comp = (tmp1.booleanValue() == tmp2.booleanValue()) ? 0 : (tmp1.booleanValue() ? 1 : -1);
					return (isAscend) ? comp : -1 * comp;
				} else if (type == String.class) {
					String str1 = (val1 == null) ? "" : val1.toString();
					String str2 = (val2 == null) ? "" : val2.toString();
					return (isAscend) ? str1.compareToIgnoreCase(str2) :
						str2.compareToIgnoreCase(str1);
				} else {
					String str1 = (val1 == null) ? "" : val1.toString();
					String str2 = (val2 == null) ? "" : val2.toString();
					return (isAscend) ? str1.compareToIgnoreCase(str2) :
						str2.compareToIgnoreCase(str1);
				}
			}
		});
	}
}