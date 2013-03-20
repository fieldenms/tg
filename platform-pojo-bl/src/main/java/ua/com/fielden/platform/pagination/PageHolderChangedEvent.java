package ua.com.fielden.platform.pagination;

import java.util.EventObject;

/**
 * An {@link EventObject} that encapsulates the {@link IPaginatorModel}'s page holder changed event.
 * 
 * @author TG Team
 *
 */
public class PageHolderChangedEvent extends EventObject {

    private static final long serialVersionUID = -6644678630479736195L;

    private final PageHolder pageHolder;

    /**
     * Initiates this {@link PageHolderChangedEvent} with specified {@link IPaginatorModel} which page holder has changed.
     * 
     * @param source - the specified {@link IPaginatorModel} which page holder has changed.
     * @param pageSupport - new {@link PageHolder} instance.
     */
    public PageHolderChangedEvent(final IPaginatorModel source, final PageHolder pageSupport) {
	super(source);
	this.pageHolder = pageSupport;
    }

    /**
     * Returns the new instance of the {@link PageHolder}.
     * 
     * @return
     */
    public PageHolder getPageHolder() {
	return pageHolder;
    }

    /**
     * Returns the {@link IPaginatorModel} which page holder has changed.
     */
    @Override
    public IPaginatorModel getSource() {
	return (IPaginatorModel)super.getSource();
    }
}
