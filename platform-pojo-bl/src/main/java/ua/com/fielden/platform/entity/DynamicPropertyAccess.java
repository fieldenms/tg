package ua.com.fielden.platform.entity;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static ua.com.fielden.platform.reflection.Reflector.DOT_SPLITTER_PATTERN;

/**
 * Provides dynamic name-based access (read & write) to property values of entity instances.
 */
final class DynamicPropertyAccess {

    public static final DynamicPropertyAccess INSTANCE = new DynamicPropertyAccess(new PropertyIndexerImpl());

    /**
     * Returns the value of the named property in {@code entity}. Fails if the named property cannot be located.
     *
     * @param prop  property path
     */
    public Object getProperty(final AbstractEntity<?> entity, final CharSequence prop) {
        final String[] propPath = DOT_SPLITTER_PATTERN.split(prop);
        final AbstractEntity<?> lastPropOwner = lastPropOwner(entity, propPath);
        return lastPropOwner == null ? null : getProperty_(lastPropOwner, last(propPath));
    }

    /**
     * Returns an entity that owns the last property in the path.
     * <p>
     * Examples:
     * <pre>
     * (entity, "x")   -> entity
     * (entity, "x.y") -> entity.x
     * </pre>
     */
    private @Nullable AbstractEntity<?> lastPropOwner(final AbstractEntity<?> entity, final String[] path) {
        AbstractEntity<?> localEntity = entity;
        for (int i = 0; i < path.length - 1; i++) {
            localEntity = (AbstractEntity<?>) getProperty_(localEntity, path[i]);
            if (localEntity == null) {
                return null;
            }
        }
        return localEntity;
    }

    /**
     * Helper method for {@link #getProperty(AbstractEntity, CharSequence)}.
     *
     * @param prop  simple property name
     */
    private Object getProperty_(final AbstractEntity<?> entity, final String prop) {
        Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) entity.getClass();
        final VarHandle vh = indexer.indexFor(entityType).propertyHandles().get(prop);
        if (vh == null) {
            throw new IllegalArgumentException("Failed to resolve property [%s] in entity [%s]".formatted(
                    prop, entityType.getTypeName()));
        }

        return vh.get(entity);
    }

    /**
     * Assigns the value to the named property in {@code entity}. Fails if the named property cannot be located.
     * <p>
     * Unlike {@link #getProperty(AbstractEntity, CharSequence)}, property assignment is supported only for first-level properties.
     *
     * @param prop  simple property name
     */
    public void setProperty(final AbstractEntity<?> entity, final CharSequence prop, final Object value) {
        Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) entity.getClass();
        final MethodHandle setter = indexer.indexFor(entityType).setters().get(prop.toString());
        if (setter == null) {
            throw new IllegalArgumentException("Failed to resolve setter for property [%s] in entity [%s]".formatted(
                    prop, entityType.getTypeName()));
        }

        try {
            setter.invoke(entity, value);
        } catch (final Throwable e) {
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }
    }

    /**
     * Scans an entity type and builds an internal data structure, if it's not already built, to enable efficient dynamic
     * property access.
     * <p>
     * In general, such internal data structures are built on demand, but this method can be used to build them ahead of time.
     */
    public void scan(final Class<? extends AbstractEntity<?>> entityType) {
        // TODO either remove this method or change its semantics
        throw new UnsupportedOperationException();
    }

    /**
     * @see #scan(Class)
     */
    public void scan(final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
        entityTypes.forEach(this::scan);
    }

    // --- Implementation

    private final PropertyIndexer indexer;

    DynamicPropertyAccess(final PropertyIndexer indexer) {
        this.indexer = indexer;
    }

    private static <X> X last(final X[] xs) {
        return xs[xs.length - 1];
    }

}
