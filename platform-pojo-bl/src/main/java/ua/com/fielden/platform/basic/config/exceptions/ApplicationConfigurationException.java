package ua.com.fielden.platform.basic.config.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * Runtime exception that should be thrown to indicate some kind of application misconfiguration.
 * For example, this could be some IoC module misconfiguration or something to do with application properties.
 * 
 * @author TG Team
 *
 */
public class ApplicationConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public ApplicationConfigurationException(final String msg) {
        super(msg);
    }

    public ApplicationConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}