package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;


/**
 * {@link EventListener} that listens the review's configure action phases.
 * 
 * @author TG Team
 *
 */
public interface IReviewEventListener extends EventListener {

    /**
     * Invoked when configure action performed.
     * 
     * @param e
     * @return
     */
    boolean configureActionPerformed(ReviewEvent e);
}
