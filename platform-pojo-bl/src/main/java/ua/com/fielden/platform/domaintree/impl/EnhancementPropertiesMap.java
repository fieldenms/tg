package ua.com.fielden.platform.domaintree.impl;

import java.util.HashMap;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.Pair;

/**
 * A map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types. It can be used with enhanced types, but inner mechanism
 * will "persist" not enhanced ones.
 *
 * @author TG Team
 * @param <T> -- a type of values in map
 */
public class EnhancementPropertiesMap <T> extends HashMap<Pair<Class<?>, String>, T> {
    private static final long serialVersionUID = -1754488088205944423L;

    public EnhancementPropertiesMap() {
	super();
    }

    @Override
    public boolean containsKey(final Object key) {
	final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
	return super.containsKey(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
    };

    @Override
    public T put(final Pair<Class<?>,String> key, final T value) {
	return super.put(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key.getKey()), key.getValue()), value);
    };

    @Override
    public T get(final Object key) {
	final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
	return super.get(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
    };

    @Override
    public T remove(final Object key) {
	final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
        return super.remove(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
    }
}
