package sqltool.common;

import java.awt.event.ActionEvent;


/**
 * Simple interface to indicate that a class can handle menu events.
 * @author wjohnson000
 *
 */
public interface MenuHandler {

	/**
	 * Callback for a menu event
	 * @param ae menu event
	 */
	public void handleMenuEvent(ActionEvent ae);

	/**
	 * Callback to notify that an instance is now the active object in
	 * the system, and will receive menu events
	 */
	public void becomeActive();
}
