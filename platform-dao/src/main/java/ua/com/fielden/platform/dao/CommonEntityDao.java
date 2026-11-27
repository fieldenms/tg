package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import static java.lang.String.format;

/// A base type for entity companions.
///
/// Subclasses specify the entity type with the class-level [EntityType] annotation.
/// The key type is derived from the entity type.
///
public abstract class CommonEntityDao<T extends AbstractEntity<?>> extends AbstractEntityDao<T> {

    private final Class<? extends Comparable<?>> keyType;
    private final Class<T> entityType;

    /**
     * The default constructor, which looks for annotation {@link EntityType} to identify the entity type automatically.
     * An exception is thrown if the annotation is missing.
     * <p>
     * <b>Deprecated</b>: use the no-arg constructor (the {@code super} call is no longer necessary).
     */
    @Deprecated(forRemoval = true)
    protected CommonEntityDao(final IFilter filter) {
        this();
    }

    @SuppressWarnings("unchecked")
    protected CommonEntityDao() {
        final EntityType annotation = AnnotationReflector.getAnnotation(getClass(), EntityType.class);
        if (annotation == null) {
            throw new EntityCompanionException(format("Companion object [%s] is missing @EntityType annotation.", getClass().getName()));
        }
        this.entityType = (Class<T>) annotation.value();
        this.keyType = AnnotationReflector.getKeyType(entityType);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return keyType;
    }

}
