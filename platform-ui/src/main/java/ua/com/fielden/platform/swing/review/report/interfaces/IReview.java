package ua.com.fielden.platform.swing.review.report.interfaces;

import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;


/**
 * A contract for <i>entity review</i> that allows one to be configured.
 * 
 * @author TG Team
 *
 */
public interface IReview{

    /**
     * Returns the action that switches the view in to wizard mode.
     * 
     * @return
     */
    ConfigureAction getConfigureAction();
}
