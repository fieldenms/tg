package ua.com.fielden.platform.web.utils;

/**
 * Runtime exceptional situation occuring in {@link EntityResourceUtils}.
 * 
 * @author TG Team
 *
 */
public class EntityResourceUtilsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityResourceUtilsException(final String msg) {
        super(msg);
    }
    
    public EntityResourceUtilsException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}