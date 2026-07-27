package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.utils.function.Function3;

import java.util.Map;

/**
 * Utilities for immutable {@linkplain Map maps}.
 * <p>
 * <b>Null as either key or value is not permitted</b>.
 *
 * @see ImmutableSetUtils
 * @see ImmutableListUtils
 */
public final class ImmutableMapUtils {

    public static final String ERR_NULL_VALUES_ARE_NOT_PERMITTED = "Map must not contain null values. Null was associated with key: %s.";

    /**
     * Returns an immutable map that contains the given key and value, and all entries from the given map.
     * If the key is already present in the given map, the associated value is replaced with the supplied value.
     *
     * @param map  must not contain null key or null values
     * @param key  must not be null
     * @param value  must not be null
     */
    public static <K extends SK, V extends SV, SK, SV> Map<SK, SV> insert(
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V> map,
            final /*@Nonnull*/ SK key,
            final /*@Nonnull*/ SV value)
    {
        return map.isEmpty()
                ? ImmutableMap.of(key, value)
                : ImmutableMap.<SK, SV>builderWithExpectedSize(map.size() + 1)
                        .putAll(map)
                        .put(key, value)
                        .buildKeepingLast();
    }

    /**
     * Returns an immutable map that contains entries from both maps.
     * The provided combining function is used to resolve conflicts (when a key is present in both maps).
     *
     * @param combiner  function that combines values from both maps associated with the same key, must not return null.
     *                  Calls have the form {@code combiner(key, value1, value2)}, where {@code value1} is from {@code map1}
     *                  and {@code value2} is from {@code map2}.
     * @param map1  must not contain null keys or null values
     * @param map2  must not contain null keys or null values
     */
    public static <K, V1 extends SV, V2 extends SV, SV> Map<K, SV> union(
            final Function3<? super K, ? super V1, ? super V2, SV> combiner,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V1> map1,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V2> map2)
    {
        if (map1.isEmpty()) {
            return ImmutableMap.copyOf(map2);
        }
        else if (map2.isEmpty()) {
            return ImmutableMap.copyOf(map1);
        }
        else {
            // Guava's ImmutableMap builder doesn't provide an API that takes a combiner function, so we take care of duplicate keys ourselves.
            final var builder = ImmutableMap.<K, SV>builderWithExpectedSize(map1.size() + map2.size());
            map1.forEach((key, value1) -> {
                if (value1 == null) {
                    throw new NullPointerException(ERR_NULL_VALUES_ARE_NOT_PERMITTED.formatted(key));
                }

                final SV resultValue;
                if (map2.containsKey(key)) {
                    final var value2 = map2.get(key);
                    if (value2 == null) {
                        throw new NullPointerException(ERR_NULL_VALUES_ARE_NOT_PERMITTED.formatted(key));
                    }
                    resultValue = combiner.apply(key, value1, value2);
                }
                else {
                    resultValue = value1;
                }

                builder.put(key, resultValue);
            });
            map2.forEach((key, value) -> {
                // Duplicate keys were already handled.
                if (!map1.containsKey(key)) {
                    builder.put(key, value);
                }
            });
            return builder.build();
        }
    }

    /**
     * Right-biased union.
     * Returns an immutable map that contains entries from both maps.
     * If both maps have an entry for the same key, the value from {@code map2} is used.
     *
     * @param map1  must not contain null key or null values
     * @param map2  must not contain null key or null values
     */
    public static <K, V1 extends SV, V2 extends SV, SV> Map<K, SV> unionRight(
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V1> map1,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V2> map2)
    {
        if (map1 == map2 && map2 instanceof ImmutableMap<K,V2> map) {
            return (Map<K, SV>) map;
        }
        else {
            return union((k, v1, v2) -> v2, map1, map2);
        }
    }

    /**
     * Left-biased union.
     * Returns an immutable map that contains entries from both maps.
     * If both maps have an entry for the same key, the value from {@code map1} is used.
     *
     * @param map1  must not contain null key or null values
     * @param map2  must not contain null key or null values
     */
    public static <K, V1 extends SV, V2 extends SV, SV> Map<K, SV> unionLeft(
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V1> map1,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V2> map2)
    {
        if (map1 == map2 && map1 instanceof ImmutableMap<K,V1> map) {
            return (Map<K, SV>) map;
        }
        else {
            return union((k, v1, v2) -> v1, map1, map2);
        }
    }


    private ImmutableMapUtils() {}

}
