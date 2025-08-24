package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

/**
 * Utilities for immutable {@linkplain List lists}.
 * <p>
 * <b>Null as list element is not permitted</b>.
 *
 * @see ImmutableMapUtils
 * @see ImmutableSetUtils
 */
public final class ImmutableListUtils {

    /// Returns an immutable list whose head is `y` and whose tail is `xs`.
    ///
    public static <X extends Y, Y> List<Y> prepend(final Y y, final Iterable</*@Nonnull*/ X> xs) {
        final var size = Iterables.size(xs);

        if (size == 0) {
            return ImmutableList.of(y);
        }
        else {
            return ImmutableList.<Y>builderWithExpectedSize(size + 1)
                    .add(y)
                    .addAll(xs)
                    .build();
        }
    }


    private ImmutableListUtils() {}

}
