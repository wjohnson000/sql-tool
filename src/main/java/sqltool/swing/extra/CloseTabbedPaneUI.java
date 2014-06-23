package sqltool.swing.extra;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Extend the "BasicTabbedPaneUI" by adding a small button with an "X"
 * by with the tab can be closed.
 * 
 * @author wjohnson000
 *
 */
public class CloseTabbedPaneUI extends BasicTabbedPaneUI {

//  ===================================================================
//	static constants
//  ===================================================================
	private static final String TITLE_PADDING = "     ";
	private static final Color  BACKGROUND_COLOR = new Color(238, 238, 238);


	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#createLayoutManager()
	 */
	@Override
	protected LayoutManager createLayoutManager() {
		return new JJClosePaneLayout();
	}


	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getTabInsets(int, int)
	 */
	protected Insets getTabInsets(int tabPlacement,int tabIndex) {
		// Add padding to the tab insets so there is room for a "close" button.
		// Note: the insets returned to us are NOT copies
		Insets defaultInsets = (Insets)super.getTabInsets(tabPlacement,tabIndex).clone();
		defaultInsets.left  -= 15;
		defaultInsets.right += 10;
//		defaultInsets.top;
		defaultInsets.bottom += 4;
		return defaultInsets;
	}


	/**
	 * Class that extends the default "TabbedPanelLayout" by adding a close
	 * button, with an appropriate action handler
	 */
	class JJClosePaneLayout extends TabbedPaneLayout {
		// Set the back-ground color something neutral when instantiated ...
		{
			tabPane.setBackground(BACKGROUND_COLOR);
		}

		// List of our close buttons
		java.util.List<CloseButton> closeButtons = new java.util.ArrayList<CloseButton>();

		/* (non-Javadoc)
		 * @see javax.swing.plaf.basic.BasicTabbedPaneUI.TabbedPaneLayout#layoutContainer(java.awt.Container)
		 */
		@Override
		public void layoutContainer(Container parent) {
			// append some padding to force the optional icon and button
			// text to the left, so it looks nicer
			for (int i=0;  i<tabPane.getTabCount();  i++) {
				String tabTitle = tabPane.getTitleAt(i);
				if (! tabTitle.endsWith(TITLE_PADDING)) {
					tabPane.setTitleAt(i, tabTitle + TITLE_PADDING);
				}
			}
			
			super.layoutContainer(parent);

			// Ensure that there are at least as many close buttons as tabs
			while(tabPane.getTabCount() > closeButtons.size()) {
				closeButtons.add(new CloseButton(closeButtons.size()));
			}

			// Add close buttons for each of the tabs, to the far-right side of the
			// tab heading
			int tab = 0;
			Rectangle rect = new Rectangle();
			for( ;  tab<tabPane.getTabCount();  tab++) {
				rect = getTabBounds(tab, rect);
				JButton closeButton = closeButtons.get(tab);

				//shift the close button 3 down from the top of the pane and 20 to the left
				closeButton.setLocation(rect.x+rect.width-20, rect.y+5);
				closeButton.setSize(15,15);
				tabPane.add(closeButton);
			}

			// Remove any extra close buttons
			for( ;  tab<closeButtons.size();  tab++) {
				tabPane.remove(closeButtons.get(tab));
			}

		}


		// Implement UIResource so that when we add this button to the 
		// tabbed-pane, it doesn't try to make a tab for it!
		class CloseButton extends JButton implements javax.swing.plaf.UIResource {

			private static final long serialVersionUID = 2104279039111220365L;

			public CloseButton(int index) {
				super(new CloseButtonAction(index));
				setToolTipText("Close tab");

				//remove the typical padding for the button
				setMargin(new Insets(0,0,0,0));
				addMouseListener(new MouseAdapter() {
					public void mouseEntered(MouseEvent e) {
						setForeground(new Color(255,0,0));
					}

					public void mouseExited(MouseEvent e) {
						setForeground(new Color(0,0,0));
					}
				});
			}
		}  // End of CloseButton class definition


		// Close button action handler
		class CloseButtonAction extends AbstractAction {

			private static final long serialVersionUID = -2591359865715482947L;

			int index;
			
			public CloseButtonAction(int index) {
				super("x");
				this.index = index;
			}

			public void actionPerformed(ActionEvent e) {
				Component what = tabPane.getComponentAt(index);
				tabPane.remove(index);
				if (what instanceof sqltool.TabParentPanel) {
					((sqltool.TabParentPanel)what).pleaseCleanUp();
				}
			}

		}  // End of CloseButtonAction definition

	} // End of JJClosePaneLayout
}
