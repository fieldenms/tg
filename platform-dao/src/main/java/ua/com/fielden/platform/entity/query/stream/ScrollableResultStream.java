package ua.com.fielden.platform.entity.query.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.hibernate.ScrollableResults;

/**
 * This is a factory class for the most primitive stream, which enables streaming of data from {@link ScrollableResults}.
 * 
 * @author TG Team
 *
 */
public class ScrollableResultStream {
    
    private ScrollableResultStream() {}
    
    public static Stream<Object[]> streamOf(final ScrollableResults results) {
        final ScrollableResultSpliterator spliterator = new ScrollableResultSpliterator(results);
        // create a new stream and make sure the scrollable result set gets closed upon stream closing.
        return StreamSupport
                .stream(spliterator, false)
                .onClose(results::close);
    }

    /**
     * A spliterator for streaming {@link ScrollableResults}.
     *
     */
    private static class ScrollableResultSpliterator implements Spliterator<Object[]> {

        private final ScrollableResults results;

        private ScrollableResultSpliterator(final ScrollableResults results) {
            this.results = results;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Object[]> action) {
            final boolean advanced = results.next();
            
            if (!advanced) {
                return false;
            } else {
                action.accept(results.get());
            }
            
            return true;
        }

        @Override
        public Spliterator<Object[]> trySplit() {
            // no parallel processing support is currently envisaged...
            return null;
        }

        @Override
        public long estimateSize() {
            // the requirement of this method is to be able to compute the size before stream traversal has started
            // in our case this means that we would need to execute a query to compute the count even before someone tried to access the data
            // so, for now let's consider the size too expensive to compute by returning Long.MAX_VALUE.
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED & NONNULL & IMMUTABLE;
        }

    }

}
