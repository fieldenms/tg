package ua.com.fielden.platform.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;

import javax.annotation.Nonnull;

/**
 * A convenience class to provide common collection related routines and algorithms.
 *
 * @author TG Team
 *
 */
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

    @SafeVarargs
    public static <T> List<T> unmodifiableListOf(final T ... elements) {
        return unmodifiableList(asList(elements));
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

    /**
     * Merges the values from {@code map1} and all the maps provided as {@code otherMaps} into a new map of the same type as {@code map1}.
     * Passing just {@code map1} without any other maps, is equivalent to creating a shallow copy of {@code map1}.
     *
     * @param map1 the first map to merge, which should not be {@code null} as it is used for determining the type of the resultant map.
     * @param otherMaps other maps to merge.
     * @return a new map of the same type as {@code map1} with values from {@code map1} and all non-null maps in {@code otherMaps}.
     * @param <K>
     * @param <V>
     */
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

        // now that we have a resultant map, we can put all the maps into it
        result.putAll(map1);
        for (final var mapN: otherMaps) {
            if (mapN != null) {
                result.putAll(mapN);
            }
        }
        return result;
    }

    /**
     * A convenient method to obtain a tail of an array. Returns an empty optional if the length of arrays is 0.
     *
     * @param array
     * @return
     */
    public static <T> Optional<T[]> tail(final T[] array) {
        if (array.length == 0) {
            return Optional.empty();
        }

        return Optional.of(Arrays.copyOfRange(array, 1, array.length));
    }
    /**
     * Converts collection to a string separating the elements with a provided separator.
     * <p>
     * No precaution is taken if toString representation of an element already contains a symbol equal to a separator.
     */
    public static <T> String toString(final Collection<T> collection, final String separator) {
        final StringBuilder buffer = new StringBuilder();
        for (final Iterator<T> iter = collection.iterator(); iter.hasNext();) {
            buffer.append(iter.next() + (iter.hasNext() ? separator : ""));
        }
        return buffer.toString();
    }

    /**
     * Converts collection of {@link AbstractEntity}s to a string by concatenating values of the specified property using a provided separator.
     * <p>
     * No precaution is taken if toString representation of property's value already contains a symbol equal to a separator.
     */
    public static String toString(final Collection<? extends AbstractEntity<?>> collection, final String propertyName, final String separator) {
        final StringBuilder buffer = new StringBuilder();
        for (final Iterator<? extends AbstractEntity<?>> iter = collection.iterator(); iter.hasNext();) {
            final AbstractEntity<?> entity = iter.next();
            final Object value = entity != null ? entity.get(propertyName) : null;
            buffer.append(value + (iter.hasNext() ? separator : ""));
        }
        return buffer.toString();
    }

    /**
     * Tests whether two collections have the same contents irrespective of order.
     * <p>
     * Returns {@code true} iff both collections contain the same elements with the same cardinalities
     * according to {@link Object#equals(Object)}.
     */
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

    /**
     * Removes the first element matching the predicate from the collection and returns an {@link Optional} describing it, otherwise returns an empty {@link Optional}.
     * <p>
     * The supplied collection must be modifiable and must not contain {@code null} values.
     *
     * @param <E> a type of elements in {@code xs}.
     * @param xs a modifiable collection, which gets modified by removing the first element matching {@code pred}.
     * @param pred a predicate for identifying the first element to be removed from {@code xs}.
     * @return
     */
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

}
