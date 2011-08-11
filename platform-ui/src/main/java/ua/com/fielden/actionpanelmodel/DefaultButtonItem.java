package ua.com.fielden.actionpanelmodel;

import javax.swing.JToolBar;

import ua.com.fielden.platform.swing.actions.Command;

/**
 * Represents the tool bar button and implements {@link IActionItem}
 * 
 * @author oleh
 * 
 */
public class DefaultButtonItem implements IActionItem {

    private final Command<?> action;

    /**
     * creates new {@link DefaultButtonItem} for the specified action
     * 
     * @param action
     */
    public DefaultButtonItem(final Command<?> action) {
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
    public Command<?> getAction() {
	return action;
    }

}
