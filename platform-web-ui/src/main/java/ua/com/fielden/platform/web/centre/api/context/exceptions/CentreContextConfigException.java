package ua.com.fielden.platform.web.centre.api.context.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

/**
 * A runtime exception that indicates erroneous situation pertaining to an instance of {@link CentreContextConfig}. 
 * 
 * @author TG Team
 *
 */
public class CentreContextConfigException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public CentreContextConfigException(final String msg) {
        super(msg);
    }

    public CentreContextConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}