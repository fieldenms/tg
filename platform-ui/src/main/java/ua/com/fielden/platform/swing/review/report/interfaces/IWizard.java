package ua.com.fielden.platform.swing.review.report.interfaces;

import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.BuildAction;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.CancelAction;

/**
 * A contract for <i>wizard</i> that allows one to build specific view.
 * 
 * @author TG Team
 * 
 */
public interface IWizard {

    /**
     * Returns the action that accepts wizard modification and builds the report view.
     * 
     * @return
     */
    BuildAction getBuildAction();

    /**
     * Returns the action that discards wizard modification and builds the report view.
     * 
     * @return
     */
    CancelAction getCancelAction();
}
