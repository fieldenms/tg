package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

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
sealed public interface EntityMetadata extends TypeMetadata {

    @Override
    Class<? extends AbstractEntity<?>> javaType();

    EntityNature nature();

    EntityNature.Data data();

    Collection<PropertyMetadata> properties();

    Optional<PropertyMetadata> property(String name);

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