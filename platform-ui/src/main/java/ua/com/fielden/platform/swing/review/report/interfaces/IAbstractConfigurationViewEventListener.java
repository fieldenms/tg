package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;

/**
 * An {@link EventListener} that listens the open event of the abstract configuration view.
 * 
 * @author TG Team
 *
 */
public interface IAbstractConfigurationViewEventListener extends EventListener {

    /**
     * Invoked when open method on {@link AbstractConfigurationView} had been invoked.
     * 
     * @param event
     * @return
     */
    Result abstractConfigurationViewEventPerformed(AbstractConfigurationViewEvent event);
}
