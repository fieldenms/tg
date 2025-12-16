package ua.com.fielden.platform.error;

/// A kind of [Result] that represents a warning.
///
/// Stack trace is always disabled for warnings.
///
public class Warning extends Result {

    private static final long serialVersionUID = 1L;

    /// Mainly used for serialisation.
    ///
    protected Warning() {
        this(null, null);
    }

    public Warning(final String message) {
        this(null, message);
    }

    public Warning(final Object instance, final String message) {
        super(instance, message, null, false);
    }

    @Override
    public Warning copyWith(final Object anotherInstance) {
        return new Warning(anotherInstance, getMessage());
    }

}
