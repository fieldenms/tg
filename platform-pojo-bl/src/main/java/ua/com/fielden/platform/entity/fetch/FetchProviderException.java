package ua.com.fielden.platform.entity.fetch;

/**
 * A runtime exception that indicates erroneous situation during construction of fetch providers. 
 * 
 * @author TG Team
 *
 */
public class FetchProviderException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public FetchProviderException(final String msg) {
        super(msg);
    }
    
    public FetchProviderException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}