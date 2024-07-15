package ua.com.fielden.platform.error;

/**
 * This class represents Informative.
 *
 * @author TG Team
 *
 */
public class Informative extends Result {

    private static final long serialVersionUID = 1L;

    /**
     * Mainly used for serialisation.
     */
    protected Informative() {

    }

    public Informative(final String message) {
        this(null, message);
    }

    public Informative(final Object instance, final String message) {
        super(instance, message);
    }
}
