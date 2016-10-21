package ua.com.fielden.platform.web.view.master.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to an entity master configuration. 
 * 
 * @author TG Team
 *
 */
public class EntityMasterConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EntityMasterConfigurationException(final String msg) {
        super(msg);
    }
    
    public EntityMasterConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
