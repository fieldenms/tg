package ua.com.fielden.platform.swing.review.report.interfaces;

/**
 * A contract for <i>entity review</i> that allows one to be configured, saved, removed.
 * 
 * @author TG Team
 *
 */
public interface IConfigurable {

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
	PRE_REMOVE, REMOVE, POST_REMOVE;
    }

    /**
     * Adds {@link ReviewEventListener} instance to listen review events.
     * 
     * @param l
     */
    void addConfigureEventListener(ReviewEventListener l);

    /**
     * Removes {@link ReviewEventListener} instance.
     * 
     * @param l
     */
    void removeConfigureEventListener(ReviewEventListener l);
}
