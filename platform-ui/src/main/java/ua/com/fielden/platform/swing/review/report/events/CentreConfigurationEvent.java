package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

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

    /**
     * The action phase. It is one of the available {@link CentreConfigurationAction}s.
     */
    private final CentreConfigurationAction eventAction;

    /**
     * The name of the saved as configuration. It has value different then null when the {@link #eventAction} is {@link CentreConfigurationAction#PRE_SAVE_AS}.
     */
    private final String saveAsName;

    /**
     * Represents the exception that occurred during one of the centre configuration actions.
     */
    private final Throwable exception;

    /**
     * Indicates whether save as action was fired during close action or not.
     */
    private final boolean afterClose;

    /**
     * Initiates this {@link CentreConfigurationEvent} with instance of {@link CentreConfigurationModel} where the event was generated and the {@link CentreConfigurationAction}.
     *
     * @param source
     * @param eventAction
     */
    public CentreConfigurationEvent(final CentreConfigurationView<?, ?> source, final String saveAsName, final Throwable exception, final boolean afterClose, final CentreConfigurationAction eventAction) {
	super(source);
	this.eventAction = eventAction;
	switch(eventAction){
	case PRE_SAVE_AS:
	case POST_SAVE_AS:
	case SAVE_AS:
	    this.saveAsName = saveAsName;
	    this.exception = null;
	    this.afterClose = afterClose;
	    break;
	case SAVE_AS_FAILED:
	    this.saveAsName = saveAsName;
	    this.exception = exception;
	    this.afterClose = afterClose;
	    break;
	case SAVE_FAILED:
	case REMOVE_FAILED:
	    this.saveAsName = null;
	    this.exception = exception;
	    this.afterClose = false;
	    break;
	default:
	    this.exception = null;
	    this.saveAsName = null;
	    this.afterClose = false;
	}
    }

    @Override
    public CentreConfigurationView<?, ?> getSource() {
	return (CentreConfigurationView<?, ?>)super.getSource();
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

    /**
     * Returns the exception that occurred during one of the centre configuration actions
     *
     * @return
     */
    public Throwable getException() {
	return exception;
    }

    /**
     * Returns value that indicates whether save as action fired up during close action or not.
     *
     * @return
     */
    public boolean isAfterClose() {
	return afterClose;
    }
}
