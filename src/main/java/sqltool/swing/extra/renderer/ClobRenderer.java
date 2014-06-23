package sqltool.swing.extra.renderer;

import javax.swing.table.DefaultTableCellRenderer;


/**
 * Custom renderer for "Clob" objects ... the class name will be displayed,
 * with the CLOB contents as a mouse-over ... I hope
 * @author wjohnson000
 *
 */
public class ClobRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 6576696632745691658L;

    public ClobRenderer() { }

    @Override
    protected void setValue(Object value) {
        setText(value==null ? "" : value.getClass().getName());
    }
}
