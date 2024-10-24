package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.utils.function.Function3;

import java.util.Map;

import static java.lang.String.format;

/**
 * Utilites for immutable {@linkplain Map maps}.
 * <p>
 * <b>Null as either key or value is not permitted</b>.
 *
 * @see ImmutableCollectionUtil
 */
public final class ImmutableMapUtils {

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
     * @param combiner  function that combines values from both maps associated with the same key, must not return null
     * @param map1  must not contain null key or null values
     * @param map2  must not contain null key or null values
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
                    throw new NullPointerException(
                            format("Map must not contain null values. Null was associated with key: %s", key));
                }

                final SV resultValue;
                if (map2.containsKey(key)) {
                    final var value2 = map2.get(key);
                    if (value2 == null) {
                        throw new NullPointerException(
                                format("Map must not contain null values. Null was associated with key: %s", key));
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
     * Returns an immutable map that contains entries from both maps.
     * If duplicate keys are encountered, values from {@code map2} are used.
     *
     * @param map1  must not contain null key or null values
     * @param map2  must not contain null key or null values
     */
    public static <K, V1 extends SV, V2 extends SV, SV> Map<K, SV> unionRight(
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V1> map1,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V2> map2)
    {
        return union((k, v1, v2) -> v2, map1, map2);
    }

    /**
     * Returns an immutable map that contains entries from both maps.
     * If duplicate keys are encountered, values from {@code map1} are used.
     *
     * @param map1  must not contain null key or null values
     * @param map2  must not contain null key or null values
     */
    public static <K, V1 extends SV, V2 extends SV, SV> Map<K, SV> unionLeft(
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V1> map1,
            final Map</*@Nonnull*/ K, /*@Nonnull*/ V2> map2)
    {
        return union((k, v1, v2) -> v1, map1, map2);
    }


    private ImmutableMapUtils() {}

}
