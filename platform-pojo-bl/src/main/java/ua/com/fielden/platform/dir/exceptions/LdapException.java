package ua.com.fielden.platform.dir.exceptions;

/**
 * A runtime exception that indicates erroneous situation pertaining to LDAP integration. 
 * 
 * @author TG Team
 *
 */
public class LdapException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public LdapException(final String msg) {
        super(msg);
    }
    
    public LdapException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
