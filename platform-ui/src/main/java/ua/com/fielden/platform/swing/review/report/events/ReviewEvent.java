package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.interfaces.IConfigurable.ReviewAction;

/**
 * {@link EventObject} that represents review's configure event.
 * 
 * @author TG Team
 *
 */
public class ReviewEvent extends EventObject {

    private static final long serialVersionUID = -8695504451663349353L;

    private final ReviewAction reviewAction;

    /**
     * Initiates this {@link ReviewEvent} with source object and {@link ReviewAction} instance.
     * 
     * @param source - determines the object where event occurred.
     * @param reviewAction - determines the configure event phase.
     */
    public ReviewEvent(final Object source, final ReviewAction reviewAction) {
	super(source);
	this.reviewAction = reviewAction;
    }

    public ReviewAction getConfigureAction() {
	return reviewAction;
    }

}
