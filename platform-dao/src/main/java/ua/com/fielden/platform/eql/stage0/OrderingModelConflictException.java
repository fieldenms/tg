package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class OrderingModelConflictException extends EqlException {

    private static final long serialVersionUID = 1L;

    public OrderingModelConflictException(final String msg) {
        super(msg);
    }

    public OrderingModelConflictException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
