package ua.com.fielden.platform.web.centre.exceptions;

/**
 * A runtime exception that indicates erroneous situation in an Entity Centre configuration.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigurationException  extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityCentreConfigurationException(final String msg) {
        super(msg);
    }

    public EntityCentreConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
