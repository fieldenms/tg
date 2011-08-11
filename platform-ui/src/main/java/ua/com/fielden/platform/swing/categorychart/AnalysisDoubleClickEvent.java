package ua.com.fielden.platform.swing.categorychart;

import java.util.EventObject;

/**
 * This {@link EventObject} represents double click event that occurred on the analysis report view.
 * 
 * @author oleh
 * 
 */
public class AnalysisDoubleClickEvent extends EventObject {

    private static final long serialVersionUID = 7701847634577049113L;

    private final EventObject sourceMouseEvent;

    public AnalysisDoubleClickEvent(final Object source, final EventObject sourceMouseEvent) {
	super(source);
	this.sourceMouseEvent = sourceMouseEvent;
    }

    /**
     * Returns the source mouse double click event object.
     * 
     * @return
     */
    public EventObject getSourceMouseEvent() {
	return sourceMouseEvent;
    }

}
