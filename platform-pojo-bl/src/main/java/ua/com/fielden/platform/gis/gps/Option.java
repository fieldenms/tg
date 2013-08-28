package ua.com.fielden.platform.gis.gps;

/**
 * Wrapper class to avoid null handling.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class Option<T> {
    private final T value;

    public Option(final T value) {
	this.value = value;
    }

    public boolean hasValue() {
	return value != null;
    }

    public T value() {
	return value;
    }

}
