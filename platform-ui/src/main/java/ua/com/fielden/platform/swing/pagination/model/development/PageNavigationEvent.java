package ua.com.fielden.platform.swing.pagination.model.development;

import java.util.EventObject;

import ua.com.fielden.platform.swing.pagination.model.development.IPaginatorModel.PageNavigationPhases;

/**
 * Encapsulates the page navigation event.
 * 
 * @author TG Team
 *
 */
public class PageNavigationEvent extends EventObject {

    private static final long serialVersionUID = -3469137465853295849L;

    private final PageNavigationPhases pageNavigationPhases;

    /**
     * Initiates {@link PageNavigationEvent} with page holder which has thrown that event and {@link PageNavigationPhases} instance.
     * 
     * @param source
     * @param pageNavigationPhases
     */
    public PageNavigationEvent(final PageHolder source, final PageNavigationPhases pageNavigationPhases) {
	super(source);
	this.pageNavigationPhases = pageNavigationPhases;
    }

    /**
     * Returns the page navigation phase. (See {@link PageNavigationPhases} for more information).
     * 
     * @return
     */
    public PageNavigationPhases getPageNavigationPhases() {
	return pageNavigationPhases;
    }

    @Override
    public PageHolder getSource() {
	return (PageHolder)super.getSource();
    }
}
