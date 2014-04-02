package ua.com.fielden.platform.swing.review.report.centre;

import javax.swing.Action;

/**
 * Represents configurable criteria panel.
 * 
 * @author TG Team
 * 
 */
public interface ICriteriaPanel {

    /**
     * Returns value that indicates whether this criteria panel can be configured.
     * 
     * @return
     */
    boolean canConfigure();

    /**
     * Returns the action that allows one to switch criteria panel between configure and view modes.
     * 
     * @return
     */
    Action getSwitchAction();

    /**
     * Must updates the underlying model according to it's view.
     * 
     * @return
     */
    public void updateModel();
}
