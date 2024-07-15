package ua.com.fielden.platform.dir.exceptions;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;

/**
 * A runtime exception that indicates erroneous situation pertaining to LDAP integration. 
 * 
 * @author TG Team
 *
 */
public class LdapException extends AbstractPlatformRuntimeException {
    private static final long serialVersionUID = 1L;

    public LdapException(final String msg) {
        super(msg);
    }

    public LdapException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}