package ua.com.fielden.platform.streaming;

import ua.com.fielden.platform.utils.StreamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;

/**
 * A stream factory that produces a stream of groups that contain elements of the base stream.
 * The grouping of elements is performed sequentially according to the description of methods in this class.
 * 
 * @author TG Team
 */
public final class SequentialGroupingStream {

    public static final String ERR_NON_POSITIVE_GROUP_SIZE = "Group size must be greater than 0.";
    public static final String ERR_NON_POSITIVE_GROUP_SIZE_ESTIMATE = "Group size estimate should be a positive integer.";

    private SequentialGroupingStream() {}

    /**
     * Creates a new stream based on {@code baseStream} by grouping its elements into non-empty groups of the specified size.
     * The last group may be smaller if the number of elements in the base stream is not evenly divisible by the group size.
     * <p>
     * <b>Important: </b> <i>The base stream gets closed if the resultant stream is closed.</i>
     * <p>
     * <b>Note:</b> Prefer the use of method {@link StreamUtils#windowed(Stream, int)} as alternative.
     *
     * @param baseStream  a stream of data that needs to be grouped
     * @param groupSize  must be greater than 0
     *
     * @see StreamUtils#windowed(Stream, int)
     */
    public static <T> Stream<List<T>> stream(final Stream<T> baseStream, final int groupSize) {
        final var spliterator = new SequentialSizedGroupSpliterator<>(baseStream.spliterator(), groupSize);
        return StreamSupport.stream(spliterator, false)
                .onClose(baseStream::close);
    }

    /**
     * Creates a new stream based on {@code baseStream} by grouping its elements using the {@code grouping} predicate
     * and an optional group size estimate of the resultant stream (i.e., estimated number of groups).
     * Empty groups are excluded from the result.
     * <p>
     * <b>Important: </b> <i>The base stream gets closed if the resultant stream is closed.</i>
     *
     * @param grouping  given {@code (currentElement, groupSoFar)}, returns {@code true} if the group should be closed
     *                  ({@code currentElement} goes into the next group); otherwise, {@code currentElement} is added
     *                  to the group and the grouping continues.
     * @param  groupSizeEstimate  must be greater than 0
     *
     * @see StreamUtils#windowed(Stream, int)
     */
    public static <T> Stream<List<T>> stream(
            final Stream<T> baseStream,
            final BiPredicate<T, List<T>> grouping,
            final Optional<Integer> groupSizeEstimate)
    {
        final var spliterator = new SequentialGroupSpliterator<>(baseStream, grouping, groupSizeEstimate);
        return StreamSupport.stream(spliterator, false)
                            .onClose(baseStream::close);
    }

    /**
     * The same as {@link #stream(Stream, BiPredicate, Optional)}, but without a group size estimate.
     */
    public static <T> Stream<List<T>> stream(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping) {
        return stream(baseStream, grouping, empty());
    }

    private static final class SequentialGroupSpliterator<T> implements Spliterator<List<T>> {
        // Null should not be used to denote absence of value because elements of the stream may themselves be null.
        private static final Object NO_REMAINDER = new Object();

        private final Spliterator<T> baseSpliterator;
        private final BiPredicate<T, List<T>> grouping;
        private final int groupSizeEstimate;
        /** An element scheduled for the next group. */
        private Object remainder = NO_REMAINDER;

        public SequentialGroupSpliterator(final Stream<T> stream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
            this.baseSpliterator = stream.spliterator();
            this.grouping = grouping;
            this.groupSizeEstimate = groupSizeEstimate.orElse(25);
            if (this.groupSizeEstimate <= 0) {
                throw new IllegalArgumentException(ERR_NON_POSITIVE_GROUP_SIZE_ESTIMATE);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            final var group = populateGroup();

            if (group.isEmpty()) {
                return false;
            }
            else {
                action.accept(group);
                return true;
            }
        }

        private List<T> populateGroup() {
            // Empty groups are ignored, so we must continue until at least one element is accepted.
            // If there is a remainder, then we add it to the group and all the following elements that satisfy the predicate.
            // Otherwise, there are 2 scenarios:
            // 1. The next element satisfies the predicate - add it to the group and all the following elements that satisfy the predicate.
            // 2. Otherwise, the group remains empty, we record the element as the remainder and continue.

            final var group = new ArrayList<T>(groupSizeEstimate);
            boolean advanced = true;

            while (advanced && group.isEmpty()) {
                if (remainder != NO_REMAINDER) {
                    group.add((T) remainder);
                    remainder = NO_REMAINDER;
                }

                final var groupOpen = new AtomicBoolean(true);
                while (advanced && groupOpen.get()) {
                    advanced = baseSpliterator.tryAdvance(elt -> {
                        if (grouping.test(elt, group)) {
                            group.add(elt);
                        } else {
                            remainder = elt;
                            groupOpen.set(false);
                        }
                    });
                }
            }

            return group;
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            // Unsupported.
            return null;
        }

        @Override
        public long estimateSize() {
            final long baseSize = baseSpliterator.estimateSize();
            if (!baseSpliterator.hasCharacteristics(Spliterator.SIZED) && baseSize == Long.MAX_VALUE) {
                // Unknown size.
                return Long.MAX_VALUE;
            }
            else {
                return baseSize % groupSizeEstimate == 0
                       ? baseSize / groupSizeEstimate
                       : 1 + baseSize / groupSizeEstimate;
            }
        }

        @Override
        public int characteristics() {
            // Exclude SIZED & SUBSIZED due to "uninspectable" nature of the grouping predicate,
            // which cannot be used to reliably identify the expected size of the resulting stream.
            // Exclude SORTED because groups (instances of List) are not comparable.
            return baseSpliterator.characteristics()
                   & (~Spliterator.SIZED)
                   & (~Spliterator.SUBSIZED)
                   & (~Spliterator.SORTED);
        }

    }

    private static final class SequentialSizedGroupSpliterator<T> implements Spliterator<List<T>> {

        private final Spliterator<T> baseSpliterator;
        private final int groupSize;


        private SequentialSizedGroupSpliterator(final Spliterator<T> baseSpliterator, final int groupSize) {
            if (groupSize <= 0) {
                throw new IllegalArgumentException(ERR_NON_POSITIVE_GROUP_SIZE);
            }
            this.baseSpliterator = baseSpliterator;
            this.groupSize = groupSize;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super List<T>> action) {
            var group = new ArrayList<T>(groupSize);

            for (int i = 0; i < groupSize; i++) {
                final boolean advanced = baseSpliterator.tryAdvance(group::add);
                if (!advanced) {
                    break;
                }
            }

            if (!group.isEmpty()) {
                action.accept(group);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            // Unsupported.
            return null;
        }

        @Override
        public long estimateSize() {
            final long baseSize = baseSpliterator.estimateSize();
            if (!baseSpliterator.hasCharacteristics(Spliterator.SIZED) && baseSize == Long.MAX_VALUE) {
                // Unknown size.
                return Long.MAX_VALUE;
            }
            else {
                return baseSize % groupSize == 0
                       ? baseSize / groupSize
                       : 1 + baseSize / groupSize;
            }
        }

        @Override
        public int characteristics() {
            // Exclude SORTED because groups (instances of List) are not comparable.
            return baseSpliterator.characteristics()
                   & (~Spliterator.SORTED);
        }

    }

}
