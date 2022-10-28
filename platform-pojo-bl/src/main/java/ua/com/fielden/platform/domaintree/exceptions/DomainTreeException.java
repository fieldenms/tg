package ua.com.fielden.platform.domaintree.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to some operations within or construction of entity domain trees. 
 * 
 * @author TG Team
 *
 */
public class DomainTreeException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public DomainTreeException(final String msg) {
        super(msg);
    }

    public DomainTreeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}