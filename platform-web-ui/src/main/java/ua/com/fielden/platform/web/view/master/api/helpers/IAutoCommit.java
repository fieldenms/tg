package ua.com.fielden.platform.web.view.master.api.helpers;

/**
 * This is an interface to provide auto-commit functionality after property addition.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutoCommit<T> {
    
    /**
     * This declaration indicates that validation should be auto-triggered after {@code millis} milliseconds after user input.
     * 
     * @param millis -- approximate number of milliseconds after user input to trigger auto-committing of the value
     */
    T autoCommit(final int millis);
    
}