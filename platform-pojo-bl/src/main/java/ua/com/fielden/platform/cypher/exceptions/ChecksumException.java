package ua.com.fielden.platform.cypher.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates an erroneous situation during a checksum computation.
 * 
 * @author TG Team
 *
 */
public class ChecksumException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public ChecksumException(final String msg) {
        super(msg);
    }

    public ChecksumException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}