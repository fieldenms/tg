package ua.com.fielden.platform.mail.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to email composition or sending. 
 * 
 * @author TG Team
 *
 */
public class EmailException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;
    
    public EmailException(final String msg) {
        super(msg);
    }
    
    public EmailException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}