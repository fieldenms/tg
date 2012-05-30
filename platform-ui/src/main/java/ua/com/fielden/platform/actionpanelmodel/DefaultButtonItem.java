package ua.com.fielden.platform.actionpanelmodel;

import javax.swing.Action;
import javax.swing.JToolBar;

/**
 * Represents the tool bar button and implements {@link IActionItem}
 * 
 * @author oleh
 * 
 */
public class DefaultButtonItem implements IActionItem {

    private final Action action;

    /**
     * creates new {@link DefaultButtonItem} for the specified action
     * 
     * @param action
     */
    public DefaultButtonItem(final Action action) {
	this.action = action;
    }

    /**
     * creates the tool bar button
     */
    @Override
    public void build(final JToolBar toolBar) {
	toolBar.add(action);
    }

    /**
     * returns the action that was used for creating this instance of {@link DefaultButtonItem}
     * 
     * @return
     */
    public Action getAction() {
	return action;
    }

}
