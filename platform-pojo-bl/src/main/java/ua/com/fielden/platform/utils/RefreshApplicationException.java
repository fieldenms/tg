package ua.com.fielden.platform.utils;

/**
 * The exception type that indicates that the server has been restarted and it is necessary to restart client application.
 * <p>
 * There are two cases:
 * 1) The client app registered entity types are not the same as in server (most likely centre's actions will fail).
 * 2) The server's 'previously Run centre manager' is not initialised (centre's pagination actions will fail).
 * 
 * @author TG Team
 *
 */
public class RefreshApplicationException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    
    public RefreshApplicationException() {
        super("The server has been restarted. Please refresh the application.", new Exception("The server has been restarted. Please refresh the application."));
    }
}