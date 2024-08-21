package ua.com.fielden.platform.utils;

import com.google.common.collect.Iterables;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNonNull;

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
     * Returns a stream of distinct elements from {@code stream}, where unique identity of an element is determined by {@code uidMapper}.
     * <p>
     * It is required that the return type of {@code uidMapper} has proper implementation of {@code hashCode()} and {@code equals()}.
     * <p>
     * The order of the original stream's elements is preserved.
     *
     * @param stream
     * @param uidMapper a function that maps the original element to its unique identity.
     * @return
     */
    public static <T, R> Stream<T> distinct(final Stream<T> stream, final Function<T, R> uidMapper) {
        return StreamSupport.stream(distinct(stream.spliterator(), uidMapper), false);
    }

    private static <T, R> Spliterator<T> distinct(final Spliterator<T> splitr, final Function<T, R> uidMapper) {
        return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            final LinkedHashSet<R> uniqs = new LinkedHashSet<>();

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                return splitr.tryAdvance(elem -> {
                    final R uid = uidMapper.apply(elem);
                    if (!uniqs.contains(uid)) {
                        consumer.accept(elem);
                        uniqs.add(uid);
                    }
                });
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
     * Constructs a zipped stream.
     * 
     * @param xs
     * @param ys
     * @param combine
     * @return
     */
    public static <A, B, Z> Stream<Z> zip(final Stream<? extends A> xs, final Stream<? extends B> ys, final BiFunction<? super A, ? super B, ? extends Z> combine) {
        requireNonNull(combine, "combine");
        final Spliterator<? extends A> xsSpliterator = requireNonNull(xs, "xs").spliterator();
        final Spliterator<? extends B> ysSpliterator = requireNonNull(ys, "ys").spliterator();

        // zipping should lose DISTINCT and SORTED characteristics
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

    /**
     * Creates a filtering function that accepts only instances of the given type. Intended to be passed to {@link Stream#mapMulti(BiConsumer)}.
     * It replaces the following pattern:
     * <pre>{@code
     *     stream.map(x -> x instanceof Y y ? y : null)
     *           .filter(y -> y != null)
     * }</pre>
     * with:
     * <pre>{@code
     *     stream.mapMulti(typeFilter(Y.class))
     * }</pre>
     *
     * <b>NOTE</b>: this method, unlike {@code instanceof}, can be used to test incompatible types. As such, it sacrifices
     * the benefit of compile-time detection of "meaningless" filtering for succinctness.
     * For example, {@code "a" instanceof List} is an illegal statement, while
     * {@code Stream.of("a").mapMulti(typeFilter(List.class))} is allowed.
     *
     * @param type  the type of elements that will be preserved in the resulting stream
     */
    public static <T, R extends T> BiConsumer<T, Consumer<R>> typeFilter(Class<R> type) {
        return (item, sink) -> {
            if (type.isInstance(item)) {
                sink.accept(type.cast(item));
            }
        };
    }

    /**
     * Sometimes there are situations where it is required to identify whether a stream is empty, and if it is empty, supply an alternative stream.
     * <p>
     * There is no way to check if a stream is empty without invoking a terminal operation on it. This method returns either a stream with the elements of the original stream, if it was not empty,
     * or an alternative infinite stream of elements generated by {@code supplier}.
     *
     * @param stream
     * @param supplier
     * @return
     * @param <T>
     */
    public static <T> Stream<T> supplyIfEmpty(final Stream<T> stream, final Supplier<T> supplier) {
        final Spliterator<T> spliterator = stream.spliterator();
        final AtomicReference<T> referenceToFirstElement = new AtomicReference<>();
        // If we can advance then the stream is not empty, but because we advanced it is necessary to create a new stream.
        if (spliterator.tryAdvance(referenceToFirstElement::set)) {
            return Stream.concat(Stream.of(referenceToFirstElement.get()), StreamSupport.stream(spliterator, stream.isParallel()));
        }
        // Otherwise, let's return an infinite stream of elements generated by supplier.
        else {
            return Stream.generate(supplier);
        }
    }

    /**
     * Transforms the given stream by filtering out all elements contained in {@code ys} that satisfy the predicate.
     *
     * @param test  returns {@code true} if an {@code x} matches a {@code y} and should be removed from the stream
     */
    public static <X, Y> Stream<X> removeAll(final Stream<X> xs, final Iterable<Y> ys, final BiPredicate<? super X, ? super Y> test) {
        requireNonNull(xs, "xs");
        requireNonNull(ys, "ys");
        requireNonNull(test, "test");

        if (ys instanceof Collection<Y> ysColl) {
            return removeAll(xs, ysColl, test);
        } else {
            return xs.filter(x -> !Iterables.any(ys, y -> test.test(x, y)));
        }
    }

    /**
     * @see #removeAll(Stream, Iterable, BiPredicate)
     */
    public static <X, Y> Stream<X> removeAll(final Stream<X> xs, final Collection<Y> ys, final BiPredicate<? super X, ? super Y> test) {
        requireNonNull(xs, "xs");
        requireNonNull(ys, "ys");
        requireNonNull(test, "test");

        if (ys.isEmpty()) {
            return xs;
        } else {
            return xs.filter(x -> ys.stream().noneMatch(y -> test.test(x, y)));
        }
    }

    /**
     * Transforms the given stream by filtering out all elements contained in {@code items}.
     */
    public static <X> Stream<X> removeAll(final Stream<X> xs, final Iterable<X> items) {
        return removeAll(xs, items, Objects::equals);
    }

    /**
     * @see #removeAll(Stream, Iterable)
     */
    public static <X> Stream<X> removeAll(final Stream<X> xs, final Collection<X> items) {
        return removeAll(xs, items, Objects::equals);
    }

}
