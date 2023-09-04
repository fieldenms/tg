package ua.com.fielden.platform.web.view.master.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation in an Entity Master configuration.
 *
 * @author TG Team
 *
 */
public class EntityMasterConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityMasterConfigurationException(final String msg) {
        super(msg);
    }

    public EntityMasterConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
