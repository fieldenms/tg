package ua.com.fielden.platform.dao;

/**
 * Runtime exceptional situation describing incorrect use of context decomposition API inside producer implementation.
 * 
 * @author TG Team
 *
 */
public class EntityProducingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityProducingException(final String msg) {
        super(msg);
    }
    
    public EntityProducingException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}