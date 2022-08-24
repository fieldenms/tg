package ua.com.fielden.platform.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ua.com.fielden.platform.streaming.SequentialGroupingStream;
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
     * Returns the longest prefix of the {@code stream} until (inclusive) an element that satisfies {@code predicate} is encountered.
     * <p>
     * If no such element was encountered then the whole stream is returned.
     *
     * @param stream
     * @param predicate
     * @return
     */
    public static <T> Stream<T> stopAfter(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamSupport.stream(stopAfter(stream.spliterator(), predicate), false);
    }

    private static <T> Spliterator<T> stopAfter(final Spliterator<T> splitr, final Predicate<? super T> predicate) {
        return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            boolean stillGoing = true;

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                if (stillGoing) {
                    final boolean hadNext = splitr.tryAdvance(elem -> {
                        consumer.accept(elem);
                        if (predicate.test(elem)) {
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
     * Returns a stream of distinct elements from <code>stream</code>, where uniqueness of an element is determined by <code>uniquenessMapper</code>.
     * <p>
     * It is assumed that the return type of <code>uniquenessMapper</code> will have a proper implementation of <code>hashCode()</code>, otherwise the resulting stream may still contain duplicates.
     * <p>
     * The order of the original stream's elements is preserved.
     *
     * @param stream
     * @param uniquenessMapper a function that maps the original element to its unique representation
     * @return
     */
    public static <T, R> Stream<T> distinct(final Stream<T> stream, final Function<T, R> uniquenessMapper) {
        return StreamSupport.stream(distinct(stream.spliterator(), uniquenessMapper), false);
    }

    private static <T, R> Spliterator<T> distinct(final Spliterator<T> splitr, final Function<T, R> mapper) {
        return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            final LinkedHashSet<R> uniqs = new LinkedHashSet<>();

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                return splitr.tryAdvance(elem -> {
                    final R uniqRepr = mapper.apply(elem);
                    if (!uniqs.contains(uniqRepr)) {
                        consumer.accept(elem);
                        uniqs.add(uniqRepr);
                    }
                });
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

    /**
     * Splits stream {@code source} into a windowed stream where elements from {@code source} are placed in groups of size {@code windowSize}.
     * The last group may have its size less than the {@code windowSize}.
     *
     * @param <T> A type over which to stream.
     * @param source An input stream to be "windowed".
     * @param windowSize A window size.
     * @return
     */
    public static <T> Stream<List<T>> windowed(final Stream<T> source, final int windowSize){
        return SequentialGroupingStream.stream(source, (el, group) -> group.size() < windowSize);
    }

}