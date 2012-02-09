package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;

/**
 * An {@link EventObject} that is thrown when centre configuration related event occurred.
 * 
 * @author TG Team
 *
 */
public class CentreConfigurationEvent extends EventObject {

    private static final long serialVersionUID = 5346629269498132588L;

    /**
     * Represents the phases of the Centre configuration events: save, save as, remove.
     * 
     * @author TG Team
     *
     */
    public enum CentreConfigurationAction{
	PRE_SAVE, SAVE, POST_SAVE, SAVE_FAILED,
	PRE_SAVE_AS, SAVE_AS, POST_SAVE_AS, SAVE_AS_FAILED,
	PRE_REMOVE, REMOVE, POST_REMOVE, REMOVE_FAILED;
    }

    private final CentreConfigurationAction eventAction;

    /**
     * The name of the saved as configuration. It has value different then null when the {@link #eventAction} is {@link CentreConfigurationAction#PRE_SAVE_AS}.
     */
    private final String saveAsName;

    /**
     * Initiates this {@link CentreConfigurationEvent} with instance of {@link CentreConfigurationModel} where the event was generated and the {@link CentreConfigurationAction}.
     * 
     * @param source
     * @param eventAction
     */
    public CentreConfigurationEvent(final CentreConfigurationModel<?> source, final String saveAsName,final CentreConfigurationAction eventAction) {
	super(source);
	this.eventAction = eventAction;
	switch(eventAction){
	case PRE_SAVE_AS:
	case SAVE:
	case POST_SAVE_AS:
	case SAVE_AS_FAILED:
	    this.saveAsName = saveAsName;
	    break;
	default:
	    this.saveAsName = null;
	}
    }

    @Override
    public CentreConfigurationModel<?> getSource() {
	return (CentreConfigurationModel<?>)super.getSource();
    }

    /**
     * Returns the {@link CentreConfigurationAction} that indicates the type of generated event.
     * 
     * @return
     */
    public CentreConfigurationAction getEventAction() {
	return eventAction;
    }

    /**
     * Returns the name of the save as configuration.
     * 
     * @return
     */
    public String getSaveAsName() {
	return saveAsName;
    }
}
