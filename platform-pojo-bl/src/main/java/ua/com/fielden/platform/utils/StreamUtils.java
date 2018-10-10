package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
    public static final String ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL = "First stream element cannot be null.";

    private StreamUtils() {
    }

    /**
     * Creates a new stream from the provided values.
     * 
     * @param first
     * @param rest
     * @return
     */
    public static <T> Stream<T> of(final T first, final T... rest) {
        if (first == null) {
            throw new NullPointerException(ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL);
        }
        final Stream<T> xs = Stream.of(first);
        final Stream<T> ys = Stream.of(rest);
        return Stream.concat(xs, ys);
    }
    
    /**
     * Prepends a non-null {@code first} to stream {@code rest}, returning a new stream.
     * 
     * @param first
     * @param rest
     * @return
     */
    public static <T> Stream<T> prepend(final T first, final Stream<T> rest) {
        if (first == null) {
            throw new NullPointerException(ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL);
        }
        final Stream<T> xs = Stream.of(first);
        return Stream.concat(xs, rest);
    }
    
    /**
     * Splits a stream into a <code>head</code> and <code>tail</code>. The head is optional as the passed in stream could be empty. The tail is a stream, which could be empty if
     * the input stream is empty or contains only a single element.
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

    /**
     * Returns the longest prefix of the <code>stream</code> whose elements satisfy <code>predicate</code>.
     *
     * @param stream
     * @param predicate
     * @return
     */
    public static <T> Stream<T> takeWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
    }

    private static <T> Spliterator<T> takeWhile(final Spliterator<T> splitr, final Predicate<? super T> predicate) {
        return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            boolean stillGoing = true;

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                if (stillGoing) {
                    final boolean hadNext = splitr.tryAdvance(elem -> {
                        if (predicate.test(elem)) {
                            consumer.accept(elem);
                        } else {
                            stillGoing = false;
                        }
                    });
                    return hadNext && stillGoing;
                }
                return false;
            }
        };
    }

    /**
     * Constructs a zipped stream.
     * 
     * @param xs
     * @param ys
     * @param combine
     * @return
     */
    public static <A, B, Z> Stream<Z> zip(final Stream<? extends A> xs, final Stream<? extends B> ys, final BiFunction<? super A, ? super B, ? extends Z> combine) {
        Objects.requireNonNull(combine);
        final Spliterator<? extends A> xsSpliterator = Objects.requireNonNull(xs).spliterator();
        final Spliterator<? extends B> ysSpliterator = Objects.requireNonNull(ys).spliterator();

        // zipping should loose DISTINCT and SORTED characteristics
        final int characteristics = xsSpliterator.characteristics() & ysSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);

        // let's try to identify the size
        final long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(xsSpliterator.getExactSizeIfKnown(), ysSpliterator.getExactSizeIfKnown())
                : -1;

        // making new iterators to be used as the basis for a new splitterator
        final Iterator<A> xsIterator = Spliterators.iterator(xsSpliterator);
        final Iterator<B> ysIterator = Spliterators.iterator(ysSpliterator);
        final Iterator<Z> zsIterator = new Iterator<Z>() {
            @Override
            public boolean hasNext() {
                return xsIterator.hasNext() && ysIterator.hasNext();
            }

            @Override
            public Z next() {
                return combine.apply(xsIterator.next(), ysIterator.next());
            }
        };

        final Spliterator<Z> split = Spliterators.spliterator(zsIterator, zipSize, characteristics);
        return xs.isParallel() && ys.isParallel() // if both streams are parallel then the produced one can also be parallel
                ? StreamSupport.stream(split, true)
                : StreamSupport.stream(split, false);
    }

}
