package ua.com.fielden.platform.processors.metamodel.elements.utils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A singleton class representing a cache for instances of {@link TypeElement} produced by (potentially multiple) instances of {@link Elements}.
 * <p>
 * Since different instances of {@link Elements} produce incompatible type elements, there is a need to maintain a 2-level cache -
 * a map of type element caches keyed on instances of {@link Elements}.
 * <p>
 * Recording of statistics about type element caches can be enabled through {@link #recordStats()} and accessed with {@link #getStats()}.
 *
 * @author TG Team
 */
public final class TypeElementCache {

    private static boolean statsEnabled = false;
    private static final TypeElementCache INSTANCE = new TypeElementCache();

    /** Top-level cache keyed on instances of {@link Elements} and based on reference-equality. */
    private final IdentityHashMap<Elements, Cache<String, TypeElement>> cache = new IdentityHashMap<>();

    private TypeElementCache() {}

    /**
     * Enables recording of statistics about type element caches. 
     * @see #getStats()
     */
    public static void recordStats() {
        statsEnabled = true;
    }

    /**
     * Returns an optional describing an unmodifiable cache for the specified {@link Elements} instance. 
     * @param elements
     * @return
     */
    protected static Optional<Map<String, TypeElement>> cacheViewFor(final Elements elements) {
        return Optional.ofNullable(INSTANCE.cache.get(elements)).map(eltCache -> eltCache.cacheView());
    }

    /**
     * Clears this cache by first clearing all sub-caches associated with {@link Elements} instances, and then clears the top-level cache.
     * Cache statistics, if enabled, are cleared as well.
     */
    public static void clear() {
        INSTANCE.cache.forEach((elements, eltCache) -> eltCache.clear());
        INSTANCE.cache.clear();
    }

    /**
     * Similar to {@link Elements#getTypeElement(CharSequence)} with the results being cached.
     * Another distinction of this method is that in case of a multi-module application, where multiple type elements
     * have the same canonical name, it will return the first one, unlike the specification, which dictates that {@code null} be returned.
     *
     * @param elements the {@link Elements} instance to use for lookup
     * @param name canonical name of the type element to find
     * @return the named type element or {@code null} if there was no matches
     */
    public static TypeElement getTypeElement(final Elements elements, final String name) {
        Objects.requireNonNull(elements, "Argument elements cannot be null.");
        Objects.requireNonNull(name, "Argument name cannot be null.");
        final var elementCache = INSTANCE.getCache(elements);
        return elementCache.get(name, () -> elements.getAllTypeElements(name).stream().findFirst().orElse(null));
    }

    /**
     * Returns an unmodifiable view of the map containing statistics about type element caches.
     * Each cache's statistics is represented by a map keyed on type element names that map to their respective hit counts.
     *
     * @return
     */
    public static Map<Elements, Map<String, Long>> getStats() {
        return INSTANCE.cache.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey(), entry -> entry.getValue().getStats()));
    }

    /**
     * Returns the cache associated with the given instance of {@link Elements}. If a cache doesn't exist yet, it will be created.
     *
     * @param elements
     * @return
     */
    private Cache<String, TypeElement> getCache(final Elements elements) {
        return cache.computeIfAbsent(elements, k -> new Cache<String, TypeElement>(statsEnabled));
    }

    /**
     * A simple cache backed up by a {@link HashMap} with optional recording of statistics.
     * {@code null} values are not cached.
     *
     * @author TG Team
     */
    private static final class Cache<K, V> {
        private final HashMap<K, V> cache = new HashMap<>();
        private final HashMap<K, Long> stats = new HashMap<>();
        private final Function<K, V> cacheGetter;

        Cache(final boolean statsEnabled) {
            this.cacheGetter = statsEnabled ? k -> {
                recordStats(k);
                return cacheGet(k);
            } : this::cacheGet;
        }

        private void recordStats(final K key) {
            stats.compute(key, (k, v) -> v == null ? 0 : v + 1L);
        }

        private V cacheGet(final K key) {
            return cache.get(key);
        }

        /**
         * Equivalent to {@link HashMap#computeIfAbsent(Object, java.util.function.Function)}.
         */
        public V get(final K key, Supplier<V> valueSupplier) {
            return cache.computeIfAbsent(key, k -> valueSupplier.get());
        }

        /**
         * Clears the cache and its statistics.
         */
        public void clear() {
            cache.clear();
            stats.clear();
        }

        /**
         * Returns an unmodifiable view of the cache.
         */
        public Map<K, V> cacheView() {
            return Collections.unmodifiableMap(cache);
        }

        /**
         * Returns an unmodifiable view of the cache statistics.
         */
        public Map<K, Long> getStats() {
            return Collections.unmodifiableMap(stats);
        }
    }

}
