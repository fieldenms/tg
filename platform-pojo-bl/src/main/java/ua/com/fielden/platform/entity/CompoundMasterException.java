package ua.com.fielden.platform.entity;

/**
 * Runtime exception that should be thrown when illegal situations occur during compound master lifecycle.
 * 
 * @author TG Team
 *
 */
public class CompoundMasterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompoundMasterException(final String msg) {
        super(msg);
    }
    
    public CompoundMasterException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}