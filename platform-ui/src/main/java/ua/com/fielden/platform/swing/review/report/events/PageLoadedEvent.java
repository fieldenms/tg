package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

public class PageLoadedEvent extends EventObject {

    private static final long serialVersionUID = -8684967364329050103L;

    private final IPage<AbstractEntity> loadedPage;

    public PageLoadedEvent(final Object source, final IPage<AbstractEntity> loadedPage) {
	super(source);
	this.loadedPage = loadedPage;
    }

    public IPage<AbstractEntity> getLoadedPage() {
	return loadedPage;
    }
}
