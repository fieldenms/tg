package ua.com.fielden.platform.ioc.exceptions;

import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;

import java.util.Properties;

/**
 * A runtime exception that indicates incorrect or unresolved dependence injection that pertains to application or environment parameters.
 * 
 * @author TG Team
 *
 */
public class MissingParameterDependencyException extends ApplicationConfigurationException {
    private static final long serialVersionUID = 1L;

    public MissingParameterDependencyException(final String msg) {
        super(msg);
    }

    public MissingParameterDependencyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static String requireNonEmpty(final Properties properties, final String property) {
        final var value = properties.getProperty(property);
        if (value == null) {
            throw new MissingParameterDependencyException("Required application property [%s] is missing.".formatted(property));
        }
        if (value.isEmpty()) {
            throw new MissingParameterDependencyException("Required application property [%s] is empty.".formatted(property));
        }
        return value;
    }

}
