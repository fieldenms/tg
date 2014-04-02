package ua.com.fielden.platform.domaintree.impl;

import java.util.HashSet;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.Pair;

/**
 * A set of properties (pairs root+propertyName). This set will correctly handle "enhanced" root types. It can be used with enhanced types, but inner mechanism will "persist" not
 * enhanced ones.
 * 
 * @author TG Team
 * 
 */
public class EnhancementSet extends HashSet<Pair<Class<?>, String>> {
    private static final long serialVersionUID = 7773953570321667258L;

    public EnhancementSet() {
        super();
    }

    @Override
    public boolean contains(final Object key) {
        final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
        final Class<?> originalType = DynamicEntityClassLoader.getOriginalType(key1.getKey());
        return super.contains(new Pair<Class<?>, String>(originalType, key1.getValue()));
    }

    @Override
    public boolean add(final Pair<Class<?>, String> key) {
        return super.add(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key.getKey()), key.getValue()));
    }

    @Override
    public boolean remove(final Object key) {
        final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
        return super.remove(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
    }
}