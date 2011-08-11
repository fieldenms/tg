package ua.com.fielden.platform.error;

/**
 * This class represents warning.
 *
 * @author TG Team
 *
 */
public class Warning extends Result {

    private static final long serialVersionUID = 1L;

    /**
     * Mainly used for serialisation.
     */
    protected Warning() {

    }

    public Warning(final String message) {
	this(null, message);
    }

    public Warning(final Object instance, final String message) {
	super(instance, message);
    }
}
