package ua.com.fielden.platform.swing.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.PopupMenuListener;
import org.jvnet.flamingo.common.icon.ResizableIcon;

import ua.com.fielden.platform.swing.actions.ActionChanger;

/**
 * ActionChangeButton is used to perform action and change it's appearance according to the item that was selected
 * 
 * @author oleh
 * 
 */
public class ActionChangeButton extends JCommandButton {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unchecked")
    private ActionChanger defaultAction;
    @SuppressWarnings("unchecked")
    final private List<ActionChanger> actions = new ArrayList<ActionChanger>();

    /**
     * creates new instance of the ActionChangeButton and set the given action as the default action for the button
     * 
     * @param action
     *            - specified default action
     */
    @SuppressWarnings("unchecked")
    public ActionChangeButton(final ActionChanger action) {
	this((String) action.getValue(Action.NAME), (ResizableIcon) action.getValue(Action.LARGE_ICON_KEY));
	addAction(action);
	setDefaultAction(action);
	setState(ElementState.SMALL, true);
	setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);

	this.addPopupMenuListener(new PopupMenuListener() {

	    @Override
	    public void menuAboutToShow(final JPopupMenu popup) {
		for (final Action action : actions) {
		    popup.add(new JMenuItem(action));
		}
	    }

	});
    }

    private ActionChangeButton(final String name, final ResizableIcon icon) {
	super(name, icon);
    }

    /**
     * 
     * @return the default action
     */
    @SuppressWarnings("unchecked")
    public ActionChanger getDefaultAction() {
	return defaultAction;
    }

    /**
     * set the default action for the button and removes the previous one
     * 
     * @param defaultAction
     *            - specified new default action
     */
    @SuppressWarnings("unchecked")
    public void setDefaultAction(final ActionChanger defaultAction) {
	if ((defaultAction != null) && (actions.indexOf(defaultAction) != -1)) {
	    removeActionListener(this.defaultAction);
	    this.defaultAction = defaultAction;
	    addActionListener(this.defaultAction);
	    final Object objIcon = defaultAction.getValue(Action.LARGE_ICON_KEY);
	    if (objIcon instanceof ResizableIcon) {
		setIcon((ResizableIcon) objIcon);
	    }
	    setText((String) defaultAction.getValue(Action.NAME));
	    setToolTipText((String) defaultAction.getValue(Action.SHORT_DESCRIPTION));
	}
    }

    /**
     * append new menu item to the pop-up menu and save the action to the actions list
     * 
     * @param action
     *            - specified action for the new JMenuItem instance
     */
    @SuppressWarnings("unchecked")
    public void addAction(final ActionChanger action) {
	if ((action != null) && (actions.indexOf(action) == -1)) {
	    actions.add(action);
	    action.setButton(this);
	}
    }

    /**
     * removes the menu item from the pop-up menu
     * 
     * @param index
     *            - index of the menu item that must be removed
     * @return the removed action from menu item
     */
    @SuppressWarnings("unchecked")
    public ActionChanger removeAction(final int index) {
	final ActionChanger action = actions.remove(index);
	action.setButton(null);
	return action;
    }

    /**
     * removes the menu item from the pop-up menu
     * 
     * @param action
     *            - specified action that must be removed
     * @return the removed action from menu item
     */
    @SuppressWarnings("unchecked")
    public ActionChanger removeAction(final ActionChanger action) {
	final int index = actions.indexOf(action);
	return removeAction(index);
    }

}
