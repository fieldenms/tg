package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.WizardCancelledEvent;

/**
 * The {@link EventListener} that is notified after the wizard modification had been cancelled.
 * 
 * @author TG Team
 *
 */
public interface IWizardCancelledEventListener extends EventListener {

    /**
     * Invoked after the wizard modification had been cancelled.
     * 
     * @param e
     */
    void wizardCancelled(WizardCancelledEvent e);

}
