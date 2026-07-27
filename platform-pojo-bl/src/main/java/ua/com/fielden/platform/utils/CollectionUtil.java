package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import jakarta.annotation.Nonnull;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/// A convenience class to provide common collection related routines and algorithms.
///
public final class CollectionUtil {
    private CollectionUtil() {
    }

    @SafeVarargs
    public static <T> Set<T> setOf(final T ... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> Set<T> unmodifiableSetOf(final T ... elements) {
        return unmodifiableSet(new HashSet<>(Arrays.asList(elements)));
    }

    @SafeVarargs
    public static <T> LinkedHashSet<T> linkedSetOf(final T ... elements) {
        return new LinkedHashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> List<T> listOf(final T ... elements) {
        return elements != null ? new ArrayList<>(asList(elements)) : emptyList();
    }

    /// An efficient alternative to `Collections.unmodifiableList(Arrays.asList(array))`.
    ///
    @SafeVarargs
    public static <T> List<T> unmodifiableListOf(final T... elements) {
        return elements.length == 0 ? ImmutableList.of() : unmodifiableList(asList(elements));
    }

    /// An alternative of [#copyOf(Collection)] that allows the given collection to contain nulls.
    ///
    public static <T> List<T> listCopy(final Collection<? extends T> collection) {
        if (collection.isEmpty()) {
            return ImmutableList.of();
        }
        else if (collection instanceof ImmutableCollection<? extends T> immCol) {
            return ImmutableList.copyOf(immCol);
        }
        else {
            final ArrayList<? extends T> list = new ArrayList<>(collection);
            list.trimToSize();
            return unmodifiableList(list);
        }
    }

    /// Returns a new list builder initialised with given elements.
    /// @param xs  initial contents
    ///
    @SafeVarargs
    public static <T> CollectionBuilder<List<T>, T> listb(final T... xs) {
        final List<T> list = new ArrayList<>(xs.length);
        Collections.addAll(list, xs);
        return new CollectionBuilder<>(list);
    }

    /// Returns a new set builder initialised with given elements.
    /// @param xs  initial contents
    ///
    @SafeVarargs
    public static <T> CollectionBuilder<Set<T>, T> setb(final T... xs) {
        final Set<T> set = new HashSet<>(xs.length);
        Collections.addAll(set, xs);
        return new CollectionBuilder<>(set);
    }

    /// Returns a collection that results from concatenating given collections.
    /// The resulting collection is created by calling the supplied constructor.
    ///
    @SafeVarargs
    public static <T, C extends Collection<T>> C concat(final IntFunction<C> constructor, final Collection<? extends T>... collections) {
        final C result = constructor.apply(Arrays.stream(collections).map(Collection::size).reduce(0, Integer::sum));
        for (final var collection : collections) {
            result.addAll(collection);
        }
        return result;
    }

    /// Returns an immutable set that results from concatenating given iterables.
    ///
    @SafeVarargs
    public static <X> ImmutableSet<X> concatSet(final Iterable<? extends X>... iterables) {
        if (iterables.length == 0) {
            return ImmutableSet.of();
        }
        else if (iterables.length == 1) {
            return ImmutableSet.copyOf(iterables[0]);
        }

        // Don't Stream to be a little bit more efficient.
        int size = 0;
        for (final var iter : iterables) {
            if (iter instanceof Collection<?> c) {
                size += c.size();
            }
        }

        final var builder = ImmutableSet.<X>builderWithExpectedSize(size);
        for (final var iter : iterables) {
            builder.addAll(iter);
        }

        return builder.build();
    }

    /// Returns an immutable list that results from concatenating given iterables.
    ///
    @SafeVarargs
    public static <X> ImmutableList<X> concatList(final Iterable<? extends X>... iterables) {
        if (iterables.length == 0) {
            return ImmutableList.of();
        }
        else if (iterables.length == 1) {
            return ImmutableList.copyOf(iterables[0]);
        }

        // Don't Stream to be a little bit more efficient.
        int size = 0;
        for (final var iter : iterables) {
            if (iter instanceof Collection<?> c) {
                size += c.size();
            }
        }

        final var builder = ImmutableList.<X>builderWithExpectedSize(size);
        for (final var iter : iterables) {
            builder.addAll(iter);
        }

        return builder.build();
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(final T2<K, V>... tupples) {
        final Map<K, V> map = new HashMap<>(tupples.length);
        for (final T2<K, V> t2 : tupples) {
            map.put(t2._1, t2._2);
        }
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> linkedMapOf(final T2<K, V>... tupples) {
        final Map<K, V> map = new LinkedHashMap<>(tupples.length);
        for (final T2<K, V> t2 : tupples) {
            map.put(t2._1, t2._2);
        }
        return map;
    }

    /// Merges the values from `map1` and all the maps provided as `otherMaps` into a new map of the same type as `map1`.
    /// Passing just `map1` without any other maps, is equivalent to creating a shallow copy of `map1`.
    ///
    /// @param map1       the first map to merge, which should not be `null` as it is used for determining the type of the resultant map
    /// @param otherMaps  the other maps to merge
    /// @return a new map of the same type as `map1` with values from `map1` and all non-null maps in `otherMaps`.
    ///
    @SafeVarargs
    public static <K,V> Map<K, V> merge(@Nonnull final Map<K, V> map1, final Map<? extends K, ? extends V> ... otherMaps) {
        final Map<K, V> result;
        // get the class of map1 and use it to instantiate the resultant map
        if (map1 == null) {
            throw new NullPointerException("First map cannot be null.");
        }
        final Class<? extends Map> mapClass = map1.getClass();
        try {
            final Constructor<? extends Map> constructor = mapClass.getDeclaredConstructor(); // get the constructor of the map1 class
            constructor.setAccessible(true); // make the constructor accessible
            result = constructor.newInstance(); // create a new instance using the constructor
        } catch (final Exception ex) {
            throw new ReflectionException("Could not instantiate a map of type [%s].".formatted(mapClass), ex);
        }

        // Now that we have a resultant map, we can put all the maps into it.
        result.putAll(map1);
        for (final var mapN: otherMaps) {
            if (mapN != null) {
                result.putAll(mapN);
            }
        }
        return result;
    }

    /// A convenient method to obtain a tail of an array.
    /// Returns an empty optional if the length of arrays is 0.
    ///
    public static <T> Optional<T[]> tail(final T[] array) {
        if (array.length == 0) {
            return Optional.empty();
        }

        return Optional.of(Arrays.copyOfRange(array, 1, array.length));
    }

    /// Converts collection to a string separating the elements with a provided separator.
    ///
    /// No precaution is taken if `toString` representation of an element already contains a symbol equal to a separator.
    ///
    public static <T> String toString(final Collection<T> collection, final String separator) {
        final StringBuilder buffer = new StringBuilder();
        for (final Iterator<T> iter = collection.iterator(); iter.hasNext();) {
            buffer.append(iter.next() + (iter.hasNext() ? separator : ""));
        }
        return buffer.toString();
    }

    /// Converts collection to a string separating the elements with a provided separator.
    ///
    /// No precaution is taken if toString representation of an element already contains a symbol equal to a separator.
    ///
    /// @param mapper  maps each collection element to its string representation
    ///
    public static <T> String toString(final Collection<T> collection, final Function<? super T, String> mapper, final String separator) {
        final StringBuilder buffer = new StringBuilder();
        for (final Iterator<T> iter = collection.iterator(); iter.hasNext();) {
            buffer.append(mapper.apply(iter.next()));
            if (iter.hasNext()) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    /// Converts collection of [AbstractEntity]s to a string by concatenating values of the specified property using a provided separator.
    ///
    /// No precaution is taken if toString representation of property's value already contains a symbol equal to a separator.
    ///
    public static String toString(final Collection<? extends AbstractEntity<?>> collection, final String propertyName, final String separator) {
        final StringBuilder buffer = new StringBuilder();
        for (final Iterator<? extends AbstractEntity<?>> iter = collection.iterator(); iter.hasNext();) {
            final AbstractEntity<?> entity = iter.next();
            final Object value = entity != null ? entity.get(propertyName) : null;
            buffer.append(value + (iter.hasNext() ? separator : ""));
        }
        return buffer.toString();
    }

    /// Tests whether two collections have the same contents irrespective of order.
    ///
    /// Returns `true` iff both collections contain the same elements with the same cardinalities according to [#equals(Object)].
    ///
    public static boolean areEqualByContents(final Collection<?> c1, final Collection<?> c2) {
        if (c1 == null || c2 == null) {
            return false;
        }
        if (c1 == c2) {
            return true;
        }
        if (c1.size() != c2.size()) {
            return false;
        }
        final Map<?, Long> cardinalMap1 = c1.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        final Map<?, Long> cardinalMap2 = c2.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return cardinalMap1.equals(cardinalMap2);
    }

    /// Removes the first element in the given collection that matches the predicate, returning it as an [Optional].
    /// If no matching element is found, an empty [Optional] is returned.
    ///
    /// The supplied collection must be modifiable and must not contain `null` values.
    ///
    /// @param <E>  the element type of the collection
    /// @param xs   a modifiable collection that will be updated by removing the first matching element
    /// @param pred a predicate used to identify the element to remove
    /// @return an [Optional] containing the removed element, or an empty [Optional] if no match is found
    ///
    public static <E> Optional<E> removeFirst(final Collection<E> xs, final Predicate<? super E> pred) {
        if (xs == null) {
            throw new InvalidArgumentException("Collection cannot be null.");
        }
        if (pred == null) {
            throw new InvalidArgumentException("Predicate cannot be null.");
        }

        for (final Iterator<E> iter = xs.iterator(); iter.hasNext();) {
            final E elt = iter.next();
            if (elt == null) {
                throw new InvalidArgumentException("Collection contains null elements, which is not permitted.");
            }
            if (pred.test(elt)) {
                iter.remove();
                return Optional.of(elt);
            }
        }

        return Optional.empty();
    }

    /// If the collection is not empty, returns its first element, which must not be `null`.
    ///
    /// @throws InvalidArgumentException  if the first element is `null``
    /// @see #firstNullable(Collection)
    ///
    public static <E> Optional<E> first(final Collection<E> xs) {
        if (xs.isEmpty()) {
            return Optional.empty();
        }

        // Creating a new iterator bears a cost, try to avoid it.
        final E elt = xs instanceof SequencedCollection<E> seq ? seq.getFirst() : xs.iterator().next();
        if (elt == null) {
            throw new InvalidArgumentException("Collection's first element must not be null.");
        }

        return Optional.of(elt);
    }

    /// If the collection is not empty, returns its first element.
    /// If the first element is `null`, an empty optional is returned.
    ///
    /// @see #firstNullable(Collection)
    public static <E> Optional<E> firstNullable(final Collection<E> xs) {
        if (xs.isEmpty()) {
            return Optional.empty();
        }
        // Creating a new iterator bears a cost, try to avoid it.
        final E elt = xs instanceof SequencedCollection<E> seq ? seq.getFirst() : xs.iterator().next();
        return Optional.ofNullable(elt);
    }

    /// Produces a new immutable list containing all elements from `xs` with `x` appended at the end.
    ///
    /// @param <X> the element type
    /// @param xs  the source elements to include in the resulting list
    /// @param x   the element to append as the last element of the resulting list
    /// @return an immutable list containing all elements from `xs` followed by `x`
    ///
    public static <X> ImmutableList<X> append(final Iterable<? extends X> xs, final X x) {
        final int xsSize = xs instanceof Collection<?> c ? c.size() : 0;
        final var builder = ImmutableList.<X> builderWithExpectedSize(xsSize + 1);
        return builder.addAll(xs).add(x).build();
    }

    /// Transforms a map into another map by applying the given mapping functions to its entries.
    ///
    /// Disallows duplicates among resulting keys and disallows `null` as a resulting key.
    ///
    /// @param <K>  the type of keys in the input map
    /// @param <V>  the type of values in the input map
    /// @param <RK> the type of keys in the resulting map
    /// @param <RV> the type of values in the resulting map
    /// @param map          the source map whose entries are to be transformed
    /// @param keyMapper    function that produces a key for the resulting map from an entry's key and value
    /// @param valueMapper  function that produces a value for the resulting map from an entry's key and value
    /// @return a new map whose entries are the result of applying the given mappers to each input entry
    /// @throws IllegalStateException if a resulting key is `null` or if duplicate keys are produced
    ///
    public static <K, V, RK, RV> Map<RK, RV> map(
            final Map<K, V> map,
            final BiFunction<? super K, ? super V, RK> keyMapper,
            final BiFunction<? super K, ? super V, RV> valueMapper)
    {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> {
                            final RK newKey = keyMapper.apply(entry.getKey(), entry.getValue());
                            if (newKey == null) {
                                throw new IllegalStateException("Key was mapped to null. Entry: %s".formatted(entry));
                            }
                            return newKey;
                        },
                        entry -> valueMapper.apply(entry.getKey(), entry.getValue())));
    }

    /// Like [#map(Map,BiFunction,BiFunction)] but only the values of the input map undergo transformation.
    ///
    public static <K, V, RV> Map<K, RV> mapValues(
            final Map<K, V> map,
            final BiFunction<? super K, ? super V, RV> valueMapper)
    {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> valueMapper.apply(entry.getKey(), entry.getValue())));
    }

    /// Returns a sublist, backed by the specified list, that excludes `n` rightmost elements.
    ///
    public static <X> List<X> dropRight(final List<X> xs, final int n) {
        if (n >= xs.size()) {
            return ImmutableList.of();
        }
        else {
            return xs.subList(0, xs.size() - n);
        }
    }

    public static final class CollectionBuilder<C extends Collection<E>, E> {
        private final C collection;

        private CollectionBuilder(final C collection) {
            this.collection = collection;
        }

        private CollectionBuilder(final Supplier<? extends C> supplier) {
            this.collection = supplier.get();
        }

        public CollectionBuilder<C, E> add(final E e) {
            collection.add(e);
            return this;
        }

        public CollectionBuilder<C, E> addAll(final Collection<? extends E> es) {
            collection.addAll(es);
            return this;
        }

        public C $() {
            return collection;
        }
    }

    /// Partitions `xs` into a pair `(a, b)`, where `a` contains all elements that satisfy `predicate`
    /// and `b` contains all remaining elements.
    ///
    /// The relative encounter order of elements in `xs` is preserved within each resulting list.
    ///
    /// @param <X>       the element type
    /// @param xs        the source of elements to partition; may contain `null`
    /// @param predicate the predicate used to decide which list an element belongs to
    /// @return a pair of lists `(a, b)` where the first list contains elements satisfying `predicate`
    ///         and the second list contains all other elements
    ///
    public static <X> T2<List<X>, List<X>> partitionBy(final Iterable<X> xs, final Predicate<? super X> predicate) {
        if (xs instanceof Collection<X> coll) {
            if (coll.isEmpty()) {
                return t2(ImmutableList.of(), ImmutableList.of());
            }
            else if (coll.size() == 1) {
                final var x0 = coll instanceof SequencedCollection<X> seq ? seq.getFirst() : coll.iterator().next();
                return predicate.test(x0) ? t2(ImmutableList.of(x0), ImmutableList.of()) : t2(ImmutableList.of(), ImmutableList.of(x0));
            }
            else {
                return coll.stream().collect(StreamUtils.partitioning(predicate, coll.size()));
            }
        }
        else {
            return Streams.stream(xs).collect(StreamUtils.partitioning(predicate));
        }
    }

}
