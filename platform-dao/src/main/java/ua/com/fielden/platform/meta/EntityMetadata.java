package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.types.either.Either;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents entity type metadata.
 * <p>
 * Each entity type is classified according to its nature. A nature may specify essential metadata that is attributed an
 * entity type. This essential data can be accessed via {@link #data()}.
 * Matching on the nature can be performed with the {@code switch} statement or via {@link #match(EntityMetadataVisitor)}.
 * <p>
 * The set of properties includes both declared and inherited ones.
 * <p>
 * No metadata exists for entity nature {@link EntityNature.Other}.
 */
public sealed interface EntityMetadata extends TypeMetadata {

    @Override
    Class<? extends AbstractEntity<?>> javaType();

    EntityNature nature();

    EntityNature.Data data();

    Collection<PropertyMetadata> properties();

    /**
     * Retrieves metadata for a property if it exists in this entity type, otherwise throws an exception.
     * </p>
     * {@link #propertyOpt(String)} is a non-throwing alternative.
     */
    PropertyMetadata property(String name);

    /**
     * Non-throwing alternative to {@link #property(String)}.
     */
    Optional<PropertyMetadata> propertyOpt(String name);

    /**
     * Returns metadata for a property represented by the given meta-property.
     * <ul>
     *   <li> If the property has metadata, returns an optional describing it.
     *   <li> If the property doesn't have metadata but satisfies {@link AbstractEntity#isAlwaysMetaProperty(String)},
     *   returns an empty optional.
     *   <li> Otherwise, returns an error.
     * </ul>
     */
    Either<RuntimeException, Optional<PropertyMetadata>> property(MetaProperty<?> metaProperty);

    <R> R match(EntityMetadataVisitor<R> visitor);

    default boolean isPersistent() {
        return this instanceof Persistent;
    }

    default boolean isSynthetic() {
        return this instanceof Synthetic;
    }

    default boolean isUnion() {
        return this instanceof Union;
    }

    // ****************************************
    // * Convenient methods as an alternative to a visitor with a single clause

    default Optional<EntityMetadata.Union> asUnion() {
        return this instanceof Union u ? Optional.of(u) : Optional.empty();
    }

    default Optional<EntityMetadata.Persistent> asPersistent() {
        return this instanceof Persistent p ? Optional.of(p) : Optional.empty();
    }

    default Optional<EntityMetadata.Synthetic> asSynthetic() {
        return this instanceof Synthetic s ? Optional.of(s) : Optional.empty();
    }

    non-sealed interface Persistent extends EntityMetadata {
        @Override
        EntityNature.Persistent nature();

        @Override
        EntityNature.Persistent.Data data();
    }

    non-sealed interface Synthetic extends EntityMetadata {
        @Override
        EntityNature.Synthetic nature();

        @Override
        EntityNature.Synthetic.Data data();
    }

    non-sealed interface Union extends EntityMetadata {
        @Override
        Class<? extends AbstractUnionEntity> javaType();

        @Override
        EntityNature.Union nature();

        @Override
        EntityNature.Union.Data data();
    }

}
