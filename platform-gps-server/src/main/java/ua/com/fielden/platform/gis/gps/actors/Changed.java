package ua.com.fielden.platform.gis.gps.actors;

public class Changed<T> {
    private final T value;

    public Changed(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
