package ua.com.fielden.platform.swing.review.optionbuilder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.jvnet.flamingo.common.ElementState;

import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.components.ActionChangeButton;

public class ActionChangerBuilder {

    private final List<ActionChanger<?>> actions = new ArrayList<ActionChanger<?>>();

    public ActionChangerBuilder() {
	actions.clear();
    }

    /**
     * Adds or replaces the specified action. (Based on the name of the action).
     * 
     * @param action
     */
    public void setAction(final ActionChanger<?> action) {
	final int index = getActionIndex(action);
	if (index < 0) {
	    actions.add(action);
	} else {
	    actions.set(index, action);
	}
    }

    /**
     * Returns the index of the specified action. (Based on the name of the action).
     * 
     * @param action
     * @return
     */
    private int getActionIndex(final ActionChanger<?> action) {
	final String name = (String) action.getValue(Action.NAME);
	for (int index = 0; index < actions.size(); index++) {
	    final String actionName = (String) actions.get(index).getValue(Action.NAME);
	    if (name.equals(actionName)) {
		return index;
	    }
	}
	return -1;
    }

    /**
     * Returns the {@link ActionChanger} instance for specified action name. Returns null if there is no action with such name.
     * 
     * @param name
     * @return
     */
    public ActionChanger<?> getActionChanger(final String name) {
	if (StringUtils.isEmpty(name)) {
	    throw new IllegalArgumentException("It's impossible to search for an action with empty name");
	}
	for (final ActionChanger<?> action : actions) {
	    final String actionName = (String) action.getValue(Action.NAME);
	    if (name.equals(actionName)) {
		return action;
	    }
	}
	return null;
    }

    /**
     * Builds the action changer button.
     * 
     * @param actionOrder
     * @return
     */
    public JComponent buildActionChanger(final List<String> actionOrder) {
	if (actionOrder == null) {
	    return null;
	}
	final List<String> distinctValues = new ArrayList<String>();
	for (final String name : actionOrder) {
	    if (!distinctValues.contains(name)) {
		distinctValues.add(name);
	    }
	}
	final List<ActionChanger<?>> changers = new ArrayList<ActionChanger<?>>();
	for (final String actionName : distinctValues) {
	    final ActionChanger<?> action = getActionChanger(actionName);
	    if (action != null) {
		changers.add(action);
	    }
	}
	if (changers.isEmpty()) {
	    return null;
	}
	if (changers.size() == 1) {
	    return new JButton(changers.get(0));
	}
	final ActionChangeButton button = new ActionChangeButton(changers.get(0));
	button.setState(ElementState.MEDIUM, true);
	button.setFlat(false);
	for (int actionIndex = 1; actionIndex < changers.size(); actionIndex++) {
	    button.addAction(changers.get(actionIndex));
	}
	return button;
    }
}
