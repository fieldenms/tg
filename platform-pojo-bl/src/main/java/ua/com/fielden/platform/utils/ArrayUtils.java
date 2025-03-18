package ua.com.fielden.platform.utils;

import java.lang.reflect.Array;
import java.util.stream.Stream;

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

    private ArrayUtils() {}

}
