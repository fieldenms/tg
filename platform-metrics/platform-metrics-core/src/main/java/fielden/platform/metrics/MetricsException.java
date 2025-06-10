package fielden.platform.metrics;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class MetricsException extends AbstractPlatformRuntimeException {

    public MetricsException(final String message) {
        super(message);
    }

    public MetricsException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
