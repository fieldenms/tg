package ua.com.fielden.platform.cypher.exceptions;

/**
 * A runtime exception that indicates erroneous situation during a checksum computation.
 * 
 * @author TG Team
 *
 */
public class ChecksumException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ChecksumException(final String msg) {
        super(msg);
    }
    
    public ChecksumException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
