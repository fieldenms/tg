package ua.com.fielden.platform.utils;

import java.util.Objects;

public final class ArrayUtils {

    /**
     * Returns the last element of an array.
     * Throws if the array is empty.
     */
    public static <X> X getLast(final X[] xs) {
        return xs[xs.length - 1];
    }

    /**
     * Returns {@code true} if the specified array contains the specified item.
     * Array elements and the item are compared using {@link Objects#equals(Object, Object)}.
     */
    public static <X> boolean contains(final X[] xs, final X item) {
        for (var x : xs) {
            if (Objects.equals(x, item)) {
                return true;
            }
        }
        return false;
    }

    private ArrayUtils() {}

}
