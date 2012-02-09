package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;

/**
 * An {@link EventListener} that listens centre configuration related events.
 * 
 * @author TG Team
 *
 */
public interface ICentreConfigurationEventListener extends EventListener {

    /**
     * Invoked after the {@link CentreConfigurationEvent} was thrown.
     * 
     * @param event
     * @return
     */
    boolean centerConfigurationEventPerformed(CentreConfigurationEvent event);
}
