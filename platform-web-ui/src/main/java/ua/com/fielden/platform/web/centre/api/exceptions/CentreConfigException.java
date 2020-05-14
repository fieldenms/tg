package ua.com.fielden.platform.web.centre.api.exceptions;

import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * A runtime exception that indicates erroneous situation pertaining to an instance of {@link EntityCentreConfig}.
 *
 * @author TG Team
 *
 */
public class CentreConfigException extends RuntimeException {

private static final long serialVersionUID = 1L;

    public CentreConfigException(final String msg) {
        super(msg);
    }

    public CentreConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
