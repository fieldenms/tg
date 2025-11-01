package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.Pair;

import java.util.HashSet;

/// A set of properties, represented as pairs `(root, property)`.
/// This set will correctly handle enhanced root types.
/// Its methods accept enhanced types as arguments, but the contents of the set are always the original types.
///
public class EnhancementSet extends HashSet<Pair<Class<?>, String>> {
    private static final long serialVersionUID = 7773953570321667258L;

    public EnhancementSet() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object key) {
        return super.contains(updateWithOriginalType((Pair<Class<?>, String>) key));
    }

    @Override
    public boolean add(final Pair<Class<?>, String> key) {
        return super.add(updateWithOriginalType(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object key) {
        return super.remove(updateWithOriginalType((Pair<Class<?>, String>) key));
    }

    private static Pair<Class<?>, String> updateWithOriginalType(final Pair<Class<?>, String> pair) {
        final Class<?> originalType = DynamicEntityClassLoader.getOriginalType(pair.getKey());
        return originalType.equals(pair.getKey()) ? pair : new Pair<>(originalType, pair.getValue());
    }

}
