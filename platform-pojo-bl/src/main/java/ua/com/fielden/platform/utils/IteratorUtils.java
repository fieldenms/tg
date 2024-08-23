package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

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

    /**
     * Executes an action for the next {@code n} elements of an iterator.
     * If the iterator does not have enough elements, this method fails.
     */
    public static <X> void forNextN(final Iterator<X> iterator, final int n, final Consumer<? super X> action) {
        for (int i = 0; i < n; i++) {
            action.accept(iterator.next());
        }
    }

    /**
     * Skips the next {@code n} elements of an iterator.
     * If the iterator does not have enough elements, this method fails.
     */
    public static <X> void skipN(final Iterator<X> iterator, final int n) {
        for (int i = 0; i < n; i++) {
            iterator.next();
        }
    }

    /**
     * Executes {@code butLastAction} for each remaining element of an iterator except for the last, for which {@code lastAction}
     * is executed.
     *
     * @param butLastAction  action to execute for each remaining element except for the last
     * @param lastAction  action to execute for the last element
     */
    public static <X> void forEachRemainingAndLast(final Iterator<X> iterator,
                                                   final Consumer<? super X> butLastAction,
                                                   final Consumer<? super X> lastAction)
    {
        if (!iterator.hasNext()) {
            return;
        }

        var next = iterator.next();
        while (iterator.hasNext()) {
            butLastAction.accept(next);
            next = iterator.next();
        }

        lastAction.accept(next);
    }

    /**
     * Executes {@code action} for each element of an iterator until an element that doesn't satisfy a predicate is encountered,
     * at which point {@code lastAction} is executed for it.
     *
     * @param action  action to execute for each element before the first one that doesn't satisfy the predicate
     * @param lastAction  action to execute for the first element that doesn't satisfy the predicate
     */
    public static <X> void doWhile(final Iterator<X> iterator, final Predicate<? super X> predicate,
                                   final Consumer<? super X> action, final Consumer<? super X> lastAction)
    {
        while (iterator.hasNext()) {
            final var next = iterator.next();
            if (predicate.test(next)) {
                action.accept(next);
            } else {
                lastAction.accept(next);
                break;
            }
        }
    }

    private IteratorUtils() {}

}
