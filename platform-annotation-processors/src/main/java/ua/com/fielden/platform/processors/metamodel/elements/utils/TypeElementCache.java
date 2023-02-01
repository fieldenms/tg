package ua.com.fielden.platform.processors.metamodel.elements.utils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * A singleton class representing a cache for instances of {@link TypeElement} produced by (potentially multiple) instances of {@link Elements}.
 * <p>
 * Since different instances of {@link Elements} produce incompatible type elements, there is a need to maintain a 2-level cache -
 * a map of type element caches keyed on instances of {@link Elements}.
 *
 * @author TG Team
 */
public final class TypeElementCache {

    private static TypeElementCache instance;

    /** Top-level cache keyed on instances of {@link Elements} and based on reference-equality. */
    private final IdentityHashMap<Elements, HashMap<String, TypeElement>> cache = new IdentityHashMap<>();

    private TypeElementCache() {}

    /**
     * Returns the singleton instance.
     * @return
     */
    public static TypeElementCache getInstance() {
        if (instance == null) {
            instance = new TypeElementCache();
        }
        return instance;
    }


    /**
     * Clears this cache by first clearing all sub-caches associated with {@link Elements} instances, and then clears the top-level cache.
     */
    public void clear() {
        cache.forEach((elements, map) -> map.clear());
        cache.clear();
    }

    /**
     * Similar to {@link Elements#getTypeElement(CharSequence)} with the results being cached. No-match results (when {@code null} is returned)
     * are cached as well. Another distinction of this method is that in case of a multi-module application, where multiple type elements
     * have the same canonical name, it will return the first one, unlike the specification, which dictates that {@code null} be returned.
     * 
     * @param elements the {@link Elements} instance to use for lookup
     * @param name canonical name of the type element to find
     * @return the named type element or {@code null} if there was no matches
     */
    public TypeElement getTypeElement(final Elements elements, final String name) {
        Objects.requireNonNull(elements, "Argument elements cannot be null.");
        Objects.requireNonNull(name, "Argument name cannot be null.");
        final var elementCache = getCache(elements);
        return computeIfTrulyAbsent(elementCache, name, () -> elements.getAllTypeElements(name).stream().findFirst().orElse(null));
    }

    /**
     * Returns the cache associated with the given instance of {@link Elements}. If a cache doesn't exist yet, it will be created.
     * @param elements
     * @return
     */
    private HashMap<String, TypeElement> getCache(final Elements elements) {
        return cache.computeIfAbsent(elements, k -> new HashMap<>());
    }

    /**
     * Similar to {@link HashMap#computeIfAbsent(Object, java.util.function.Function)}, but treats {@code null} values equally. 
     * <p>
     * The value is computed and inserted into the map only if the mapping is absent, otherwise the existing value is returned. 
     * That is, if {@code typeName} is mapped to {@code null}, then {@code null} is returned and no computation takes place.
     * <p>
     * If {@code mappingFunction} produces {@code null}, then it is inserted into the map and returned.
     * 
     * @param elementCache Elements associated cache to use
     * @param name mapping key representing type element's name
     * @param mappingFunction
     * @return existing/computed value associated with the key
     */
    private TypeElement computeIfTrulyAbsent(
            final HashMap<String, TypeElement> elementCache, final String name, Supplier<TypeElement> mappingFunction) 
    {
        if (elementCache.containsKey(name)) {
            return elementCache.get(name);
        } else {
            final TypeElement elt = mappingFunction.get();
            elementCache.put(name, elt);
            return elt;
        }
    }

}
