package ua.com.fielden.platform.swing.actions;

import ua.com.fielden.platform.swing.components.ActionChangeButton;

/**
 * ActionChanger class must be used with a ActionChangeButton, to get all the features of the action. first create the ActionChanger and then use it in the addAction method of the
 * ActionChangeButton to initialize it
 * 
 * @author oleh
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class ActionChanger<T> extends Command<T> {

    private ActionChangeButton button;

    /**
     * creates new ActionChanger instance for the given name
     * 
     * @param name
     */
    public ActionChanger(final String name) {
	super(name);
	setButton(null);
    }

    /**
     * get the ActionChangeButton instance
     * 
     * @return
     */
    public ActionChangeButton getButton() {
	return button;
    }

    /**
     * set the button which action must be changed
     * 
     * @param button
     *            - specified button
     */
    public void setButton(final ActionChangeButton button) {
	this.button = button;
    }

    @Override
    protected boolean preAction() {
	final boolean result = super.preAction();
	if (button != null) {
	    button.setDefaultAction(this);
	}
	return result;
    }
}
