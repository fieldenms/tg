package ua.com.fielden.platform.pmodels;

import java.util.Collection;

import ua.com.fielden.platform.events.MultipleSelectionHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * SelectionHolder class holds the selected node of the specified PselectionEventHandler. That class is needed for MultipleDragEventHandler to get all the selectedNode that should
 * be dragged
 * 
 * @author oleh
 * 
 */
public class SelectionHolder {

    private MultipleSelectionHandler selectionHandler;

    /**
     * creates new instance of SelectionHolder class for the specified PSelectionEventHandler
     * 
     * @param handler
     */
    public SelectionHolder(final MultipleSelectionHandler handler) {
        this.selectionHandler = handler;
    }

    /**
     * returns the collection of selected nodes
     * 
     */
    @SuppressWarnings("unchecked")
    public Collection getSelection() {
        return selectionHandler.getSelection();
    }

    public boolean isMarqueeSelection(final PInputEvent pie) {
        return selectionHandler.isSelecting(pie);
    }
}
