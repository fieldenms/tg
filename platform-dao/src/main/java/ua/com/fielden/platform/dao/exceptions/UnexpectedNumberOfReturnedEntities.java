package ua.com.fielden.platform.dao.exceptions;

/**
 * Runtime exception that should be thrown within entity companion implementation in cases where an unexpected number of instances is returned.
 * 
 * @author TG Team
 *
 */
public class UnexpectedNumberOfReturnedEntities extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnexpectedNumberOfReturnedEntities(final String msg) {
        super(msg);
    }
}
