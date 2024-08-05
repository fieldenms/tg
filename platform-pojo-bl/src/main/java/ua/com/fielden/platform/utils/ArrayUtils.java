package ua.com.fielden.platform.utils;

public final class ArrayUtils {

    /**
     * Returns the last element of an array. Throws if array is empty.
     */
    public static <X> X getLast(final X[] xs) {
        return xs[xs.length - 1];
    }

    private ArrayUtils() {}

}
