package ua.com.fielden.platform.processors.metamodel.exceptions;

public class AnomalousStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AnomalousStateException(final String message) {
        super(message);
    }
}
