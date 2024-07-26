package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

import static ua.com.fielden.platform.reflection.Reflector.DOT_SPLITTER_PATTERN;

/**
 * Provides dynamic name-based access (read & write) to property values of entity instances.
 */
final class DynamicPropertyAccess {

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
        final var getter = indexer.indexFor(entityType).getter(prop);
        if (getter == null) {
            throw new IllegalArgumentException("Failed to resolve property [%s] in entity [%s]".formatted(
                    prop, entityType.getTypeName()));
        }

        try {
            return getter.invoke(entity);
        } catch (final Throwable e) {
            throw new RuntimeException("Failed to invoke getter for property [%s] in entity [%s]".formatted(
                    prop, entityType.getTypeName()), e);
        }
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
        final MethodHandle setter = indexer.indexFor(entityType).setter(prop.toString());
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
     * Builds an index for an entity type, internally, to enable efficient dynamic property access.
     * <p>
     * Whether indices are cached depends on the active Guice module bindings.
     * If they are cached, this method can be used to build them ahead of time.
     */
    public void index(final Class<? extends AbstractEntity<?>> entityType) {
        indexer.indexFor(entityType);
    }

    /**
     * @see #index(Class)
     */
    public void index(final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
        entityTypes.forEach(this::index);
    }

    // --- Implementation

    private final PropertyIndexer indexer;

    @Inject
    DynamicPropertyAccess(final PropertyIndexer indexer) {
        this.indexer = indexer;
    }

    private static <X> X last(final X[] xs) {
        return xs[xs.length - 1];
    }

}
