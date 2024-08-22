package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Spliterators.spliteratorUnknownSize;
import static ua.com.fielden.platform.utils.IteratorUtils.distinctIterator;

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
     * Equivalent of {@link IteratorUtils#distinctIterator(Iterator, Function)} but for streams.
     */
    public static <T, U> Stream<T> distinct(final Stream<T> stream, final Function<T, U> classifier) {
        return StreamSupport.stream(spliteratorUnknownSize(distinctIterator(stream.iterator(), classifier), 0),
                                    false);
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
    public static <X, Y, Z> Stream<Z> zip(final BaseStream<? extends X, ?> xs, final BaseStream<? extends Y, ?> ys, final BiFunction<? super X, ? super Y, ? extends Z> combine) {
        Objects.requireNonNull(combine);
        final Spliterator<? extends X> xsSpliterator = Objects.requireNonNull(xs).spliterator();
        final Spliterator<? extends Y> ysSpliterator = Objects.requireNonNull(ys).spliterator();

        // zipping should lose DISTINCT and SORTED characteristics
        final int characteristics = xsSpliterator.characteristics() & ysSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);

        // let's try to identify the size
        final long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(xsSpliterator.getExactSizeIfKnown(), ysSpliterator.getExactSizeIfKnown())
                : -1;

        // making new iterators to be used as the basis for a new splitterator
        final Iterator<X> xsIterator = Spliterators.iterator(xsSpliterator);
        final Iterator<Y> ysIterator = Spliterators.iterator(ysSpliterator);
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
    public static <X, Y, Z> Stream<Z> zip(
            final Collection<? extends X> xs, final Collection<? extends Y> ys,
            final BiFunction<? super X, ? super Y, ? extends Z> combine)
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
        IteratorUtils.zipDo(xs.iterator(), ys.iterator(), action);
    }

    /**
     * Performs an action for each pair of elements from given sources. Terminates upon reaching the end of the shorter source.
     */
    public static <X, Y> void zipDo(
            final BaseStream<? extends X, ?> xs, final BaseStream<? extends Y, ?> ys,
            final BiConsumer<? super X, ? super Y> action)
    {
        IteratorUtils.zipDo(xs.iterator(), ys.iterator(), action);
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
     * {@snippet :
     *     stream.map(x -> x instanceof Y y ? y : null)
     *           .filter(y -> y != null)
     * }
     * with:
     * {@snippet :
     *     stream.mapMulti(typeFilter(Y.class))
     * }
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
     * If the stream is empty, returns an empty optional. Otherwise, equivalent to {@link #foldLeft(BaseStream, Object, BiFunction)}
     * where the initial result value is the first element of the stream and folding is performed on the rest of the stream.
     */
    public static <T> Optional<T> foldLeft(final BaseStream<T, ?> stream,
                                           final BiFunction<? super T, ? super T, T> fn) {
        var iter = stream.iterator();

        if (!iter.hasNext())
            return Optional.empty();

        return Optional.of(foldLeft_(iter, iter.next(), fn));
    }

    /**
     * Sequential reduction of the stream from left to right.
     *
     * @param fn  function that folds an element into the result
     * @param init  initial result value
     */
    public static <A, B> B foldLeft(final BaseStream<A, ?> stream,
                                    final B init,
                                    final BiFunction<? super B, ? super A, B> fn) {
        return foldLeft_(stream.iterator(), init, fn);
    }

    private static <A, B> B foldLeft_(final Iterator<A> iter,
                                      final B init,
                                      final BiFunction<? super B, ? super A, B> fn) {
        if (!iter.hasNext())
            return init;

        B acc = init;
        while (iter.hasNext())
            acc = fn.apply(acc, iter.next());

        return acc;
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
<<<<<<< HEAD
     * Returns an infinite stream of integers starting from the given one.
     */
    public static IntStream integers(final int start) {
        return IntStream.iterate(start, i -> i + 1);
    }

    /**
     * Returns an infinite stream of longs starting from the given one.
     */
    public static LongStream longs(final long start) {
        return LongStream.iterate(start, l -> l + 1);
    }

    /**
     * Pairs each elements of a stream with a number and applies the given function to obtain an element of
     * the resulting stream. Numbers are drawn from an infinite stream starting from {@code start} and increasing by 1.
     */
    public static <X, Y> Stream<Y> enumerate(final BaseStream<X, ?> xs, final int start, final EnumerateF<? super X, Y> f) {
        // construct an iterator by hand instead of using zip() to avoid boxing of integers
        final Iterator<Y> ysIterator = new Iterator<Y>() {
            final Iterator<X> xsIterator = xs.iterator();
            int i = start;

            @Override
            public boolean hasNext() {
                return xsIterator.hasNext();
            }

            @Override
            public Y next() {
                final Y y = f.apply(xsIterator.next(), i);
                i += 1;
                return y;
            }
        };
        return StreamSupport.stream(spliteratorUnknownSize(ysIterator, 0), false);
    }

    /**
     * {@link #enumerate(BaseStream, int, EnumerateF)} starting from 0.
     */
    public static <X, Y> Stream<Y> enumerate(final BaseStream<X, ?> xs, final EnumerateF<? super X, Y> f) {
        return enumerate(xs, 0, f);
    }

    @FunctionalInterface
    public interface EnumerateF<X, Y> {
        Y apply(X x, int i);
    }

    /**
     * Builds an immutable map from streams of keys and values.
     * Terminates upon reaching the end of the shorter stream.
     *
     * @param ks  stream of keys
     * @param vs  stream of values
     */
    public static <K, V> ImmutableMap<K, V> collectToImmutableMap(final BaseStream<? extends K, ?> ks,
                                                                  final BaseStream<? extends V, ?> vs) {
        final var builder = ImmutableMap.<K, V>builder();
        zipDo(ks, vs, builder::put);
        return builder.build();
    }

    /**
     * Builds an immutable map from 2 streams by applying given functions to obtain keys and values.
     * Terminates upon reaching the end of the shorter stream.
     *
     * @param kf  function that produces keys
     * @param vf  function that produces values
     */
    public static <X, Y, K, V> ImmutableMap<K, V> collectToImmutableMap(final BaseStream<X, ?> xs,
                                                                        final BaseStream<Y, ?> ys,
                                                                        final BiFunction<? super X, ? super Y, K> kf,
                                                                        final BiFunction<? super X, ? super Y, V> vf) {
        final var builder = ImmutableMap.<K, V> builder();
        zipDo(xs, ys, (x, y) -> builder.put(kf.apply(x, y), vf.apply(x, y)));
        return builder.build();
    }

    /**
     * Given a collection of streams, returns a stream of lists where each list is formed by consuming the next element
     * from each input stream.
     * This operation can be thought of as matrix transposition where the input streams are rows and the output lists are columns.
     * <p>
     * One caveat is that the resulting stream will be as long as the shortest input stream, which is possible only if
     * the "input matrix" has "rows" of different length.
     * <p>
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

    /**
     * @see #transposeBase(Collection)
     */
    public static <T> Stream<List<T>> transpose(final Collection<? extends Collection<T>> source) {
        return transpose(source, Collection::stream);
    }

    /**
     * @see #transposeBase(Collection)
     */
    public static <T, R> Stream<List<R>> transpose(final Collection<T> source, final Function<? super T, ? extends BaseStream<R, ?>> mapper) {
        return transposeBase(source.stream().map(mapper).toList());
    }

    /**
     * Tests whether a stream contains a single element.
     * This is a <b>terminal</b> operation on the stream.
     */
    public static boolean isSingleElementStream(final BaseStream<?, ?> stream) {
        final var it = stream.iterator();
        int i = 0;
        while (i < 2 && it.hasNext()) {
            it.next();
            i += 1;
        }

        return i == 1;
    }

    /**
     * Tests whether a stream contains more than one element.
     * This is a <b>terminal</b> operation on the stream.
     */
    public static boolean isMultiElementStream(final BaseStream<?, ?> stream) {
        final var it = stream.iterator();
        int i = 0;
        while (i < 2 && it.hasNext()) {
            it.next();
            i += 1;
        }

        return i > 1;
    }

    /**
     * Tests whether all integers in a stream are equal. If the stream is empty, returns an empty optional.
     * <p>
     * This method is more efficient than the usage of {@link IntStream#distinct()} because the implementation of the latter
     * uses boxing.
     */
    public static Optional<Boolean> areAllEqual(final IntStream stream) {
        final PrimitiveIterator.OfInt it = stream.iterator();
        if (!it.hasNext()) {
            return Optional.empty();
        }

        final int n = it.next();
        while (it.hasNext()) {
            if (n != it.next()) {
                return OPTIONAL_FALSE;
            }
        }

        return OPTIONAL_TRUE;
    }
    // where
    private static final Optional<Boolean> OPTIONAL_FALSE = Optional.of(Boolean.FALSE);
    private static final Optional<Boolean> OPTIONAL_TRUE = Optional.of(Boolean.TRUE);

    /**
     * Constructs a stream by zipping the given stream with a sequential ascending stream of integers (step = 1).
     *
     * @param start  starting point of the integer stream
     */
    public static <R, T> Stream<R> enumerated(final BaseStream<T, ?> stream, final int start, final BiFunction<? super Integer, ? super T, R> combiner) {
        return zip(IntStream.iterate(start, n -> n + 1), stream, combiner);
    }

    /**
     * Like {@link #enumerated(BaseStream, int, BiFunction)} but always starts from 0.
     */
    public static <R, T> Stream<R> enumerated(final BaseStream<T, ?> stream, final BiFunction<? super Integer, ? super T, R> combiner) {
        return zip(IntStream.iterate(0, n -> n + 1), stream, combiner);
    }

}
