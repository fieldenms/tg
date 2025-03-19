package ua.com.fielden.platform.utils;

import java.lang.reflect.Array;
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
     * Creates a new array with `firstElement` prepended to `array`.
     */
    public static <T> T[] prepend(final T firstElement, final T[] array) {
        @SuppressWarnings("unchecked")
        final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        newArray[0] = firstElement;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
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
