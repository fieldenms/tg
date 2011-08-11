package ua.com.fielden.platform.swing.menu.filter;

/**
 * Filter contract.
 * 
 * @author oleh
 * 
 * @param <T>
 */
public interface IFilter {

    /**
     * Should return true if value does not matcher filtering criterion.
     * 
     * @param value
     *            - value that must be tested
     * @param filterCrit
     *            - filtering criterion
     * @return
     */
    boolean filter(Object value, String filterCrit);

    /**
     * Determines whether filter is enabled.
     * 
     * @return
     */
    boolean isEnabled();

    /**
     * Enables/disables the filter.
     */
    void setEnabled(final boolean enabled);
}
