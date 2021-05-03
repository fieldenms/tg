package ua.com.fielden.platform.web.view.master.api.helpers;

/**
 * This is an interface to provide auto-commit functionality immediately after a property was added to an Entity Master definition.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAutoCommit<T> {
    
    /**
     * This API should be used to instruct a property editor to commit a value typed by a user automatically without a need to hit the Enter key or for the editor to lose focus.
     * Such auto-commit is triggered in about {@code millis} after the user stops typing.
     * <p>
     * The original intent for this functionality was to support barcoding, where values are not actually typed, but read by a barcode scanner.
     * 
     * @param millis -- a number of milliseconds to gauge when to trigger auto-committing of the editor value after the user stops typing.
     */
    T autoCommit(final int millis);
    
}