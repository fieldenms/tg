package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

/**
 * {@link EventObject} that represents review's configure event.
 * 
 * @author TG Team
 *
 */
public class ReviewEvent extends EventObject {

    private static final long serialVersionUID = -8695504451663349353L;

    /**
     * Represents phases of review actions: configure, save, save as, save as default, load default, remove.
     * The save as default and load default actions must be available only for entity locators.
     * The save as action must be disabled for entity locator.
     * The remove action must be disabled for entity locator and principle entity centre.
     * 
     * @author TG Team
     *
     */
    public enum ReviewAction{
	PRE_CONFIGURE, CONFIGURE, POST_CONFIGURE, CONFIGURE_FAILED;
    }

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

    public ReviewAction getReviewAction() {
	return reviewAction;
    }

}
