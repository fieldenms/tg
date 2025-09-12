package ua.com.fielden.platform.eql.stage1.sources.exceptions;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNodesGenerator;

/**
 * Thrown when an invalid yield matrix is encountered.
 * This applies to the processing of source queries in a union.
 *
 * @see YieldInfoNodesGenerator
 */
public final class InvalidYieldMatrixException extends EqlException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public InvalidYieldMatrixException(final String s) {
        super(s);
    }

    public InvalidYieldMatrixException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
