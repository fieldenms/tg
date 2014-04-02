package ua.com.fielden.platform.actionpanelmodel;

import java.util.List;

import javax.swing.JToolBar;

import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.components.ActionChangeButton;

/**
 * represents {@link ActionChangeButton} control
 * 
 * @author oleh
 * 
 */
public class ChangeActionButtonItem implements IActionItem {

    private final List<? extends ActionChanger<?>> actionList;

    /**
     * creates new instance of {@link ChangeActionButtonItem} and initiate the list of the {@link ActionChangeButton} with specified one
     * 
     * @param actionList
     */
    public ChangeActionButtonItem(final List<? extends ActionChanger<?>> actionList) {
        this.actionList = actionList;
    }

    /**
     * creates the {@link ActionChangeButton} instance and places it on the specified tool bar instance
     */
    @Override
    public void build(final JToolBar toolBar) {
        if (actionList.size() != 0) {
            final ActionChangeButton changeButton = new ActionChangeButton(actionList.get(0));
            for (int actionIndex = 1; actionIndex < actionList.size(); actionIndex++) {
                changeButton.addAction(actionList.get(actionIndex));
            }
            toolBar.add(changeButton);
        }
    }

    /**
     * returns the list of {@link ActionChangeButton} actions
     * 
     * @return
     */
    public List<? extends ActionChanger<?>> getActionList() {
        return actionList;
    }

}
