package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Service that provides domain metadata specific to a TG application.
 */
public interface IDomainMetadata {

    PropertyMetadataUtils propertyMetadataUtils();

    EntityMetadataUtils entityMetadataUtils();

    /**
     * Returns all existing type metadata instances.
     */
    Stream<TypeMetadata> allTypes();

    /**
     * Returns all existing type metadata instances that are of the given metadata type.
     */
    <T extends TypeMetadata> Stream<T> allTypes(Class<T> metadataType);

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
     * Empty optional is returned if the given type is not composite or not a known composite type.
     */
    Optional<TypeMetadata.Composite> forComposite(Class<?> javaType);

    /**
     * A non-throwing alternative to {@link #forProperty(Class, CharSequence)}.
     */
    Optional<PropertyMetadata> forPropertyOpt(Class<?> enclosingType, CharSequence propPath);

    /**
     * Provides access to property metadata.
     * An exception will be thrown if either of the following holds:
     * <ul>
     *   <li>a property with the given name cannot be found in the given type's metadata
     *   <li>the given type is not part of the domain, hence there is no metadata associated with it
     * </ul>
     *
     * {@link #forPropertyOpt(Class, CharSequence)} is a non-throwing alternative.
     *
     * @param propPath  property path (dot-notation supported)
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
