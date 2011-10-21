package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.interfaces.IWizard.WizardAction;

/**
 * {@link EventObject} that represents wizard's build or cancel events.
 * 
 * @author TG Team
 *
 */
public class WizardEvent extends EventObject {

    private static final long serialVersionUID = -8370129218433979205L;

    private final WizardAction wizardAction;

    /**
     * Initiates {@link WizardEvent} with source object and {@link WizardAction} instance.
     * 
     * @param source - determines the object where event occurred.
     * @param wizardAction - determines the wizard's event phase.
     */
    public WizardEvent(final Object source, final WizardAction wizardAction) {
	super(source);
	this.wizardAction = wizardAction;
    }

    /**
     * Returns the event phase.
     * 
     * @return
     */
    public WizardAction getWizardAction() {
	return wizardAction;
    }

}
