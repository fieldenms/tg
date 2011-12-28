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
     * Represents phases of review actions: configure, save, save as, remove.
     * 
     * @author TG Team
     *
     */
    public enum ReviewAction{
	PRE_CONFIGURE, CONFIGURE, POST_CONFIGURE,
	PRE_SAVE, SAVE, POST_SAVE,
	PRE_SAVE_AS, SAVE_AS, POST_SAVE_AS,
	PRE_SAVE_AS_DEFAULT, SAVE_AS_DEFAULT, POST_SAVE_AS_DEFAULT,
	PRE_REMOVE, REMOVE, POST_REMOVE;
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

    public ReviewAction getConfigureAction() {
	return reviewAction;
    }

}
