package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;

public final class TopologicalSortException extends AbstractPlatformCheckedException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public TopologicalSortException(final String s) {
        super(s);
    }

    public TopologicalSortException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
