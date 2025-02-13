package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;

/**
 * Utilities for immutable {@linkplain Set sets}.
 * <p>
 * <b>Null as set member is not permitted</b>.
 *
 * @see ImmutableMapUtils
 */
public final class ImmutableSetUtils {

    /**
     * Returns an immutable set that is the result of {@code union(xs, Set.of(y))};
     *
     * @param xs  must not contain null elements
     * @param y  must not be null
     */
    public static <X extends Y, Y> Set<Y> insert(final Iterable</*@Nonnull*/ X> xs, final /*@Nonnull*/ Y y) {
        final var size = Iterables.size(xs);
        return size == 0
                ? ImmutableSet.of(y)
                : ImmutableSet.<Y>builderWithExpectedSize(size + 1)
                        .addAll(xs)
                        .add(y)
                        .build();
    }

    /**
     * Returns an immutable set that is a union of the given iterables.
     *
     * @param xs  must not contain null elements
     * @param ys  must not contain null elements
     */
    public static <X extends Y, Y> Set<Y> union(final Iterable</*@Nonnull*/ X> xs, final Iterable</*@Nonnull*/ Y> ys) {
        final int xsSize = Iterables.size(xs);
        final int ysSize;

        if (xsSize == 0) {
            return ImmutableSet.copyOf(ys);
        }
        else if ((ysSize = Iterables.size(ys)) == 0) {
            return ImmutableSet.copyOf(xs);
        }
        else {
            return ImmutableSet.<Y>builderWithExpectedSize(xsSize + ysSize)
                    .addAll(xs)
                    .addAll(ys)
                    .build();
        }
    }


    private ImmutableSetUtils() {}

}
