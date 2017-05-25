package ua.com.fielden.platform.eql.dbschema.exceptions;
/**
 * A runtime exception to indicate TG specific DB schema related exception.
 * 
 * @author TG Team
 *
 */
public class DbSchemaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public DbSchemaException(final String msg) {
        super(msg);
    }
    
    public DbSchemaException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}