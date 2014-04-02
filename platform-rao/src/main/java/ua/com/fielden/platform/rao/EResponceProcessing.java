package ua.com.fielden.platform.rao;

/**
 * An exception used to indicated errors that might occur during the processing of responses from the application server.
 * 
 * @author TG Team
 * 
 */
public class EResponceProcessing extends RuntimeException {

    public EResponceProcessing(final String message) {
        super(message);
    }

    public EResponceProcessing(final String message, final Throwable cause) {
        super(message, cause);
    }
}
