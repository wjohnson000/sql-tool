package sqltool.swing.extra;

import javax.swing.JTabbedPane;


/**
 * Extend the "JTabbedPane" class by adding the ability to include a small
 * "X" icon by which the tab can be closed.
 * 
 * @author wjohnson000
 *
 */
public class CloseTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = -4473729018462033111L;

	/**
	 * Default constructor
	 */
	public CloseTabbedPane() {
		super();
		setCloseTabbedPaneUI();
	}

	/**
	 * Constructor with initial tab placement
	 * @param tabPlacement JTabbedPane.[TOP,BOTTOM,LEFT,RIGHT]
	 */
	public CloseTabbedPane(int tabPlacement) {
		super(tabPlacement);
		setCloseTabbedPaneUI();
	}

	/**
	 * Constructor with initial tab placement and layout policy
	 * @param tabPlacement JTabbedPane.[TOP,BOTTOM,LEFT,RIGHT]
	 * @param tabLayoutPolicy JTabbedPane.[WRAP_TAB_LAYOUT,SCROLL_TAB_LAYOUT]
	 */
	public CloseTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		setCloseTabbedPaneUI();
	}

	/**
	 * Set the UI object which implements the look-and-feel
	 */
	private void setCloseTabbedPaneUI() {
		setUI(new CloseTabbedPaneUI());
	}
}
