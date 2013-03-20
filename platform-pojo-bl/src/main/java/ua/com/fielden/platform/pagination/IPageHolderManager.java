package ua.com.fielden.platform.pagination;


/**
 * A contract for anything that manages {@link PageHolder} set.
 * 
 * @author TG Team
 */
public interface IPageHolderManager {

    /**
     * Selects the specified {@link PageHolder}.
     * 
     * @param pageHolder
     */
    void selectPageHolder(PageHolder pageHolder);

    /**
     * Allows one to add another {@link PageHolder} instance to this {@link IPageHolderManager}.
     * 
     * @param pageHolder
     */
    void addPageHolder(PageHolder pageHolder);

    /**
     * Removes the specified {@link PageHolder} instance from this {@link IPageHolderManager}.
     * 
     * @param pageHolder
     */
    void removePageHolder(PageHolder pageHolder);
}
