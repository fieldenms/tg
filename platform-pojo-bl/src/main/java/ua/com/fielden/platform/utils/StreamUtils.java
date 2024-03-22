package ua.com.fielden.platform.utils;

import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.*;
import java.util.function.*;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableList;

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
     * Constructs a zipped stream.
     */
    public static <A, B, Z> Stream<Z> zip(
            final Collection<? extends A> xs, final Collection<? extends B> ys,
            final BiFunction<? super A, ? super B, ? extends Z> combine)
    {
        return zip(xs.stream(), ys.stream(), combine);
    }

    /**
     * Constructs a zipped stream.
     */
    public static <X, Y, Z> Stream<Z> zip(final X[] xs, final Y[] ys, final BiFunction<? super X, ? super Y, ? extends Z> combine) {
        return zip(Arrays.stream(xs), Arrays.stream(ys), combine);
    }

    /**
     * Performs an action for each pair of elements from given sources. Terminates upon reaching the end of the shorter source.
     */
    public static <X, Y> void zipDo(
            final Collection<? extends X> xs, final Collection<? extends Y> ys,
            final BiConsumer<? super X, ? super Y> action)
    {
        final Iterator<? extends X> xIt = xs.iterator();
        final Iterator<? extends Y> yIt = ys.iterator();

        while (xIt.hasNext() && yIt.hasNext()) {
            action.accept(xIt.next(), yIt.next());
        }
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
     * Returns a stream that is the result of concatenating given streams.
     */
    @SafeVarargs
    public static <T> Stream<T> concat(final Stream<? extends T>... streams) {
        return Stream.of(streams).flatMap(Function.identity());
    }

    /**
     * Given a collection of streams (rows in a matrix), returns a stream of lists each containing nth elements of the
     * original streams (matrix columns).
     * <p>
     * One caveat is that the resulting stream will be as long as the shortest original stream, which is possible only if
     * the "input matrix" has "rows" of different length.
     * <p>
     * T
     *
     * @see #transpose(Collection, Function)
     * @see #transpose(Collection)
     */
    public static <T> Stream<List<T>> transposeBase(final Collection<? extends BaseStream<T, ?>> source) {
        if (source.isEmpty()) {
            return Stream.empty();
        }

        final int n = source.size();
        final List<? extends Spliterator<T>> spliterators = source.stream().map(BaseStream::spliterator).toList();
        final long minSize = spliterators.stream().mapToLong(Spliterator::estimateSize).min().orElse(Long.MAX_VALUE);
        final int characteristics = spliterators.stream().mapToInt(Spliterator::characteristics).reduce(~Spliterator.SORTED, (x, y) -> x & y);

        final var spliterator = new Spliterators.AbstractSpliterator<List<T>>(minSize, characteristics) {
            @Override
            public boolean tryAdvance(final Consumer<? super List<T>> consumer) {
                final List<T> elements = new ArrayList<>(n);

                for (final var spliterator : spliterators) {
                    final boolean advanced = spliterator.tryAdvance(elements::add);
                    // shortest end reached
                    if (!advanced) {
                        return false;
                    }
                }

                consumer.accept(unmodifiableList(elements));
                return true;
            }
        };

        return StreamSupport.stream(spliterator, false);
    }

    public static <T> Stream<List<T>> transpose(final Collection<? extends Collection<T>> source) {
        return transpose(source, Collection::stream);
    }

    public static <T, R> Stream<List<R>> transpose(final Collection<T> source, final Function<? super T, ? extends BaseStream<R, ?>> mapper) {
        return transposeBase(source.stream().map(mapper).toList());
    }

}
