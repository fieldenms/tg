package ua.com.fielden.platform.treemodel.rules.impl;

import java.util.LinkedHashSet;

import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * A set of properties (pairs root+propertyName). This set will correctly handle "enhanced" root types.
 * It can be used with enhanced types, but inner mechanism will "persist" not enhanced ones.
 *
 * @author TG Team
 *
 */
public class EnhancementLinkedRootsSet extends LinkedHashSet<Class<?>> {
    private static final long serialVersionUID = 1L;

    public EnhancementLinkedRootsSet() {
	super();
    }

    @Override
    public boolean contains(final Object o) {
	final Class<?> root = (Class<?>) o;
	return super.contains(DynamicEntityClassLoader.getOriginalType(root));
    };
}
