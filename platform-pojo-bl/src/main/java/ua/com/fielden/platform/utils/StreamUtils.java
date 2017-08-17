package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ua.com.fielden.platform.types.tuples.T2;

/**
 * A set of convenient APIs for working with {@link Stream}.
 * 
 * @author TG Team
 *
 */
public class StreamUtils {
    private StreamUtils() {}
    
    /**
     * Splits a stream into a <code>head</code> and <code>tail</code>. 
     * The head is optional as the passed in stream could be empty.
     * The tail is a stream, which could be empty if the input stream is empty or contains only a single element.
     * 
     * @param stream
     * @return
     */
    public static <T> T2<Optional<T>, Stream<T>> head_and_tail(final Stream<T> stream) {
        final Iterator<T> iter = stream.iterator();
        final Optional<T> head = iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
        
        final Iterable<T> iterable = () -> iter;
        Stream<T> tail = StreamSupport.stream(iterable.spliterator(), false);
        
        return T2.t2(head, tail);
    }

}
