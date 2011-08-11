package ua.com.fielden.platform.swing.menu.filter;

import javax.swing.tree.TreeNode;

/**
 * Interface that can be used with any other model to filter entities
 * 
 * @author oleh
 * 
 */
public interface IFilterableModel {

    /**
     * Applies filtering to the underlying model. This method should be invoked each time filtering criterion changes.
     * 
     * @param value
     *            -- filtering criterion
     */
    void filter(final String value);

    /**
     * Adds a filter to the model. There can be more than one filter.
     * <p>
     * If <code>andMode</code> is <code>true</code> then filters are joined with logical operation <code>and</code>, otherwise <code>or<code>.
     * 
     * @param filter
     */
    void addFilter(final IFilter filter);

    /**
     * Removes filter from the model
     * 
     * @param filter
     */
    void removeFilter(final IFilter filter);

    /**
     * Removes filter from the model by index.
     * 
     * @param index
     *            -- index of the filter to be removed from the model
     * @return -- removed filter
     */
    IFilter removeFilter(final int index);

    /**
     * Returns filter by index.
     * 
     * @param index
     *            - index of the filter to return
     * @return
     */
    IFilter getFilter(final int index);

    /**
     * Determines the current <code>andMode</code> -- if it's <code>true</code> then filters are joined with <code>and</code>, otherwise with <code>or</code>.
     * 
     * @return
     */
    boolean isAndMode();

    /**
     * Specifies the <code>andMode</code> (see also {@link #isAndMode()}).
     * 
     * @param andMode
     */
    void setAndMode(boolean andMode);

    /**
     * Should return <code>true</code> if the specified node matches the last filter criteria.
     * 
     * @param node
     * @return
     */
    boolean matches(TreeNode node);

    void addFilterListener(final IFilterListener listener);

    void removeFilterListener(final IFilterListener listener);

    void addFilterBreakListener(final IFilterBreakListener listener);

    void removeFilterBreakListener(final IFilterBreakListener listener);
}
