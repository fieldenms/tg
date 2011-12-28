package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

/**
 * {@link EventObject} that represents wizard's build or cancel events.
 * 
 * @author TG Team
 *
 */
public class WizardEvent extends EventObject {

    private static final long serialVersionUID = -8370129218433979205L;

    /**
     * Special wizard actions: build, cancel.
     * 
     * @author TG Team
     *
     */
    public enum WizardAction{
	PRE_BUILD, BUILD, POST_BUILD,
	PRE_CANCEL, CANCEL, POST_CANCEL;
    }

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
