package ua.com.fielden.platform.web.utils;

/**
 * Runtime exceptional situation occuring in {@link EntityRestorationUtils}.
 * 
 * @author TG Team
 *
 */
public class EntityRestorationUtilsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityRestorationUtilsException(final String msg) {
        super(msg);
    }
    
    public EntityRestorationUtilsException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}