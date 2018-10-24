package ua.com.fielden.platform.ui.config.controller;

/**
 * A runtime exception to indicate exceptional situations in {@link CentreUpdater}.
 * 
 * @author TG Team
 *
 */
public class EntityCentreConfigDaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityCentreConfigDaoException(final String msg) {
        super(msg);
    }
    
    public EntityCentreConfigDaoException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
}