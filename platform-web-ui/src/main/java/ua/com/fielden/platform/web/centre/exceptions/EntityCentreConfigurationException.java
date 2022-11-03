package ua.com.fielden.platform.web.centre.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation in an Entity Centre configuration.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigurationException  extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityCentreConfigurationException(final String msg) {
        super(msg);
    }

    public EntityCentreConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}