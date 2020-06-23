package ua.com.fielden.platform.web.view.master.exceptions;

/**
 * The Exception that is thrown because of incorrect entity centre configuration.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfigurationException  extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityCentreConfigurationException(final String msg) {
        super(msg);
    }

    public EntityCentreConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
