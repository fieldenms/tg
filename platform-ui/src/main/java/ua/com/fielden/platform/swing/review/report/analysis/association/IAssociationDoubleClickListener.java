package ua.com.fielden.platform.swing.review.report.analysis.association;

import java.util.EventListener;

/**
 * An {@link EventListener} that listens the double click event on {@link AssociationTable}.
 * 
 * @author TG Team
 * 
 */
public interface IAssociationDoubleClickListener extends EventListener {

    /**
     * Invoked after association table's cell have been clicked.
     * 
     * @param event
     */
    void cellDoubleClicked(AssociationClickEvent event);
}
