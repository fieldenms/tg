package ua.com.fielden.platform.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;

/**
 * A stream factory that produces a stream of groups that are made of the element from the base stream. 
 * The grouping of elements is performed sequentially using the provided grouping function of type {@link BiPredicate}.
 * 
 * @author TG Team
 *
 */
public class SequentialGroupingStream {

    private SequentialGroupingStream() {}

    /**
     * Creates a new stream based on {@code baseStream} by grouping its elements using the {@code grouping} predicate
     * and an optional group size estimate to assist with estimating the size of the resultant stream.
     * <p>
     * <b>Important: </b> <i>The base stream gets closed if the resultant stream is closed.</i>
     */
    public static <T> Stream<List<T>> stream(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
        final var spliterator = new SequentialGroupSplitterator<>(baseStream, grouping, groupSizeEstimate);
        return StreamSupport.stream(spliterator, false)
                            .onClose(baseStream::close);
    }

    /**
     * The same as {@link #stream(Stream, BiPredicate, Optional)}, but without a group size estimate.
     */
    public static <T> Stream<List<T>> stream(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping) {
        return stream(baseStream, grouping, empty());
    }

    private static class SequentialGroupSplitterator<T> implements Spliterator<List<T>> {
        private final Spliterator<T> baseSpliterator;
        private final BiPredicate<T, List<T>> grouping;
        private final int groupSizeEstimate;
        private T remainder;

        public SequentialGroupSplitterator(final Stream<T> stream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
            this.baseSpliterator = stream.spliterator();
            this.grouping = grouping;
            this.groupSizeEstimate = groupSizeEstimate.orElse(25);
            if (this.groupSizeEstimate <= 0) {
                throw new IllegalArgumentException("Group size estimate should be a positive integer.");
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            final List<T> group = new ArrayList<>(groupSizeEstimate);
            if (remainder != null) {
                group.add(remainder);
                remainder = null;
            }

            final AtomicBoolean grew = new AtomicBoolean(false);
            boolean advanced;
            while (advanced = baseSpliterator.tryAdvance(el -> {
                if (grouping.test(el, group)) {
                    group.add(el);
                    grew.set(true);
                } else {
                    remainder = el;
                    grew.set(false);
                }
            }) && grew.get());

            // an empty group gets ignored
            if (!group.isEmpty()) {
                action.accept(group);
            }

            final var couldAdvanceOrRemainingElementsExist = advanced || remainder != null;
            return couldAdvanceOrRemainingElementsExist;
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            // no support for parallel processing
            return null;
        }

        @Override
        public long estimateSize() {
            return 1 + baseSpliterator.estimateSize() / groupSizeEstimate;
        }

        @Override
        public int characteristics() {
            return baseSpliterator.characteristics();
        }

    }
}
