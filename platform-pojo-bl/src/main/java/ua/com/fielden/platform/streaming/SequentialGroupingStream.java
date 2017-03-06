package ua.com.fielden.platform.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A stream factory that produces a stream of groups that are made of the element from the base stream. 
 * The grouping of elements is performed sequentially using the provided grouping function of type {@link BiPredicate}.
 * 
 * @author TG Team
 *
 */
public class SequentialGroupingStream {

    private SequentialGroupingStream() {}
    
    public static <T> Stream<List<T>> stream(final Stream<T> stream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
        return StreamSupport.stream(new SequentialGroupSplitterator<>(stream, grouping, groupSizeEstimate), false);
    }

    public static <T> Stream<List<T>> stream(final Stream<T> stream, final BiPredicate<T, List<T>> grouping) {
        return StreamSupport.stream(new SequentialGroupSplitterator<>(stream, grouping, Optional.empty()), false);
    }

    private static class SequentialGroupSplitterator<T> implements Spliterator<List<T>> {
        private final Spliterator<T> baseSpliterator;
        private final BiPredicate<T, List<T>> grouping;
        private final Optional<Integer> groupSizeEstimate;
        private T remainder;

        public SequentialGroupSplitterator(final Stream<T> stream, final BiPredicate<T, List<T>> grouping, final Optional<Integer> groupSizeEstimate) {
            this.baseSpliterator = stream.spliterator();
            this.grouping = grouping;
            this.groupSizeEstimate = groupSizeEstimate;
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            final List<T> group = new ArrayList<>(groupSizeEstimate.orElse(25));
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

            return advanced || remainder != null;
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            // no support for parallel processing
            return null;
        }

        @Override
        public long estimateSize() {
            return baseSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return baseSpliterator.characteristics();
        }

    }
}
