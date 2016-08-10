package ua.com.fielden.platform.web.centre.api.context.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to an instance of {@link CentreContextConfig}. 
 * 
 * @author TG Team
 *
 */
public class CentreContextConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public CentreContextConfigException(final String msg) {
        super(msg);
    }
    
    public CentreContextConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
