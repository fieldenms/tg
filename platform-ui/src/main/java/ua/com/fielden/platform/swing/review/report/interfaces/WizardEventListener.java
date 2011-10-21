package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.WizardEvent;

/**
 * {@link EventListener} that listens wizard's build and cancel action phases.
 * 
 * @author TG Team
 *
 */
public interface WizardEventListener extends EventListener {

    /**
     * Invoked when build or cancel action performed.
     * 
     * @param e
     * @return
     */
    boolean wizardActionPerformed(WizardEvent e);
}
