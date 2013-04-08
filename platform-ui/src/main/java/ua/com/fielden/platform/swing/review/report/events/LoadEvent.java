package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.interfaces.ILoadingNode;

/**
 * An {@link EventObject} that specifies the component that was loaded
 *
 * @author TG Team
 *
 */
public class LoadEvent extends EventObject {

    private static final long serialVersionUID = 190489982304734671L;

    /**
     * Initialises {@link LoadEvent} with loaded component
     *
     * @param source
     */
    public LoadEvent(final ILoadingNode source) {
	super(source);
    }
}
