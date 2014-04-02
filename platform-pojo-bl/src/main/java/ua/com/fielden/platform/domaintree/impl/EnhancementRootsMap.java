package ua.com.fielden.platform.domaintree.impl;

import java.util.HashMap;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * A map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types. It can be used with enhanced types, but inner mechanism will
 * "persist" not enhanced ones.
 * 
 * @author TG Team
 * 
 * @param <T>
 *            -- a type of values in map
 */
public class EnhancementRootsMap<T> extends HashMap<Class<?>, T> {
    private static final long serialVersionUID = -2157556231184035447L;

    public EnhancementRootsMap() {
        super();
    }

    @Override
    public boolean containsKey(final Object key) {
        final Class<?> key1 = (Class<?>) key;
        return super.containsKey(DynamicEntityClassLoader.getOriginalType(key1));
    };

    @Override
    public T put(final Class<?> key, final T value) {
        return super.put(DynamicEntityClassLoader.getOriginalType(key), value);
    };

    @Override
    public T get(final Object key) {
        final Class<?> key1 = (Class<?>) key;
        return super.get(DynamicEntityClassLoader.getOriginalType(key1));
    };

    @Override
    public T remove(final Object key) {
        final Class<?> key1 = (Class<?>) key;
        return super.remove(DynamicEntityClassLoader.getOriginalType(key1));
    }
}
