package ua.com.fielden.platform.swing.review.report.interfaces;


/**
 * A contract for <i>entity review</i> that allows one to be configured, saved, removed.
 * 
 * @author TG Team
 *
 */
public interface IReview extends ISelectable{

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
