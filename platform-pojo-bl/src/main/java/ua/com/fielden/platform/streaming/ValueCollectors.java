package ua.com.fielden.platform.streaming;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A collection of static helper methods to instantiate alternative collectors to those provided in {@link Collectors}.
 * The main motivation is be able to have a collectors that
 * produce collections of specific types such as {@link LinkedHashMap}.
 * 
 * @author TG Team
 *
 */
public class ValueCollectors {
    
    private ValueCollectors() {}

    /**
     * Collects elements of a stream into a new instance of type {@link LinkedHashMap}.
     * <p>
     * Internally, method {@link Collectors#toMap} uses {@link HashMap#merge(Object, Object, java.util.function.BiFunction)}, which throws NPE in case of either key or value is <code>null</code>.
     * Therefore, depending on a paricular situation, it might be prudent to wrap the mapped values in {@link Optional} when implementing function <code>valueMapper</code>.
     * 
     * @param keyMapper -- Maps elements to corresponding keys.
     * @param valueMapper -- Maps elements to corresponding values.
     * @return
     */
    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(
            final Function<? super T, ? extends K> keyMapper, 
            final Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper, 
                valueMapper, 
                (k, v) -> {throw new IllegalStateException(format("A duplicate value occurred for key [%s].", k));}, 
                LinkedHashMap::new);
    }
    
    /**
     * Collects elements of a stream into a new instance of type {@link TreeMap}.
     * <p>
     * Internally, method {@link Collectors#toMap} uses {@link HashMap#merge(Object, Object, java.util.function.BiFunction)}, which throws NPE in case of either key or value is <code>null</code>.
     * Therefore, depending on a paricular situation, it might be prudent to wrap the mapped values in {@link Optional} when implementing function <code>valueMapper</code>.
     * 
     * @param keyMapper -- Maps elements to corresponding keys.
     * @param valueMapper -- Maps elements to corresponding values.
     * @return
     */
    public static <T, K, U> Collector<T, ?, TreeMap<K, U>> toTreeMap(
            final Function<? super T, ? extends K> keyMapper, 
            final Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper, 
                valueMapper, 
                (k, v) -> {throw new IllegalStateException(format("A duplicate value occurred for key [%s].", k));}, 
                TreeMap::new);
    }
}
