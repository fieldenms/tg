package ua.com.fielden.platform.file_reports.exceptions;

/**
 * A runtime exception to report export errors.
 * 
 * @author TG Team
 *
 */
public class ExportException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public ExportException(final String msg) {
        super(msg);
    }
    
    public ExportException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}