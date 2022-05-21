package ua.com.fielden.platform.ioc.exceptions;

/**
 * A runtime exception that indicates incorrect or unresolved dependence injection that pertains to application or environment parameters.
 * 
 * @author TG Team
 *
 */
public class MissingParameterDependencyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public MissingParameterDependencyException(final String msg) {
        super(msg);
    }
    
    public MissingParameterDependencyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}