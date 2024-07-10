package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.function.BiConsumer;

public final class IteratorUtils {

    /**
     * Performs an action for each pair of elements from given sources. Terminates upon reaching the end of the shorter source.
     */
    public static <X, Y> void zipDo(final Iterator<X> xs, final Iterator<Y> ys,
                                    final BiConsumer<? super X, ? super Y> action) {
        while (xs.hasNext() && ys.hasNext()) {
            action.accept(xs.next(), ys.next());
        }
    }

    private IteratorUtils() {}

}
