package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    /**
     * Returns an iterator of distinct elements drawn from the given iterator, where an element is uniquely classified by the given function.
     * <p>
     * The return type of the classifier must have proper implementations of {@link Object#hashCode()} and {@link Object#equals(Object)}.
     * <p>
     * The order of elements is preserved.
     */
    public static <T, U> Iterator<T> distinctIterator(final Iterator<T> iterator, final Function<? super T, U> classifier) {
        return new Iterator<>() {
            static final Object NIL = new Object(); // means "currently there is no next element"

            final LinkedHashSet<U> uniqs = new LinkedHashSet<>();
            Object next = NIL;

            @Override
            public boolean hasNext() {
                if (next != NIL) {
                    return true;
                }

                while (iterator.hasNext()) {
                    final T theirNext = iterator.next();
                    final U u = classifier.apply(theirNext);
                    if (!uniqs.contains(u)) {
                        uniqs.add(u);
                        next = theirNext;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final T theNext = (T) next;
                next = NIL;
                return theNext;
            }
        };
    }

    private IteratorUtils() {}

}
