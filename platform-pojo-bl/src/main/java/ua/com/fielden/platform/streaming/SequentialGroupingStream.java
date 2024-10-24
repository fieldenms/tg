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
        return StreamSupport.stream(new SequentialGroupSplitterator<>(baseStream, grouping, groupSizeEstimate, false), false)
                            .onClose(baseStream::close);
    }

    /**
     * The same as {@link #stream(Stream, BiPredicate, Optional)}, but without a group size estimate.
     */
    public static <T> Stream<List<T>> stream(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping) {
        return stream(baseStream, grouping, empty());
    }

    /**
     * The same as {@link #stream(Stream, BiPredicate, Optional)}, but with automatic closing of the base stream upon processing of its last element by a terminal operation, invoked on the resultant stream.
     * Closing of the resultant stream still leads to closing of the base stream if it was not closed sooner due to the consumption by a terminal operation.
     * <p>
     * For example, if {@link Stream#forEach(Consumer)} or {@link Stream#collect(Collector)} is invoked on the resultant stream, the based stream will get closed after the processing of its last element.
     * <p>
     * <b>Note: </b>
     * <i>
     *     The main intent for this factory method is to be used in application to the base streams with some underlying resources,
     *     which must be closed even in situations where try-with-resources or simply explicit closing of the resultant stream was not employed
     *     (either by mistake or due to some complex nested composition of the stream processing logic).
     * </i>
     */
    public static <T> Stream<List<T>> streamClosedOnTermination(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
        return StreamSupport.stream(new SequentialGroupSplitterator<>(baseStream, grouping, groupSizeEstimate, true), false)
                            .onClose(baseStream::close);
    }

    /**
     * The same as {@link #streamClosedOnTermination(Stream, BiPredicate, Optional)}, but without a group size estimate.
     */
    public static <T> Stream<List<T>> streamClosedOnTermination(final Stream<T> baseStream, final BiPredicate<T, List<T>> grouping) {
        return streamClosedOnTermination(baseStream, grouping, empty());
    }

    private static class SequentialGroupSplitterator<T> implements Spliterator<List<T>> {
        private final Stream<T> baseStream;
        private final Spliterator<T> baseSpliterator;
        private final BiPredicate<T, List<T>> grouping;
        private final int groupSizeEstimate;
        private final boolean closeBaseStreamIfCannotAdvance;
        private T remainder;

        public SequentialGroupSplitterator(final Stream<T> stream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate, final boolean closeBaseStreamIfCannotAdvance) {
            this.baseStream = stream;
            this.baseSpliterator = stream.spliterator();
            this.grouping = grouping;
            this.groupSizeEstimate = groupSizeEstimate.orElse(25);
            this.closeBaseStreamIfCannotAdvance = closeBaseStreamIfCannotAdvance;
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

            final var remainingElementsExist = advanced || remainder != null;
            if (closeBaseStreamIfCannotAdvance && !remainingElementsExist) {
                this.baseStream.close();
            }

            return remainingElementsExist;
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
