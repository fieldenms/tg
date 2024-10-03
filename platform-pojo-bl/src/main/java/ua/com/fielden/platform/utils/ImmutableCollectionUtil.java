package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Set;

/**
 * Utilities for immutable collections.
 * <p>
 * <b>Null elements are not permitted</b>.
 *
 * @see ImmutableMapUtils
 * @see CollectionUtil
 * @author TG Team
 */
public final class ImmutableCollectionUtil {

    /**
     * Returns an immutable set that contains all elements from set {@code xs} and element {@code y}.
     *
     * @param xs  must not contain null elements
     * @param y  must not be null
     */
    public static <X extends Y, Y> Set<Y> setAppend(final Set</*@Nonnull*/ X> xs, final /*@Nonnull*/ Y y) {
        return xs.isEmpty()
                ? ImmutableSet.of(y)
                : ImmutableSet.<Y>builderWithExpectedSize(xs.size() + 1)
                        .addAll(xs)
                        .add(y)
                        .build();
    }

    /**
     * Returns an immutable set that is the concatenation of the given sets.
     *
     * @param xs  must not contain null elements
     * @param ys  must not contain null elements
     */
    public static <X extends Y, Y> Set<Y> concatSet(final Set</*@Nonnull*/ X> xs, final Iterable</*@Nonnull*/ Y> ys) {
        if (xs.isEmpty()) {
            return ImmutableSet.copyOf(ys);
        }
        else if (Iterables.isEmpty(ys)) {
            return ImmutableSet.copyOf(xs);
        }
        else {
            return ImmutableSet.<Y>builderWithExpectedSize(xs.size() + sizeOrElse(ys, 0))
                    .addAll(xs)
                    .addAll(ys)
                    .build();
        }
    }

    private static int sizeOrElse(final Iterable<?> iterable, final int alternative) {
        return iterable instanceof Collection<?> c ? c.size() : alternative;
    }

    private ImmutableCollectionUtil() {}

}
