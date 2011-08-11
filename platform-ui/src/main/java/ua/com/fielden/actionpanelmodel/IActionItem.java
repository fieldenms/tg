package ua.com.fielden.actionpanelmodel;

import javax.swing.JToolBar;

import ua.com.fielden.platform.swing.components.ActionChangeButton;

/**
 * Represents the control (button, separator, {@link ActionChangeButton}, other components) that can be placed on the tool bar
 * 
 * @author oleh
 * 
 */
public interface IActionItem {

    /**
     * creates the control and places it on the tool bar
     * 
     * @param toolBar
     *            - specified tool bar on which the control must be placed
     */
    void build(JToolBar toolBar);
}
