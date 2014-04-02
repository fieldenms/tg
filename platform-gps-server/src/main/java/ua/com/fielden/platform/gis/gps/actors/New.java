package ua.com.fielden.platform.gis.gps.actors;

public class New<T> {
    private final T value;

    public New(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
