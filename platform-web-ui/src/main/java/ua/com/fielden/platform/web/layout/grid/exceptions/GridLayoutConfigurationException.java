package ua.com.fielden.platform.web.layout.grid.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/// A runtime exception that indicates an erroneous grid layout configuration — e.g. a non-positive span or track repeat, or a cell placed outside (or overlapping within) the declared grid.
///
public class GridLayoutConfigurationException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public GridLayoutConfigurationException(final String msg) {
        super(msg);
    }

    public GridLayoutConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}