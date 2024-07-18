package ua.com.fielden.platform.meta.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

public class DomainMetadataGenerationException extends AbstractPlatformRuntimeException {

    public DomainMetadataGenerationException(final String msg) {
        super(msg);
    }

    public DomainMetadataGenerationException(final Throwable cause) {
        super(cause);
    }

    public DomainMetadataGenerationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
