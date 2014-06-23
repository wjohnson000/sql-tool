package sqltool.swing.extra.renderer;

import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Custom renderer to include/exclude the decimal digits of a number
 * depending on whether or not it's a whole number value.
 * @author wjohnson000
 *
 */
public class DecimalRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 6576696632745691658L;

	DecimalFormat df1;  // Decimal format for whole numbers
	DecimalFormat df2;  // Decimal format for fractional numbers
	DecimalFormat df3;  // Decimal format for lots of digits

    public DecimalRenderer() {
        this.df1 = new DecimalFormat("0");
        this.df2 = new DecimalFormat("0.00");
        this.df3 = new DecimalFormat("0.000000");
        this.setHorizontalAlignment(JLabel.RIGHT);
        this.df1.setParseBigDecimal(true);
        this.df2.setParseBigDecimal(true);
        this.df3.setParseBigDecimal(true);
    }

    @Override
    protected void setValue(Object value) {
    	String sValue = "";
    	if (value != null) {
    	    sValue = df3.format(value);
    	    if (sValue.endsWith("00")) {
                sValue = df2.format(value);
                if (sValue.endsWith(".00")) {
                    sValue = df1.format(value);
                }
    	    }
    	}
        setText(sValue);
    }

}
