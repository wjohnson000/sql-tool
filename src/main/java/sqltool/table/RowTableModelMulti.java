package sqltool.table;

import java.util.HashSet;
import java.util.Set;


/**
 * Extend the {@link RowTableModel} for a multi-query result.  The headers for
 * subsequent queries will be in the table, and the rows will NOT be sortable.
 * 
 * @author wjohnson000
 *
 */
public class RowTableModelMulti extends RowTableModel {

	static final long serialVersionUID = -3930880096135867133L;

	/** Set containing the header rows for subsequent queries */
	private Set<String> headerRowSet = new HashSet<String>();

	/**
	 * Default constructor
	 */
	public RowTableModelMulti() {
		super();
	}

	/**
	 * If this is the first set of column data make it the primary set.  If
	 * this is a subsequent set, adjust the number of columns in case this
	 * new set of data has more columns than any previous query.  Then add
	 * an empty row, and a row containing the column headers.
	 */
	public void setColumnData(String[] colName, Class<?>[] colType) {
		if (this.colName == null) {
			super.setColumnData(colName, colType);
			this.colType = null;
		} else {
			adjustColumns(colName.length);
			addRow(new String[] { null }, false);
			addRow(colName, false);
			headerRowSet.add("" + (rowData.size()-1));
		}
	}
	
	/**
	 * Adjust the number of columns in the table if the next set of query
	 * results has more columns than is currently being managed.
	 * 
	 * @param newLen number of columns in this set of data.
	 */
	private void adjustColumns(int newLen) {
		if (colName.length < newLen) {
			String[]  cName = new String[newLen]; 
			int[]     cWdth = new int[newLen];
			boolean[] cEmty = new boolean[newLen];

			// Copy the current column data
			for (int i=0;  i<colName.length;  i++) {
				cName[i] = colName[i];
				cWdth[i] = colWidth[i];
				cEmty[i] = colEmpty[i];
			}

			// Add "empty" columns for the new data
			for (int i=colName.length;  i<newLen;  i++) {
				cName[i] = "";
				cWdth[i] = 0;
				cEmty[i] = true;
			}

			colName  = cName;
			colWidth = cWdth;
			colEmpty = cEmty;
			this.fireTableStructureChanged();
		}
	}
	
	/**
	 * Return the Java "Class" of a specific column, always "java.lang.String"
	 * except for the first column, which is a row number
	 * @param col table column number
	 * @return class type
	 */
	public Class<?> getColumnClass(int col) {
		return (colName == null) ? Integer.class : String.class;
	}

	/**
	 * Return whether the given row is a secondary header, i.e., the column
	 * names of a second or subsequent set of query results.
	 * @param row row number to check
	 */
	public boolean isSecondaryHeader(int row) {
		return headerRowSet.contains("" + row);
	}
	
	/**
	 * Prevent any sorting from taking place.  We may have several result sets,
	 * each with a different number of columns and we don't want to intermix the
	 * data from different queries.
	 */
	protected void sortData() {
		
	}
	
}
