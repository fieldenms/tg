package ua.com.fielden.platform.swing.pagination.model.development;

import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

/**
 * An {@link EventObject} that encapsulates page changed event.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class PageChangedEvent extends EventObject {

    private static final long serialVersionUID = 7389638301378300592L;

    private final IPage<? extends AbstractEntity> newPage;

    /**
     * Initiates this {@link PageChangedEvent} with specified {@link PageHolder} instance which page has changed.
     * 
     * @param source - specified {@link IPageHolder} instance which page has changed.
     * @param newPage - new page that was set.
     */
    public PageChangedEvent(final PageHolder source, final IPage<? extends AbstractEntity> newPage) {
	super(source);
	this.newPage = newPage;
    }

    /**
     * Returns the new page.
     * 
     * @return
     */
    public IPage<? extends AbstractEntity> getNewPage() {
	return newPage;
    }

    /**
     * Returns the {@link PageHolder} instance which page was changed.
     */
    @Override
    public PageHolder getSource() {
	return (PageHolder)super.getSource();
    }
}
