package ua.com.fielden.platform.swing.review.report.interfaces;


/**
 * A contract for <i>entity review</i> that allows one to be configured, saved, removed.
 * 
 * @author TG Team
 *
 */
public interface IReview {

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

    /**
     * Adds {@link IReviewEventListener} instance to listen review events.
     * 
     * @param l
     */
    void addReviewEventListener(IReviewEventListener l);

    /**
     * Removes {@link IReviewEventListener} instance.
     * 
     * @param l
     */
    void removeReviewEventListener(IReviewEventListener l);
}
