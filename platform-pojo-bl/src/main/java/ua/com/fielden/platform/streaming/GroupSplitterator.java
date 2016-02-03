package ua.com.fielden.platform.streaming;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GroupSplitterator<T> implements Spliterator<T> {
    
    private final Spliterator<T> baseSpliterator;
    private final GroupProcessor<T> gp;
    
    public static <T> Stream<T> stream(final Stream<T> stream, final GroupProcessor<T> gp) {
        return StreamSupport.stream(new GroupSplitterator<T>(stream, gp), false);
    }
    
    public GroupSplitterator(final Stream<T> stream, final GroupProcessor<T> gp) {
        this.baseSpliterator = stream.spliterator();
        this.gp = gp;
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        final boolean hasAdvanced = baseSpliterator.tryAdvance(action);
        if (!hasAdvanced) {
            gp.completeProcessing();
        }
        return hasAdvanced;
    }

    @Override
    public Spliterator<T> trySplit() {
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
