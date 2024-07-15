package ua.com.fielden.platform.migration;

/**
 * A runtime exception that indicates some error that occurred as part of the data migration framework logic.
 * 
 * @author TG Team
 *
 */
public class DataMigrationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public DataMigrationException(final String msg) {
        super(msg);
    }
    
    public DataMigrationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}