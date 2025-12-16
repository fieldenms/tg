package ua.com.fielden.platform.error;

/// A kind of [Result] that represents an informative message.
///
/// Stack trace is always disabled for informative results.
///
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
        super(instance, message, null, false);
    }

    @Override
    public Informative copyWith(final Object anotherInstance) {
        return new Informative(anotherInstance, getMessage());
    }

}
