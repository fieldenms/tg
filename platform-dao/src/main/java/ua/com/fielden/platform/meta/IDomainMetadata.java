package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.Optional;

/**
 * Service that provides domain metadata specific to a TG application.
 */
public interface IDomainMetadata {

    PropertyMetadataUtils propertyMetadataUtils();

    EntityMetadataUtils entityMetadataUtils();

    /**
     * Returns a collection of all existing type metadata instances.
     */
    Collection<? extends TypeMetadata> allTypes();

    /**
     * Returns a collection of all existing type metadata instances that are of the given metadata type.
     */
    <T extends TypeMetadata> Collection<T> allTypes(Class<T> metadataType);

    Optional<TypeMetadata> forType(Class<?> javaType);

    EntityMetadata forEntity(Class<? extends AbstractEntity<?>> entityType);

    /**
     * Empty optional is returned if the given type is not composite or not a known composite type.
     */
    Optional<TypeMetadata.Composite> forComposite(Class<?> javaType);

    /**
     * Empty optional is returned if either of the following holds:
     * <ul>
     *   <li>a property with the given name cannot be found in the given type's metadata
     *   <li>the given type is not part of the domain, hence there is no metadata associated with it
     * </ul>
     *
     * @param propPath  property path (dot-notation supported)
     */
    Optional<PropertyMetadata> forProperty(Class<?> enclosingType, CharSequence propPath);

}
