package ua.com.fielden.platform.swing.pagination.model.development;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

/**
 * Contract for Paginator. Declares API that supports multiple page by page review.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPaginatorModel {

    /**
     * Specifies page navigation phases (i.e. pre-, post- navigation and page navigation exception handling phases).
     *
     * @author TG Team
     *
     */
    public enum PageNavigationPhases {

	PRE_NAVIGATE, NAVIGATE, POST_NAVIGATE, PAGE_NAVIGATION_EXCEPTION;
    }

    /**
     * Returns the currently selected {@link PageHolder} instance.
     *
     * @return
     */
    PageHolder getCurrentPageHolder();

    /**
     * Returns the current page of data from the current {@link PageHolder} instance. If the current page holder (see {@link #getCurrentPageHolder()}) is not specified then this method returns null.
     *
     * @return
     */
    IPage<? extends AbstractEntity> getCurrentPage();

    /**
     * Navigates the page of the current {@link PageHolder} to the first page.
     */
    void firstPage();

    /**
     * Navigates the page of the current {@link PageHolder} to the previous page.
     */
    void prevPage();

    /**
     * Navigates the page of the current {@link PageHolder} to the next page.
     */
    void nextPage();

    /**
     * Navigates the page of the current {@link PageHolder} to the last page.
     */
    void lastPage();

    /**
     * Process some actions during page navigation phases specified with {@link PageNavigationPhases} parameter
     *
     * @param pageNavigationPhases
     */
    void pageNavigationPhases(PageNavigationPhases pageNavigationPhases);

    /**
     * Adds the specified {@link IPageChangedListener} that listens the page changed events.
     *
     * @param l
     */
    void addPageChangedListener(IPageChangedListener l);

    /**
     * Removes the specified {@link IPageChangedListener} that listens the page changed events.
     *
     * @param l
     */
    void removePageChangedListener(IPageChangedListener l);

    /**
     * Adds the specified {@link IPageHolderChangedListener} that listens the page holder changed events.
     *
     * @param l
     */
    void addPageHolderChangedListener(IPageHolderChangedListener l);

    /**
     * Removes the specified {@link IPageHolderChangedListener} that listens the page holder changed events.
     *
     * @param l
     */

    void removePageHolderChangedListener(IPageHolderChangedListener l);
}
