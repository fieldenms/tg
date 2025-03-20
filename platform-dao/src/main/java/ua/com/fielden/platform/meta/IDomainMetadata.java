package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/**
 * Service that provides domain metadata specific to a TG application.
 */
public interface IDomainMetadata {

    PropertyMetadataUtils propertyMetadataUtils();

    EntityMetadataUtils entityMetadataUtils();

    Optional<? extends TypeMetadata> forType(Class<?> javaType);

    /**
     * Non-throwing alternative to {@link #forEntity(Class)}.
     */
    Optional<EntityMetadata> forEntityOpt(Class<? extends AbstractEntity<?>> entityType);

    /**
     * Retrieves metadata for an entity or throws if an entity is unfit for metadata generation.
     * </p>
     * {@link #forEntityOpt(Class)} is a non-throwing alternative.
     */
    EntityMetadata forEntity(Class<? extends AbstractEntity<?>> entityType);

    /**
     * Empty optional is returned if the given type is not a component or not a known component type.
     */
    Optional<TypeMetadata.Component> forComponent(Class<?> javaType);

    /**
     * A non-throwing alternative to {@link #forProperty(Class, CharSequence)}.
     */
    Optional<PropertyMetadata> forPropertyOpt(Class<?> enclosingType, CharSequence propPath);

    /**
     * Provides access to property metadata.
     * An exception is thrown if either of the following holds:
     * <ul>
     *   <li>a property cannot be located at the given path
     *   <li>{@code enclosingType} is not part of the domain, hence there is no metadata associated with it.
     * </ul>
     *
     * {@link #forPropertyOpt(Class, CharSequence)} is a non-throwing alternative.
     *
     * @param propPath  property path (dot-expression is supported)
     */
    PropertyMetadata forProperty(Class<?> enclosingType, CharSequence propPath);

    /**
     * Returns metadata for a property represented by the given meta-property.
     * <ul>
     *   <li> If the property has metadata, returns an optional describing it.
     *   <li> If the property doesn't have metadata but satisfies {@link AbstractEntity#isAlwaysMetaProperty(String)},
     *   returns an empty optional.
     *   <li> Otherwise, returns an error.
     * </ul>
     */
    Either<RuntimeException, Optional<PropertyMetadata>> forProperty(MetaProperty<?> metaProperty);

}
