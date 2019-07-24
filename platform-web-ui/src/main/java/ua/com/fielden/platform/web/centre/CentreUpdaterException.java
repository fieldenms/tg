package ua.com.fielden.platform.web.centre;

/**
 * A runtime exception to indicate exceptional situations in {@link CentreUpdater}.
 * 
 * @author TG Team
 *
 */
public class CentreUpdaterException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public CentreUpdaterException(final String msg) {
        super(msg);
    }
    
    public CentreUpdaterException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}