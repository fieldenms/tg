package ua.com.fielden.platform.serialisation.exceptions;
/**
 * A runtime exception to indicate TG specific serialisation related exception.
 * 
 * @author TG Team
 *
 */
public class SerialisationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public SerialisationException(final String msg) {
        super(msg);
    }
    
    public SerialisationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}