package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import java.util.Set;
import java.util.SortedSet;

/// Utilities for immutable {@linkplain Set sets}.
///
/// **Null as set member is not permitted**.
///
/// @see ImmutableMapUtils
/// @see ImmutableListUtils
///
public final class ImmutableSetUtils {

    /// Returns an immutable set that is the result of `union(xs, Set.of(y))`.
    ///
    /// @param xs  must not contain null elements
    /// @param y  must not be null
    ///
    public static <X extends Y, Y> Set<Y> insert(final Iterable</*@Nonnull*/ X> xs, final /*@Nonnull*/ Y y) {
        final var size = Iterables.size(xs);

        if (size == 0) {
            return ImmutableSet.of(y);
        }
        else if (xs instanceof ImmutableSet<X> set && set.contains(y)) {
            return (Set<Y>) set;
        }
        else {
            return ImmutableSet.<Y>builderWithExpectedSize(size + 1)
                    .addAll(xs)
                    .add(y)
                    .build();
        }
    }

    /// Returns an immutable set that is the result of `union(xs, Set.of(y))`.
    ///
    /// @param xs  must not contain null elements
    /// @param y  must not be null
    ///
    public static <X extends Y, Y extends Comparable<Y>> SortedSet<Y> insert(final SortedSet</*@Nonnull*/ X> xs, final /*@Nonnull*/ Y y) {
        if (xs.isEmpty()) {
            return ImmutableSortedSet.of(y);
        }
        else if (xs instanceof ImmutableSortedSet<X> set && set.contains(y)) {
            return (SortedSet<Y>) set;
        }
        else {
            return ImmutableSortedSet.<Y>naturalOrder()
                    .addAll(xs)
                    .add(y)
                    .build();
        }
    }

    /// Returns an immutable set that is a union of the given iterables.
    ///
    /// @param xs  must not contain null elements
    /// @param ys  must not contain null elements
    ///
    public static <X extends Y, Y> Set<Y> union(final Iterable</*@Nonnull*/ X> xs, final Iterable</*@Nonnull*/ Y> ys) {
        if (xs == ys && ys instanceof ImmutableSet<Y> set) {
            return set;
        }

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
