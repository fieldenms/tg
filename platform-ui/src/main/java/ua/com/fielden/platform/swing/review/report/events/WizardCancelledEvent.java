package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;

/**
 * Represents the event that was generated after the entity review wizard cancelled it's changes.
 * 
 * @author TG Team
 *
 */
public class WizardCancelledEvent extends EventObject {

    private static final long serialVersionUID = 4891621228278022319L;

    /**
     * Initiates this event object with {@link AbstractConfigurationModel} - the configuration model where event occurred.
     * 
     * @param source
     */
    public WizardCancelledEvent(final AbstractConfigurationModel source) {
	super(source);
    }

    @Override
    public AbstractConfigurationModel getSource() {
	return (AbstractConfigurationModel)super.getSource();
    }

}
